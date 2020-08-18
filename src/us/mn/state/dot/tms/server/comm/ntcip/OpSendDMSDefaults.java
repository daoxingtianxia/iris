/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
import static us.mn.state.dot.tms.SystemAttrEnum.*;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibskyline.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.BadValue;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;

/**
 * Operation to send default parameters to a DMS.
 *
 * @author Douglas Lau
 */
public class OpSendDMSDefaults extends OpDMS {

	/** Minimum threshold for comm loss.  Some Ledstar firmware
	 * behaves strangely when dmsTimeCommLoss.0 is 1 minute. */
	static private final int COMM_LOSS_MINIMUM_MINS = 2;

	/** Number of missed polling periods for comm loss threshold */
	static private final int COMM_LOSS_PERIODS = 10;

	/** Certain Ledstar firmware versions can lock up with
	 * a CTO error if dmsTimeCommLoss is set to a non-zero value */
	static private final HashSet<String> CTO_BLACKLIST =
		new HashSet<String>(Arrays.asList(
		"VMS-MN2A-27x105 V2.6 Apr 20,2011"
	));

	/** Create a new operation to send DMS default parameters */
	public OpSendDMSDefaults(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new SetCommPowerLoss();
	}

	/** Phase to set the comm and power loss times */
	protected class SetCommPowerLoss extends Phase {

