/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.SwingRunner;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The IncidentDispatcher is a GUI component for creating incidents.
 *
 * @author Douglas Lau
 */
public class IncidentDispatcher extends JPanel
	implements ProxyListener<Incident>, ProxySelectionListener<Incident>
{
	/** Font for "L" and "R" labels */
	static protected final Font FONT = new Font(null, Font.BOLD, 24);

	/** Formatter for incident names */
	static protected final SimpleDateFormat NAME_FMT =
		new SimpleDateFormat("yyyyMMddHHmmssSSS");

	/** Card layout name for camera combo box */
	static protected final String CAMERA_CBOX = "camera_cbox";

	/** Card layout name for camera button */
	static protected final String CAMERA_BTN = "camera_btn";

	/** User session */
	protected final Session session;

	/** Incident manager */
	protected final IncidentManager manager;

	/** Incident creator */
	protected final IncidentCreator creator;

	/** Cache of incident proxy objects */
	protected final TypeCache<Incident> cache;

	/** Selection model */
	protected final ProxySelectionModel<Incident> selectionModel;

	/** Type label */
	protected final JLabel type_lbl = new JLabel();

	/** Incident detail proxy list model */
	protected final ProxyListModel<IncidentDetail> dtl_model;

	/** Incident detail combo box */
	protected final JComboBox detail_cbx = new JComboBox();

	/** Location of incident */
	protected final JTextField location_txt = FormPanel.createTextField();

	/** Card layout for camera widgets */
	protected final CardLayout cam_cards = new CardLayout();

	/** Camera panel */
	protected final JPanel cam_panel = new JPanel(cam_cards);

	/** Verify camera combo box */
	protected final JComboBox camera_cbx = new JComboBox();

	/** Verify camera button */
	protected final JButton camera_btn = new JButton();

	/** Impact panel */
	protected final ImpactPanel impact_pnl = new ImpactPanel();

	/** Button to log an incident change */
	private final IButton log_btn = new IButton("incident.log");

	/** Button to deploy devices */
	private final IButton deploy_btn = new IButton("incident.deploy");

	/** Button to clear an incident */
	private final JCheckBox clear_btn = new JCheckBox(I18N.get(
		"incident.clear"));

	/** Button to edit incident */
	private final IButton edit_btn = new IButton("incident.edit");

	/** Currently watching incident */
	protected Incident watching;

	/** Create a new incident dispatcher */
	public IncidentDispatcher(Session s, IncidentManager man,
		IncidentCreator ic)
	{
		super(new BorderLayout());
		session = s;
		manager = man;
		creator = ic;
		SonarState st = session.getSonarState();
		cache = st.getIncidents();
		dtl_model = new ProxyListModel<IncidentDetail>(
			st.getIncidentDetails());
		dtl_model.initialize();
		detail_cbx.setRenderer(new IncidentDetailRenderer());
		detail_cbx.setModel(new WrapperComboBoxModel(dtl_model, true,
			true));
		selectionModel = manager.getSelectionModel();
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
		add(createMainPanel());
		clearSelected();
		createButtonJobs();
	}

	/** Create the main panel */
	protected JPanel createMainPanel() {
		type_lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		cam_panel.add(camera_cbx, CAMERA_CBOX);
		cam_panel.add(camera_btn, CAMERA_BTN);
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		FormPanel panel = new FormPanel(true);
		panel.setBorder(BorderFactory.createTitledBorder(
			I18N.get("incident.selected")));
		panel.addRow(I18N.get("incident.type"), type_lbl);
		panel.addRow(I18N.get("incident.detail"), detail_cbx);
		panel.addRow(I18N.get("location"), location_txt);
		panel.addRow(I18N.get("camera"), cam_panel);
		panel.addRow(buildImpactBox());
		JPanel btns = new JPanel(new FlowLayout());
		btns.add(log_btn);
		btns.add(deploy_btn);
		btns.add(clear_btn);
		btns.add(edit_btn);
		panel.addRow(btns);
		return panel;
	}

	/** Build the impact box */
	protected Box buildImpactBox() {
		impact_pnl.setBorder(BorderFactory.createTitledBorder(
			I18N.get("incident.impact")));
		Box box = Box.createHorizontalBox();
		box.add(createLabel("location.left"));
		box.add(Box.createHorizontalStrut(4));
		box.add(impact_pnl);
		box.add(Box.createHorizontalStrut(4));
		box.add(createLabel("location.right"));
		return box;
	}

	/** Create an "L" or "R" label */
	protected JLabel createLabel(String t) {
		JLabel label = new ILabel(t);
		label.setFont(FONT);
		return label;
	}

	/** Create jobs for button press events */
	protected void createButtonJobs() {
		impact_pnl.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				Incident inc = watching;
				if(inc != null)
					enableWidgets(inc);
			}
		});
		new ActionJob(log_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc instanceof ClientIncident)
					create((ClientIncident)inc);
				else if(inc != null)
					logUpdate(inc);
			}
		};
		new ActionJob(deploy_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc != null &&
				   !(inc instanceof ClientIncident))
					showDeployForm(inc);
			}
		};
		new ActionJob(clear_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc != null &&
				   !(inc instanceof ClientIncident))
					inc.setCleared(clear_btn.isSelected());
			}
		};
		new ActionJob(edit_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc != null)
					editIncident(inc);
			}
		};
	}

	/** Edit (replace) an existing incident */
	private void editIncident(Incident inc) {
		ClientIncident ci = new ClientIncident(inc.getName(),
			inc.getEventType(), inc.getDetail(), inc.getLaneType(),
			inc.getRoad(), inc.getDir(), inc.getEasting(),
			inc.getNorthing(), inc.getImpact());
		selectionModel.setSelected(ci);
		creator.replaceIncident(inc);
	}

	/** Create a new incident */
	protected void create(ClientIncident inc) {
		String name = createUniqueIncidentName();
		if(canAdd(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			Incident rpl = getReplaces(inc);
			if(rpl != null) {
				if(!needsReplacing(inc, rpl)) {
					logUpdate(rpl);
					return;
				}
				attrs.put("replaces", getOriginalReplaces(rpl));
				destroyIncident(rpl);
			}
			attrs.put("event_desc_id", inc.getEventType());
			IncidentDetail dtl = getSelectedDetail();
			if(dtl != null)
				attrs.put("detail", dtl);
			attrs.put("lane_type", inc.getLaneType());
			attrs.put("road", inc.getRoad());
			attrs.put("dir", inc.getDir());
			attrs.put("easting", inc.getEasting());
			attrs.put("northing", inc.getNorthing());
			attrs.put("camera", getSelectedCamera());
			attrs.put("impact", impact_pnl.getImpact());
			attrs.put("cleared", false);
			cache.createObject(name, attrs);
			Incident proxy = getProxy(name);
			if(proxy != null)
				selectionModel.setSelected(proxy);
			else
				selectionModel.clearSelection();
		}
	}

	/** Get the incident that is being replaced */
	private Incident getReplaces(ClientIncident inc) {
		String rpl = inc.getReplaces();
		if(rpl != null)
			return cache.lookupObject(rpl);
		else
			return null;
	}

	/** Test if an incident needs to be replaced */
	private boolean needsReplacing(ClientIncident inc, Incident rpl) {
		return getSelectedDetail() != rpl.getDetail() ||
		       inc.getRoad() != rpl.getRoad() ||
		       inc.getDir() != rpl.getDir() ||
		       inc.getEasting() != rpl.getEasting() ||
		       inc.getNorthing() != rpl.getNorthing() ||
		       getSelectedCamera() != rpl.getCamera();
	}

	/** Get name of original incident this replaces */
	private static String getOriginalReplaces(Incident inc) {
		String rpl = inc.getReplaces();
		if(rpl != null && rpl.length() > 0)
			return rpl;
		else
			return inc.getName();
	}

	/** Destroy the named incident */
	private void destroyIncident(Incident inc) {
		inc.setCleared(true);
		inc.destroy();
	}

	/** Get the selected incident detail */
	protected IncidentDetail getSelectedDetail() {
		Object detail = detail_cbx.getSelectedItem();
		if(detail instanceof IncidentDetail)
			return (IncidentDetail)detail;
		else
			return null;
	}

	/** Get the selected camera */
	protected Object getSelectedCamera() {
		Object cam = camera_cbx.getSelectedItem();
		if(cam != null)
			return cam;
		else
			return "";
	}

	/** Update an incident */
	protected void logUpdate(Incident inc) {
		inc.setImpact(impact_pnl.getImpact());
	}

	/** Show the incident deploy form */
	protected void showDeployForm(Incident inc) {
		session.getDesktop().show(new IncidentDeployForm(session, inc,
			manager));
	}

	/** Create a unique incident name */
	protected String createUniqueIncidentName() {
		String name = NAME_FMT.format(System.currentTimeMillis());
		return name.substring(0, 16);
	}

	/** Get the incident proxy object */
	protected Incident getProxy(String name) {
		// wait for up to 20 seconds for proxy to be created
		for(int i = 0; i < 200; i++) {
			Incident inc = IncidentHelper.lookup(name);
			if(inc != null)
				return inc;
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				// Ignore
			}
		}
		return null;
	}

	/** A new proxy has been added */
	public void proxyAdded(Incident proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(Incident proxy) {
		// Note: the IncidentManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	public void proxyChanged(Incident proxy, String a) {
		if(proxy == getSingleSelection())
			updateAttribute(proxy, a);
	}

	/** Update an attribute for the given proxy.
	 * FIXME: this should be in ProxyDispatcher base class */
	protected void updateAttribute(final Incident proxy, final String a) {
		SwingRunner.invoke(new Runnable() {
			public void run() {
				doUpdateAttribute(proxy, a);
			}
		});
	}

	/** Get the selected incident (if only one is selected) */
	protected Incident getSingleSelection() {
		if(selectionModel.getSelectedCount() == 1) {
			for(Incident inc: selectionModel.getSelected())
				return inc;
		}
		return null;
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		if(watching != null) {
			cache.ignoreObject(watching);
			watching = null;
		}
		clearSelected();
		removeAll();
	}

	/** Called whenever an object is added to the selection */
	public void selectionAdded(Incident s) {
		updateSelected();
	}

	/** Called whenever an object is removed from the selection */
	public void selectionRemoved(Incident inc) {
		updateSelected();
		if(inc instanceof ClientIncident)
			manager.removeIncident(inc.getName());
	}

	/** Update the selected object(s) */
	protected void updateSelected() {
		Incident inc = getSingleSelection();
		if(inc != null)
			setSelected(inc);
		else
			clearSelected();
	}

	/** Clear the selection */
	protected void clearSelected() {
		disableWidgets();
	}

	/** Disable the dispatcher widgets */
	protected void disableWidgets() {
		clearEventType();
		detail_cbx.setSelectedItem(null);
		detail_cbx.setEnabled(false);
		location_txt.setText("");
		camera_cbx.setSelectedItem(null);
		camera_cbx.setEnabled(false);
		setCameraAction(null);
		cam_cards.show(cam_panel, CAMERA_BTN);
		log_btn.setEnabled(false);
		deploy_btn.setEnabled(false);
		clear_btn.setEnabled(false);
		clear_btn.setSelected(false);
		edit_btn.setEnabled(false);
		impact_pnl.setImpact("");
	}

	/** Set a single selected incident */
	protected void setSelected(Incident inc) {
		if(watching != null)
			cache.ignoreObject(watching);
		impact_pnl.setImpact(inc.getImpact());
		if(inc instanceof ClientIncident)
			watching = null;
		else {
			watching = inc;
			cache.watchObject(watching);
		}
		updateAttribute(inc, null);
		enableWidgets(inc);
	}

	/** Enable the dispatcher widgets */
	protected void enableWidgets(Incident inc) {
		if(inc instanceof ClientIncident) {
			boolean create = canAdd("oname");
			detail_cbx.setEnabled(create);
			camera_cbx.setEnabled(create);
			cam_cards.show(cam_panel, CAMERA_CBOX);
			log_btn.setEnabled(create);
			deploy_btn.setEnabled(false);
			clear_btn.setEnabled(false);
			edit_btn.setEnabled(false);
		} else {
			boolean update = canUpdate(inc);
			detail_cbx.setEnabled(false);
			camera_cbx.setEnabled(false);
			cam_cards.show(cam_panel, CAMERA_BTN);
			log_btn.setEnabled(update && isImpactChanged(inc));
			deploy_btn.setEnabled(update && canDeploy(inc) &&
				!inc.getCleared());
			clear_btn.setEnabled(update);
			edit_btn.setEnabled(update && !inc.getCleared());
		}
	}

	/** Check if the impact has changed */
	protected boolean isImpactChanged(Incident inc) {
		String imp = impact_pnl.getImpact();
		return !imp.equals(inc.getImpact());
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(Incident inc, String a) {
		if(a == null) {
			detail_cbx.setSelectedItem(inc.getDetail());
			type_lbl.setText(manager.getTypeDesc(inc));
			type_lbl.setIcon(manager.getIcon(inc));
			location_txt.setText(
				manager.getGeoLoc(inc).getDescription());
			if(inc instanceof ClientIncident)
				camera_cbx.setModel(createCameraModel(inc));
			setCameraAction(inc);
		}
		if(a == null || a.equals("impact"))
			impact_pnl.setImpact(inc.getImpact());
		if(a == null || a.equals("cleared"))
			clear_btn.setSelected(inc.getCleared());
		if(a != null && (a.equals("impact") || a.equals("cleared")))
			enableWidgets(inc);
	}

	/** Set the camera action */
	protected void setCameraAction(Incident inc) {
		Camera cam = IncidentHelper.getCamera(inc);
		if(cam != null) {
			camera_btn.setAction(new CameraSelectAction(cam,
			    session.getCameraManager().getSelectionModel()));
		} else
			camera_btn.setAction(null);
		camera_btn.setEnabled(cam != null);
	}

	/** Clear the event type */
	protected void clearEventType() {
		type_lbl.setText("");
		type_lbl.setIcon(manager.getIcon(null));
	}

	/** Create a combo box model for nearby cameras */
	protected DefaultComboBoxModel createCameraModel(Incident inc) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		if(inc instanceof ClientIncident) {
			CameraFinder finder = new CameraFinder(
				(ClientIncident)inc);
			CameraHelper.find(finder);
			for(Camera cam: finder.getNearest()) {
				model.addElement(cam);
				if(model.getSelectedItem() == null)
					model.setSelectedItem(cam);
			}
			model.addElement(null);
		} else {
			model.addElement(inc.getCamera());
			model.setSelectedItem(inc.getCamera());
		}
		return model;
	}

	/** Inner class to find the camera nearest to an incident */
	static protected class CameraFinder implements Checker<Camera> {
		protected final int easting;
		protected final int northing;
		protected TreeMap<Double, Camera> cameras =
			new TreeMap<Double, Camera>();
		protected CameraFinder(ClientIncident inc) {
			easting = inc.getEasting();
			northing = inc.getNorthing();
		}
		public boolean check(Camera cam) {
			GeoLoc loc = cam.getGeoLoc();
			Double d = GeoLocHelper.metersTo(loc, easting,northing);
			if(d != null) {
				cameras.put(d, cam);
				while(cameras.size() > 5)
					cameras.pollLastEntry();
			}
			return false;
		}
		protected Camera[] getNearest() {
			return (Camera [])cameras.values().toArray(
			       new Camera[0]);
		}
	}

	/** Check if the user can add the named incident */
	public boolean canAdd(String oname) {
		return session.canAdd(Incident.SONAR_TYPE, oname);
	}

	/** Check if the user can update the given incident */
	protected boolean canUpdate(Incident inc) {
		return session.canUpdate(inc);
	}

	/** Check if the user can deploy signs for an incident */
	protected boolean canDeploy(Incident inc) {
		switch(LaneType.fromOrdinal(inc.getLaneType())) {
		case MAINLINE:
			return canSendIndications();
		default:
			return false;
		}
	}

	/** Check if the user can send LCS array indications */
	protected boolean canSendIndications() {
		return session.canUpdate(LCSArray.SONAR_TYPE, "indicationsNext")
		    && session.canUpdate(LCSArray.SONAR_TYPE, "ownerNext");
	}
}
