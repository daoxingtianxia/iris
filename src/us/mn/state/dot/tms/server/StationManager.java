/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.utils.FileIO;

/**
 * This class writes out station XML files.
 *
 * @author Douglas Lau
 */
class StationManager {

	/** Date formatter for RFC 3339 */
	static private final SimpleDateFormat RFC3339 =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	/** JSON output directory */
	static private final File JSON_OUTPUT_DIRECTORY =
		new File("/var/www/html/iris/");

	/** Location of station sample XML file */
	static private final String SAMPLE_XML = "stat_sample.xml";

	/** Name of station sample JSON file */
	static private final String SAMPLE_JSON = "station_sample";

	/** Calculate the current data for all stations */
	public void calculateData() {
		Iterator<Station> it = StationHelper.iterator();
		while (it.hasNext()) {
			Station s = it.next();
			if (s instanceof StationImpl) {
				StationImpl si = (StationImpl) s;
				si.calculateData();
			}
		}
	}

	/** Write the station sample data out as XML */
	public void writeSampleXml() throws IOException {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			@Override protected void write(Writer w)
				throws IOException
			{
				writeSampleXmlHead(w);
				writeSampleXmlBody(w);
				writeSampleXmlTail(w);
			}
		};
		w.write();
	}

	/** Write the station sample data out as JSON */
	public void writeSampleJson() throws IOException {
		File file = new File(JSON_OUTPUT_DIRECTORY, SAMPLE_JSON);
		File temp = new File(file.getAbsolutePath() + "~");
		FileWriter writer = new FileWriter(temp);
		try {
			writeSampleJson(writer);
		}
		finally {
			writer.flush();
			writer.close();
		}
		FileIO.atomicMove(temp.toPath(), file.toPath());
	}

	/** Write the station sample data out as JSON */
	private void writeSampleJson(FileWriter writer) throws IOException {
		BufferedWriter buf_writer = new BufferedWriter(writer);
		try {
			writeSampleJson(buf_writer);
		}
		finally {
			buf_writer.flush();
		}
	}

	/** Write the station sample data out as JSON */
	private void writeSampleJson(BufferedWriter writer) throws IOException {
		long end = DetectorImpl.calculateEndTime();
		long start = end - DetectorImpl.SAMPLE_PERIOD_MS;
		writer.write("{\n");
		writer.write("\"time_stamp\":\"");
		writer.write(RFC3339.format(TimeSteward.getDateInstance()));
		writer.write("\",\n");
		writer.write("\"period\":30,\n");
		writer.write("\"samples\":{\n");
		Iterator<Station> it = StationHelper.iterator();
		while (it.hasNext()) {
			Station s = it.next();
			if (s instanceof StationImpl) {
				StationImpl si = (StationImpl) s;
				if (si.getActive()) {
					si.writeSampleJson(start, end, writer);
					if (it.hasNext())
						writer.write(',');
					writer.write('\n');
				}
			}
		}
		writer.write("}\n");
		writer.write("}\n");
	}

	/** Print the header of the station sample XML file */
	private void writeSampleXmlHead(Writer w) throws IOException {
		w.write(XmlWriter.XML_DECLARATION);
		writeDtd(w);
		w.write("<traffic_sample time_stamp='" +
			TimeSteward.getDateInstance() + "' period='30'>\n");
	}

	/** Print the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE traffic_sample [\n");
		w.write("<!ELEMENT traffic_sample (sample)*>\n");
		w.write("<!ATTLIST traffic_sample time_stamp " +
			"CDATA #REQUIRED>\n");
		w.write("<!ATTLIST traffic_sample period CDATA #REQUIRED>\n");
		w.write("<!ELEMENT sample EMPTY>\n");
		w.write("<!ATTLIST sample sensor CDATA #REQUIRED>\n");
		w.write("<!ATTLIST sample flow CDATA 'UNKNOWN'>\n");
		w.write("<!ATTLIST sample speed CDATA 'UNKNOWN'>\n");
		w.write("<!ATTLIST sample occ CDATA 'UNKNOWN'>\n");
		w.write("]>\n");
	}

	/** Print the body of the station sample XML file */
	private void writeSampleXmlBody(Writer w) throws IOException {
		Iterator<Station> it = StationHelper.iterator();
		while (it.hasNext()) {
			Station s = it.next();
			if (s instanceof StationImpl) {
				StationImpl si = (StationImpl) s;
				si.writeSampleXml(w);
			}
		}
	}

	/** Print the tail of the station sample XML file */
	private void writeSampleXmlTail(Writer w) throws IOException {
		w.write("</traffic_sample>\n");
	}
}
