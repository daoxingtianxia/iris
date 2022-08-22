/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Query the state of a beacon
 *
 * @author Douglas Lau
 */
public class OpQueryBeaconState extends OpDevice<CBWProperty> {

	/** Beacon device */
	private final BeaconImpl beacon;

	/** Relay state property */
	private final CBWProperty state;

	/** Create a new query beacon state operation */
	public OpQueryBeaconState(BeaconImpl b) {
		super(PriorityLevel.SHORT_POLL, b);
		beacon = b;
		String m = ControllerHelper.getSetup(controller, "model");
		Model mdl = Model.fromValue(m);
		state = new CBWProperty(mdl.statePath());
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseTwo() {
		return new QueryBeacon();
	}

	/** Phase to query the beacon status */
	private class QueryBeacon extends Phase<CBWProperty> {

		/** Query the beacon status */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			mess.add(state);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			beacon.setFlashingNotify(getBeaconRelay());
			setMaintStatus(formatMaintStatus());
		}
		super.cleanup();
	}

	/** Get beacon relay state */
	private boolean getBeaconRelay() {
		return state.getRelay(beacon.getPin());
	}

	/** Format the maintenance status */
	private String formatMaintStatus() {
		Integer vp = beacon.getVerifyPin();
		if (vp != null) {
			boolean f = getBeaconRelay();
			boolean v = state.getInput(vp);
			if (f && !v)
				return "Verify failed";
			if (v && !f)
				return "Verify stuck";
		}
		return "";
	}
}
