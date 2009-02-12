/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.ZTable;

/**
 * A form for displaying and editing holidays
 *
 * @author Douglas Lau
 */
public class HolidayForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Holidays";

	/** Table model for holidays */
	protected HolidayModel model;

	/** Table to hold the holiday list */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected holiday */
	protected final JButton del_holiday = new JButton("Delete");

	/** Holiday type cache */
	protected final TypeCache<Holiday> cache;

	/** Create a new holiday form */
	public HolidayForm(TypeCache<Holiday> c) {
		super(TITLE);
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new HolidayModel(cache);
		add(createHolidayPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create holiday */
	protected JPanel createHolidayPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectHoliday();
			}
		};
		new ActionJob(this, del_holiday) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		table.setRowHeight(22);
		table.setVisibleRowCount(16);
		FormPanel panel = new FormPanel(true);
		panel.addRow(table);
		panel.addRow(del_holiday);
		del_holiday.setEnabled(false);
		// FIXME: add a calendar widget (for holiday feedback)
		return panel;
	}

	/** Change the selected holiday */
	protected void selectHoliday() {
		int row = table.getSelectedRow();
		del_holiday.setEnabled(row >= 0 && !model.isLastRow(row));
	}
}