		/** Set the comm loss action */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer power_time =dmsShortPowerLossTime.makeInt();
			ASN1Integer comm_time = dmsTimeCommLoss.makeInt();
			MessageIDCode end_msg = new MessageIDCode(
				dmsEndDurationMessage.node);
			power_time.setInteger(0);
			comm_time.setInteger(getCommLossMinutes());
			end_msg.setMemoryType(DmsMessageMemoryType.blank);
			end_msg.setNumber(1);
			end_msg.setCrc(0);
			mess.add(power_time);
			mess.add(comm_time);
			mess.add(end_msg);
			logStore(power_time);
			logStore(comm_time);
			logStore(end_msg);
			mess.storeProps();
			if (dms.getSupportsPixelServiceObject())
				return new PixelService();
			else
				return new MessageDefaults();
		}
	}

	/** Get the comm loss threshold */
	private int getCommLossMinutes() {
		return (isCommLossEnabled() && !isCommLossBlacklisted())
		     ? Math.max(COMM_LOSS_MINIMUM_MINS, getLinkCommLossMins())
		     : 0;
	}

	/** Is DMS comm loss enabled? */
	private boolean isCommLossEnabled() {
		return DMS_COMM_LOSS_ENABLE.getBoolean();
	}

	/** Is the controller blacklisted for comm loss setting */
	private boolean isCommLossBlacklisted() {
		return CTO_BLACKLIST.contains(controller.getVersion());
	}

	/** Get the comm loss threshold for the comm link */
	private int getLinkCommLossMins() {
		return controller.getPollPeriodSec() * COMM_LOSS_PERIODS / 60;
	}

	/** Phase to set the pixel service schedule */
	protected class PixelService extends Phase {

		/** Set the pixel service schedule */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dur = vmsPixelServiceDuration.makeInt();
			ASN1Integer freq = vmsPixelServiceFrequency.makeInt();
			ASN1Integer time = vmsPixelServiceTime.makeInt();
			dur.setInteger(10);
			freq.setInteger(1440);
			time.setInteger(180);
			mess.add(dur);
			mess.add(freq);
			mess.add(time);
			logStore(dur);
			logStore(freq);
			logStore(time);
			mess.storeProps();
			return new MessageDefaults();
		}
	}

	/** Phase to set the message defaults */
	protected class MessageDefaults extends Phase {

		/** Set the message defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<JustificationLine> line = new ASN1Enum<
				JustificationLine>(JustificationLine.class,
				defaultJustificationLine.node);
			ASN1Enum<JustificationPage> page = new ASN1Enum<
				JustificationPage>(JustificationPage.class,
				defaultJustificationPage.node);
			ASN1Integer on_time = defaultPageOnTime.makeInt();
			ASN1Integer off_time = defaultPageOffTime.makeInt();
			line.setInteger(DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
			page.setInteger(DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
			on_time.setInteger(Math.round(10 *
				DMS_PAGE_ON_DEFAULT_SECS.getFloat()));
			off_time.setInteger(Math.round(10 *
				DMS_PAGE_OFF_DEFAULT_SECS.getFloat()));
			mess.add(line);
			mess.add(page);
			mess.add(on_time);
			mess.add(off_time);
			logStore(line);
			logStore(page);
			logStore(on_time);
			logStore(off_time);
			mess.storeProps();
			return new MessageDefaultsV2();
		}
	}

	/** Phase to set the V2 message defaults */
	protected class MessageDefaultsV2 extends Phase {

		/** Set the message defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1OctetString fg = getDefaultForeground();
			ASN1OctetString bg = getDefaultBackground();
			mess.add(bg);
			mess.add(fg);
			logStore(bg);
			logStore(fg);
			try {
				mess.storeProps();
			}
			catch (ControllerException e) {
				// NoSuchName: not a v2 sign
				// GenError: unsupported color
				// BadValue: who knows?
			}
			return new LedstarDefaults();
		}
	}

	/** Get the default background color */
	private ASN1OctetString getDefaultBackground() throws IOException {
		ASN1OctetString bg = new ASN1OctetString(
			defaultBackgroundRGB.node);
		bg.setOctetString(DMSHelper.getDefaultBackgroundBytes(dms));
		return bg;
	}

	/** Get the default foreground color */
	private ASN1OctetString getDefaultForeground() throws IOException {
		ASN1OctetString fg = new ASN1OctetString(
			defaultForegroundRGB.node);
		fg.setOctetString(DMSHelper.getDefaultForegroundBytes(dms));
		return fg;
	}

	/** Phase to set Ledstar-specific object defaults */
	protected class LedstarDefaults extends Phase {

		/** Set Ledstar-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = ledHighTempCutoff.makeInt();
			ASN1Integer override = ledSignErrorOverride.makeInt();
			ASN1Integer limit = ledBadPixelLimit.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF.getInt());
			limit.setInteger(500);
			mess.add(temp);
			mess.add(override);
			mess.add(limit);
			try {
				mess.storeProps();
				logStore(temp);
				logStore(override);
				logStore(limit);
			}
			catch (NoSuchName e) {
				// Must not be a Ledstar sign
				return new SkylineDefaults();
			}
			catch (BadValue e) {
				// Daktronics uses this instead of NoSuchName
				// Is there a better way to check for that?
			}
			return null;
		}
	}

	/** Phase to set Skyline-specific object defaults */
	protected class SkylineDefaults extends Phase {

		/** Set Skyline-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = dmsTempCritical.makeInt();
			ASN1Integer day_night = dynBrightDayNight.makeInt();
			ASN1Integer day_rate = dynBrightDayRate.makeInt();
			ASN1Integer night_rate = dynBrightNightRate.makeInt();
			ASN1Integer max_lvl = dynBrightMaxNightManLvl.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF.getInt());
			day_night.setInteger(32);
			day_rate.setInteger(1);
			night_rate.setInteger(15);
			max_lvl.setInteger(20);
			mess.add(temp);
			mess.add(day_night);
			mess.add(day_rate);
			mess.add(night_rate);
			mess.add(max_lvl);
			try {
				mess.storeProps();
				logStore(temp);
				logStore(day_night);
				logStore(day_rate);
				logStore(night_rate);
				logStore(max_lvl);
			}
			catch (NoSuchName e) {
				// Must not be a Skyline sign
				return new AddcoDefaults();
			}
			return null;
		}
	}

	/** Phase to set ADDCO-specific object defaults */
	protected class AddcoDefaults extends Phase {

		/** Set ADDCO-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// ADDCO brick signs have these dimensions
			SignDetail sd = dms.getSignDetail();
			String make = (sd != null) ? sd.getSoftwareMake() : "";
			int dtype = (sd != null)
			          ? sd.getDmsType()
			          : DMSType.UNKNOWN.ordinal();
			// NOTE: setting these objects requires use of the
			//       "administrator" community name.  We need to
			//       check that the password is not null before
			//       attempting to set them.
			if (make != null &&
			    make.startsWith("ADDCO") &&
			    dtype == DMSType.VMS_CHAR.ordinal() &&
			    controller.getPassword() != null)
			{
				ASN1Integer h_border =
					dmsHorizontalBorder.makeInt();
				ASN1Integer v_border =
					dmsVerticalBorder.makeInt();
				ASN1Integer h_pitch =
					vmsHorizontalPitch.makeInt();
				ASN1Integer v_pitch =vmsVerticalPitch.makeInt();
				h_border.setInteger(50);
				v_border.setInteger(69);
				h_pitch.setInteger(69);
				v_pitch.setInteger(69);
				mess.add(h_border);
				mess.add(v_border);
				mess.add(h_pitch);
				mess.add(v_pitch);
				mess.storeProps();
				logStore(h_border);
				logStore(v_border);
				logStore(h_pitch);
				logStore(v_pitch);
			}
			return null;
		}
	}
}
