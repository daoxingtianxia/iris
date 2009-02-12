/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.toast.ZTable;

/**
 * A form for displaying and editing warning signs
 *
 * @author Douglas Lau
 */
public class WarningSignForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Warning Signs";

	/** Table model for warning signs */
	protected WarningSignModel w_model;

	/** Table to hold the warning signs */
	protected final ZTable w_table = new ZTable();

	/** Button to display the warning sign properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected warning sign */
	protected final JButton del_sign = new JButton("Delete");

	/** TMS connection */
	protected final TmsConnection connection;

	/** Warning sign type cache */
	protected final TypeCache<WarningSign> cache;

	/** Create a new warning sign form */
	public WarningSignForm(TmsConnection tc, TypeCache<WarningSign> c) {
		super(TITLE);
		connection = tc;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		w_model = new WarningSignModel(cache);
		add(createWarningSignPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		w_model.dispose();
	}

	/** Create warning sign panel */
	protected JPanel createWarningSignPanel() {
		final ListSelectionModel s = w_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectWarningSign();
			}
		};
		w_table.setModel(w_model);
		w_table.setAutoCreateColumnsFromModel(false);
		w_table.setColumnModel(w_model.createColumnModel());
		w_table.setVisibleRowCount(12);
		new ActionJob(this, properties) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					WarningSign ws = w_model.getProxy(row);
					if(ws != null)
						showPropertiesForm(ws);
				}
			}
		};
		w_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					properties.doClick();
			}
		});
		new ActionJob(this, del_sign) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					w_model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(w_table);
		panel.add(properties);
		panel.addRow(del_sign);
		properties.setEnabled(false);
		del_sign.setEnabled(false);
		return panel;
	}

	/** Change the selected warning sign */
	protected void selectWarningSign() {
		int row = w_table.getSelectedRow();
		properties.setEnabled(row >= 0 && !w_model.isLastRow(row));
		del_sign.setEnabled(row >= 0 && !w_model.isLastRow(row));
	}

	/** Show the properties form for a warning sign */
	protected void showPropertiesForm(WarningSign ws) throws Exception {
		SmartDesktop desktop = connection.getDesktop();
		desktop.show(new WarningSignProperties(connection, ws));
	}
}
