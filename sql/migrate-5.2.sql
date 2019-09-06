\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

SELECT iris.update_version('5.1.0', '5.2.0');

-- Add incident_clear_advice system attributes
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_multi', 'JUST CLEARED');
INSERT INTO iris.system_attribute (name, value)
	VALUES ('incident_clear_advice_abbrev', 'CLEARED');

-- Drop cleared column from incident advice table/view
DELETE FROM iris.inc_advice WHERE cleared = 't';
DROP VIEW inc_advice_view;
ALTER TABLE iris.inc_advice DROP COLUMN cleared;
CREATE VIEW inc_advice_view AS
	SELECT a.name, imp.description AS impact, lt.description AS lane_type,
	       rng.description AS range, impacted_lanes, open_lanes, multi,
	       abbrev
	FROM iris.inc_advice a
	LEFT JOIN iris.inc_impact imp ON a.impact = imp.id
	LEFT JOIN iris.inc_range rng ON a.range = rng.id
	LEFT JOIN iris.lane_type lt ON a.lane_type = lt.id;
GRANT SELECT ON inc_advice_view TO PUBLIC;

COMMIT;
