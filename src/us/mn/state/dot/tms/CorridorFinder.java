/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * Interface to lookup corridors.
 *
 * @author Douglas Lau
 */
public interface CorridorFinder<T extends R_Node> {

	/** Lookup a corridor */
	CorridorBase<T> lookupCorridor(GeoLoc loc);

	/** Lookup a linked corridor */
	CorridorBase<T> lookupLinkedCorridor(GeoLoc loc);
}
