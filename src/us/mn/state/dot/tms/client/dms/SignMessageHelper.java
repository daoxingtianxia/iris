/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.LinkedList;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;

/**
 * Helper for dealing with sign messages.
 *
 * @author Douglas Lau
 */
public class SignMessageHelper {

	/** Do not allow objects of this class */
	private SignMessageHelper() {
		assert false;
	}

	/** Create an array of lines from the given message */
	static public String[] createLines(SignMessage m) {
		final LinkedList<String> ls = new LinkedList<String>();
		MultiString multi = m.getMulti();
		multi.parse(new MultiString.SpanCallback() {
			public void addSpan(int p,
				MultiString.JustificationPage jp, int l,
				MultiString.JustificationLine jl, String t)
			{
				int m_lines = Math.max(text_lines, l + 1);
				while(ls.size() < (p + 1) * m_lines)
					ls.add("");
				int i = p * m_lines + l;
				String v = ls.get(i);
				ls.set(i, trimJoin(v, t));
			}
		});
		return ls.toArray(new String[0]);
	}

	/** Join two strings with a space and then trim */
	static protected String trimJoin(String a, String b) {
		String j = a + " " + b;
		return j.trim();
	}
}
