// Copyright (C) 2022  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

/// SQL query for one alarm
pub const ALARM: &str = "\
SELECT name, description, controller, pin, state, trigger_time \
FROM iris.alarm \
WHERE name = $1";

/// SQL query for one beacon
pub const BEACON: &str = "\
SELECT b.name, location, controller, pin, verify_pin, geo_loc, message, notes, \
       preset, flashing \
FROM iris.beacon b \
LEFT JOIN geo_loc_view gl ON b.geo_loc = gl.name \
WHERE b.name = $1";

/// SQL query for one cabinet style
pub const CABINET_STYLE: &str = "\
SELECT name, police_panel_pin_1, police_panel_pin_2, watchdog_reset_pin_1, \
       watchdog_reset_pin_2, dip \
FROM iris.cabinet_style \
WHERE name = $1";

/// SQL query for one camera
pub const CAMERA: &str = "\
SELECT c.name, location, geo_loc, controller, pin, notes, cam_num, publish, \
       streamable, cam_template, encoder_type, enc_address, enc_port, \
       enc_mcast, enc_channel, video_loss \
FROM iris.camera c \
LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
WHERE c.name = $1";

/// SQL query for one comm config
pub const COMM_CONFIG: &str = "\
SELECT name, description, protocol, modem, poll_period_sec, \
       long_poll_period_sec, timeout_ms, idle_disconnect_sec, \
       no_response_disconnect_sec \
FROM iris.comm_config \
WHERE name = $1";

/// SQL query for one comm link
pub const COMM_LINK: &str = "\
SELECT name, description, uri, comm_config, poll_enabled, connected \
FROM iris.comm_link \
WHERE name = $1";

/// SQL query for one controller
pub const CONTROLLER: &str = "\
SELECT c.name, location, geo_loc, comm_link, drop_id, cabinet_style, \
       condition, notes, password, version, fail_time \
FROM iris.controller c \
LEFT JOIN geo_loc_view gl ON c.geo_loc = gl.name \
WHERE c.name = $1";

/// SQL query for all IO on one controller
pub const CONTROLLER_IO: &str = "\
SELECT pin, name, resource_n \
FROM iris.controller_io \
WHERE controller = $1 \
ORDER BY pin";

/// SQL query for one detector
pub const DETECTOR: &str = "\
SELECT d.name, label, r_node, controller, pin, notes, lane_code, lane_number, \
       abandoned, fake, field_length, force_fail, auto_fail \
FROM iris.detector d \
LEFT JOIN detector_label_view dl ON d.name = dl.det_id \
WHERE d.name = $1";

/// SQL query for one geo location
pub const GEO_LOC: &str = "\
SELECT name, resource_n, roadway, road_dir, cross_street, cross_dir, \
       cross_mod, landmark, lat, lon \
FROM iris.geo_loc \
WHERE name = $1";

/// SQL query for one lane marking
pub const LANE_MARKING: &str = "\
SELECT m.name, location, geo_loc, controller, pin, notes, deployed \
FROM iris.lane_marking m \
LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
WHERE m.name = $1";

/// SQL query for one modem
pub const MODEM: &str = "\
SELECT name, uri, config, enabled, timeout_ms FROM iris.modem \
WHERE name = $1";

/// SQL query for one ramp meter
pub const RAMP_METER: &str = "\
SELECT m.name, location, geo_loc, controller, pin, notes, meter_type, beacon, \
       preset, storage, max_wait, algorithm, am_target, pm_target, m_lock \
FROM iris.ramp_meter m \
LEFT JOIN geo_loc_view gl ON m.geo_loc = gl.name \
WHERE m.name = $1";

/// SQL query for one role
pub const ROLE: &str = "\
SELECT name, enabled FROM iris.role WHERE name = $1";

/// SQL query for one tag reader
pub const TAG_READER: &str = "\
SELECT t.name, location, geo_loc, controller, pin, notes, toll_zone \
FROM iris.tag_reader t \
LEFT JOIN geo_loc_view gl ON t.geo_loc = gl.name \
WHERE t.name = $1";

/// SQL query for one user
pub const USER: &str = "\
SELECT name, full_name, role, enabled \
FROM iris.i_user \
WHERE name = $1";

/// SQL query for one video monitor
pub const VIDEO_MONITOR: &str = "\
SELECT name, mon_num, controller, pin, notes, restricted, monitor_style, \
       camera \
FROM iris.video_monitor \
WHERE name = $1";

/// SQL query for one weather sensor
pub const WEATHER_SENSOR: &str = "\
SELECT ws.name, location, geo_loc, controller, pin, site_id, alt_id, notes, \
       settings, sample, sample_time \
FROM iris.weather_sensor ws \
LEFT JOIN geo_loc_view gl ON ws.geo_loc = gl.name \
WHERE ws.name = $1";