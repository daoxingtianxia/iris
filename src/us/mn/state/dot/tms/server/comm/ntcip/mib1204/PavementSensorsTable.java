/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import java.text.NumberFormat;
import java.util.ArrayList;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import static us.mn.state.dot.tms.units.Distance.Units.MICROMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Pavement sensors data table, where each table row contains data read from
 * a single pavement sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class PavementSensorsTable {

	/** An exposure of 101 is an error condition or missing value */
	static private final int EXPOSURE_ERROR_MISSING = 101;

	/** Convert solar exposure to percent.
	 * @param e Exposure in percent with 101 indicating an error or missing
	 *          value.
	 * @return Exposure or null for missing */
	static private Integer convertExposure(ASN1Integer e) {
		if (e != null) {
			int ie = e.getInteger();
			if (ie >= 0 && ie < EXPOSURE_ERROR_MISSING)
				return ie;
		}
		return null;
	}

	/** A depth of 255 is an error condition or missing value */
	static private final int DEPTH_V1_ERROR_MISSING = 255;

	/** Convert depth to Distance.
	 * @param d Depth in millimeters with 255 indicating an error or missing
	 *          value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepthV1(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id < DEPTH_V1_ERROR_MISSING)
				return new Distance(id, MILLIMETERS);
		}
		return null;
	}

	/** A depth of 65,535 is an error condition or missing value */
	static private final int DEPTH_V2_ERROR_MISSING = 65535;

	/** Convert depth to Distance.
	 * @param d Depth in tenth of millimeters with 65,535 indicating an
	 *          error or missing value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepthV2(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id < DEPTH_V2_ERROR_MISSING)
				return new Distance(100 * id, MICROMETERS);
		}
		return null;
	}

	/** A salinity of 65535 is an error condition or missing value */
	static private final int SALINITY_ERROR_MISSING = 65535;

	/** Convert value to salinity.
	 * @param s Salinity in parts per 100,000 by weight with 65535
	 * 	    indicating an error or missing value.
	 * @return Depth distance or null for missing */
	static private Integer convertSalinity(ASN1Integer s) {
		if (s != null) {
			int is = s.getInteger();
			if (is < SALINITY_ERROR_MISSING)
				return is;
		}
		return null;
	}

	/** Number of sensors in table */
	public final ASN1Integer num_sensors = numEssPavementSensors.makeInt();

	/** Table row */
	static public class Row {
		public final DisplayString location;
		public final ASN1Enum<PavementType> pavement_type;
		public final HeightObject height;
		public final ASN1Integer exposure;
		public final ASN1Enum<PavementSensorType> sensor_type;
		public final ASN1Enum<SurfaceStatus> surface_status;
		public final TemperatureObject surface_temp;
		public final TemperatureObject pavement_temp;
		public final ASN1Enum<PavementSensorError> sensor_error;
		public final ASN1Integer surface_water_depth;
		public final ASN1Integer surface_ice_or_water_depth;
		public final ASN1Integer salinity;
		public final TemperatureObject surface_freeze_point;
		public final ASN1Enum<SurfaceBlackIceSignal> black_ice_signal;

		/** Create a table row */
		private Row(int row) {
			location = new DisplayString(
				essPavementSensorLocation.node, row);
			pavement_type = new ASN1Enum<PavementType>(
				PavementType.class, essPavementType.node, row);
			height = new HeightObject("height",
				essPavementElevation.makeInt(row));
			exposure = essPavementExposure.makeInt(row);
			exposure.setInteger(EXPOSURE_ERROR_MISSING);
			sensor_type = new ASN1Enum<PavementSensorType>(
				PavementSensorType.class,
				essPavementSensorType.node, row);
			surface_status = new ASN1Enum<SurfaceStatus>(
				SurfaceStatus.class, essSurfaceStatus.node,
				row);
			surface_temp = new TemperatureObject("surface_temp",
				essSurfaceTemperature.makeInt(row));
			pavement_temp = new TemperatureObject("pavement_temp",
				essPavementTemperature.makeInt(row));
			sensor_error = new ASN1Enum<PavementSensorError>(
				PavementSensorError.class,
				essPavementSensorError.node, row);
			surface_water_depth = essSurfaceWaterDepth.makeInt(row);
			surface_water_depth.setInteger(DEPTH_V1_ERROR_MISSING);
			surface_ice_or_water_depth =
				essSurfaceIceOrWaterDepth.makeInt(row);
			surface_ice_or_water_depth.setInteger(
				DEPTH_V2_ERROR_MISSING);
			salinity = essSurfaceSalinity.makeInt(row);
			salinity.setInteger(SALINITY_ERROR_MISSING);
			surface_freeze_point = new TemperatureObject(
				"surface_freeze_point",
				essSurfaceFreezePoint.makeInt(row));
			black_ice_signal = new ASN1Enum<SurfaceBlackIceSignal>(
				SurfaceBlackIceSignal.class,
				essSurfaceBlackIceSignal.node, row);
		}

		/** Get the sensor location */
		public String getSensorLocation() {
			String sl = location.getValue();
			return (sl.length() > 0) ? sl : null;
		}

		/** Get pavement type or null on error */
		public PavementType getPavementType() {
			PavementType pt = pavement_type.getEnum();
			return (pt != PavementType.undefined) ? pt : null;
		}

		/** Get pavement exposure in percent */
		public Integer getExposure() {
			return convertExposure(exposure);
		}

		/** Get pavement sensor type or null on error */
		public PavementSensorType getPavementSensorType() {
			PavementSensorType pst = sensor_type.getEnum();
			return (pst != PavementSensorType.undefined)
			      ? pst
			      : null;
		}

		/** Get surface status or null on error */
		public SurfaceStatus getSurfStatus() {
			SurfaceStatus ess = surface_status.getEnum();
			return (ess != SurfaceStatus.undefined) ? ess : null;
		}

		/** Get surface temp or null on error */
		public Integer getSurfTempC() {
			return surface_temp.getTempC();
		}

		/** Get pavement temp or null on error */
		public Integer getPvmtTempC() {
			return pavement_temp.getTempC();
		}

		/** Get pavement sensor error or null on error */
		public PavementSensorError getPavementSensorError() {
			PavementSensorError pse = sensor_error.getEnum();
			return (pse != null && pse.isError()) ? pse : null;
		}

		/** Get surface water depth formatted to meter units */
		private String getSurfaceWaterDepth() {
			Distance d = convertDepthV1(surface_water_depth);
			if (d != null) {
				Float mm = d.asFloat(METERS);
				NumberFormat f = NumberFormat.getInstance();
				f.setMaximumFractionDigits(3); // mm
				f.setMinimumFractionDigits(1);
				return f.format(mm);
			} else
				return null;
		}

		/** Get surface ice or water depth formatted to meter units */
		private String getSurfaceIceOrWaterDepth() {
			Distance d = convertDepthV2(surface_ice_or_water_depth);
			if (d != null) {
				Float mm = d.asFloat(METERS);
				NumberFormat f = NumberFormat.getInstance();
				f.setMaximumFractionDigits(4); // tenth of mm
				f.setMinimumFractionDigits(1);
				return f.format(mm);
			} else
				return null;
		}

		/** Get surface salinity in parts per 100,000 by weight */
		public Integer getSalinity() {
			return convertSalinity(salinity);
		}

		/** Get surf freeze temp or null on error */
		public Integer getSurfFreezePointC() {
			return surface_freeze_point.getTempC();
		}

		/** Get black ice signal or null on error */
		public SurfaceBlackIceSignal getBlackIceSignal() {
			SurfaceBlackIceSignal bis = black_ice_signal.getEnum();
			return (bis != null && bis.isValue()) ? bis : null;
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.str("location", getSensorLocation()));
			sb.append(Json.str("pavement_type", getPavementType()));
			sb.append(height.toJson());
			sb.append(Json.num("exposure", getExposure()));
			sb.append(Json.str("sensor_type",
				getPavementSensorType()));
			sb.append(Json.str("surface_status", getSurfStatus()));
			sb.append(surface_temp.toJson());
			sb.append(pavement_temp.toJson());
			sb.append(Json.str("sensor_error",
				getPavementSensorError()));
			sb.append(Json.num("surface_water_depth",
				getSurfaceWaterDepth()));
			sb.append(Json.num("surface_ice_or_water_depth",
				getSurfaceIceOrWaterDepth()));
			sb.append(Json.num("salinity", getSalinity()));
			sb.append(surface_freeze_point.toJson());
			sb.append(Json.str("black_ice_signal",
				getBlackIceSignal()));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("},");
			return sb.toString();
		}
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Get number of rows in table reported by ESS */
	private int size() {
		return num_sensors.getInteger();
	}

	/** Check if all rows have been read */
	public boolean isDone() {
		return table_rows.size() >= size();
	}

	/** Add a row to the table */
	public Row addRow() {
		Row tr = new Row(table_rows.size() + 1);
		table_rows.add(tr);
		return tr;
	}

	/** Get one table row */
	public Row getRow(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1)
		      : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		if (table_rows.size() > 0) {
			sb.append("\"pavement_sensor\":[");
			for (Row row : table_rows)
				sb.append(row.toJson());
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("],");
		}
		return sb.toString();
	}
}
