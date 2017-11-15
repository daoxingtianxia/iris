/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  Minnesota Department of Transportation
 * Copyright (C) 2017       SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.addco;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * A Poller to communicate with ADDCO signs (NodeComm).
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class AddcoPoller extends ThreadedPoller<AddcoProperty>
	implements DMSPoller
{
	/** Addco debug log */
	static private final DebugLog ADDCO_LOG = new DebugLog("addco");

	/** Create a new ADDCO poller */
	public AddcoPoller(String n) {
		super(n, TCP, ADDCO_LOG);
		attrCommIdleDisconnect = SystemAttrEnum.COMM_IDLE_DISCONNECT_DMS_SEC;
	}

	/** Create a comm thread */
	@Override
	protected AddcoThread createCommThread(String uri, int timeout) {
		return new AddcoThread(this, queue, scheme, uri, timeout,
			ADDCO_LOG);
	}

	/** Send a device request message to the sign */
	@Override
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		switch (r) {
		case QUERY_CONFIGURATION:
			addOp(new OpQueryDMSConfiguration(dms));
			break;
		case QUERY_MESSAGE:
			addOp(new OpQueryDMSMessage(dms));
			break;
		case QUERY_STATUS:
			addOp(new OpQueryDMSStatus(dms));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	@Override
	public void sendMessage(DMSImpl dms, SignMessage sm) {
		addOp(new OpSendDMSMessage(dms, sm));
	}
}