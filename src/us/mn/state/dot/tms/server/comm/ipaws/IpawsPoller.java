/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ipaws;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import static us.mn.state.dot.tms.server.CapAlert.LOG;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.FeedPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTPS;

/**
 * Integrated Public Alert and Warning System (IPAWS) poller, which
 * periodically retrieves emergency alerts generated by IPAWS, a system
 * managed by FEMA to help disseminate alerts to the public.  Administrators
 * must specify a URL that contains an identification number obtained from
 * FEMA that is unique to each organization.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class IpawsPoller extends ThreadedPoller<IpawsProperty>
	implements FeedPoller
{
	/** Log a message to the debug log */
	static public void slog(String msg) {
		LOG.log(msg);
	}

	/** Create a new poller */
	public IpawsPoller(CommLink link) {
		super(link, HTTPS, LOG);
	}

	/** Create a comm thread */
	@Override
	protected IpawsThread createCommThread(String uri, int timeout, int nrd)
	{
		return new IpawsThread(this, queue, scheme, uri, timeout, nrd,
			LOG);
	}

	/** Query IPAWS for alert messages */
	@Override
	public void queryFeed(ControllerImpl c) {
		slog("creating OpReadIpaws: " + c);
		addOp(new OpReadIpaws(c, name));
	}

	/** Run a test of the IPAWS Alert processing system.
	 *
	 *  Note that this does not test communication with the IPAWS system
	 *  itself, which is assumed to be working and relatively easy to
	 *  verify/debug, but instead tests the machinery in IRIS to read,
	 *  parse, process, and deploy these alerts to DMS. */
	public void startTesting(ControllerImpl c) {
		slog("creating OpTestIpaws: " + c);
		addOp(new OpTestIpaws(c, name));
	}
}
