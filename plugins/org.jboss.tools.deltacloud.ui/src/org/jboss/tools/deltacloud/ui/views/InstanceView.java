/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.deltacloud.ui.views;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IEvaluationService;
import org.jboss.tools.deltacloud.core.DeltaCloud;
import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
import org.jboss.tools.deltacloud.core.DeltaCloudManager;
import org.jboss.tools.deltacloud.core.ICloudManagerListener;
import org.jboss.tools.deltacloud.core.IInstanceFilter;
import org.jboss.tools.deltacloud.core.IInstanceListListener;
import org.jboss.tools.deltacloud.ui.Activator;
import org.jboss.tools.deltacloud.ui.IDeltaCloudPreferenceConstants;
import org.jboss.tools.internal.deltacloud.ui.utils.UIUtils;
import org.osgi.service.prefs.Preferences;

public class InstanceView extends ViewPart implements ICloudManagerListener, IInstanceListListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.jboss.tools.deltacloud.ui.views.InstanceView";

	private final static String CLOUD_SELECTOR_LABEL = "CloudSelector.label"; //$NON-NLS-1$

	private static final String FILTERED_LABEL = "Filtered.label"; //$NON-NLS-1$
	private static final String FILTERED_TOOLTIP = "FilteredImages.tooltip"; //$NON-NLS-1$	

	private TableViewer viewer;
	private Composite container;
	private Combo cloudSelector;
	private Label filterLabel;

	private DeltaCloud[] clouds;
	private DeltaCloud currCloud;

	private InstanceViewLabelAndContentProvider contentProvider;

	private InstanceView parentView;

	public InstanceView() {
		parentView = this;
	}

	private ModifyListener cloudModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = cloudSelector.getSelectionIndex();
			if (currCloud != null) {
				currCloud.removeInstanceListListener(parentView);
			}
			currCloud = clouds[index];
			storeSelectedCloud();
			Display.getCurrent().asyncExec(new Runnable() {

				@Override
				public void run() {
					viewer.setInput(currCloud);
					currCloud.addInstanceListListener(parentView);
					viewer.refresh();
				}

			});
		}

		private void storeSelectedCloud() {
			Preferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
			try {
				prefs.put(IDeltaCloudPreferenceConstants.LAST_CLOUD_INSTANCE_VIEW, currCloud.getName());
			} catch (Exception exc) {
				// do nothing
			}
		}
	};

	private class ColumnListener extends SelectionAdapter {

		private int column;

		public ColumnListener(int column) {
			this.column = column;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			InstanceComparator comparator = (InstanceComparator) viewer.getComparator();
			Table t = viewer.getTable();
			if (comparator.getColumn() == column) {
				comparator.reverseDirection();
			}
			comparator.setColumn(column);
			TableColumn tc = (TableColumn) e.getSource();
			t.setSortColumn(tc);
			t.setSortDirection(SWT.NONE);
			viewer.refresh();
		}

	};

	@Override
	public void dispose() {
		for (DeltaCloud cloud : clouds) {
			cloud.removeInstanceListListener(this);
		}
		DeltaCloudManager.getDefault().removeCloudManagerListener(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		Label cloudSelectorLabel = createCloudSelector();

		filterLabel = new Label(container, SWT.NULL);
		filterLabel.setText(CVMessages.getString(FILTERED_LABEL));
		filterLabel.setToolTipText(CVMessages.getString(FILTERED_TOOLTIP));

		Composite tableArea = new Composite(container, SWT.NULL);
		viewer = createTableViewer(tableArea);

		if (currCloud != null) {
			currCloud.removeInstanceListListener(parentView);
			viewer.setInput(currCloud);
			currCloud.addInstanceListListener(parentView);
			IInstanceFilter filter = currCloud.getInstanceFilter();
			filterLabel.setVisible(!filter.toString().equals(IInstanceFilter.ALL_STRING));
		}

		Point p1 = cloudSelectorLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = cloudSelector.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0, 5 + centering);
		f.left = new FormAttachment(0, 30);
		cloudSelectorLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(0, 5);
		f.left = new FormAttachment(cloudSelectorLabel, 5);
		cloudSelector.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(0, 5 + centering);
		f.right = new FormAttachment(100, -10);
		filterLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(cloudSelector, 8);
		f.left = new FormAttachment(0, 0);
		f.right = new FormAttachment(100, 0);
		f.bottom = new FormAttachment(100, 0);
		tableArea.setLayoutData(f);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.jboss.tools.deltacloud.ui.viewer");
		hookContextMenu(viewer.getTable());
		getSite().setSelectionProvider(viewer);

		DeltaCloudManager.getDefault().addCloudManagerListener(this);
	}

	private TableViewer createTableViewer(Composite tableArea) {
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableArea.setLayout(tableLayout);

		TableViewer viewer = new TableViewer(tableArea, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
				| SWT.MULTI);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		contentProvider = new InstanceViewLabelAndContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(contentProvider);
		InstanceComparator comparator = new InstanceComparator(0);
		viewer.setComparator(comparator);
		createColumns(tableLayout, table);
		table.setSortDirection(SWT.NONE);
		return viewer;

	}

	private void createColumns(TableColumnLayout tableLayout, Table table) {
		for (int i = 0; i < InstanceViewLabelAndContentProvider.Column.getSize(); ++i) {
			InstanceViewLabelAndContentProvider.Column c =
					InstanceViewLabelAndContentProvider.Column.getColumn(i);
			TableColumn tc = new TableColumn(table, SWT.NONE);
			if (i == 0) {
				table.setSortColumn(tc);
			}
			tc.setText(CVMessages.getString(c.name()));
			tableLayout.setColumnData(tc, new ColumnWeightData(c.getWeight(), true));
			tc.addSelectionListener(new ColumnListener(i));
		}
	}

	private Label createCloudSelector() {
		Label cloudSelectorLabel = new Label(container, SWT.NULL);
		cloudSelectorLabel.setText(CVMessages.getString(CLOUD_SELECTOR_LABEL));

		cloudSelector = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		initializeCloudSelector();
		cloudSelector.addModifyListener(cloudModifyListener);
		// Following is a kludge so that on Linux the Combo is read-only but
		// has a white background.
		cloudSelector.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = false;
			}
		});
		return cloudSelectorLabel;
	}

	private void hookContextMenu(Control control) {
		IMenuManager contextMenu = UIUtils.createContextMenu(control);
		UIUtils.registerContributionManager(UIUtils.getContextMenuId(ID), contextMenu, control);
	}

	private void initializeCloudSelector() {
		int defaultIndex = 0;
		clouds = DeltaCloudManager.getDefault().getClouds();
		String[] cloudNames = new String[clouds.length];
		// If we have saved the last cloud used from a previous session,
		// default to using that cloud to start unless it no longer exists
		Preferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		String lastCloudUsed = prefs.get(IDeltaCloudPreferenceConstants.LAST_CLOUD_INSTANCE_VIEW, "");
		for (int i = 0; i < clouds.length; ++i) {
			cloudNames[i] = clouds[i].getName();
			if (cloudNames[i].equals(lastCloudUsed))
				defaultIndex = i;
		}
		cloudSelector.setItems(cloudNames);
		if (clouds.length > 0) {
			cloudSelector.setText(cloudNames[defaultIndex]);
			currCloud = clouds[defaultIndex];
		}
	}

	public void changeEvent(int type) {
		String currName = null;
		int currIndex = 0;
		if (currCloud != null) {
			currName = currCloud.getName();
			currIndex = cloudSelector.getSelectionIndex();
		}
		clouds = DeltaCloudManager.getDefault().getClouds();
		String[] cloudNames = new String[clouds.length];
		int index = 0;
		for (int i = 0; i < clouds.length; ++i) {
			cloudNames[i] = clouds[i].getName();
			if (cloudNames[i].equals(currName))
				index = i;
		}
		if (type == ICloudManagerListener.RENAME_EVENT) {
			index = currIndex; // no change in cloud displayed
		}
		cloudSelector.removeModifyListener(cloudModifyListener);
		cloudSelector.setItems(cloudNames);
		if (cloudNames.length > 0) {
			cloudSelector.setText(cloudNames[index]);
			currCloud = clouds[index];
			viewer.setInput(currCloud);
		} else {
			currCloud = null;
			cloudSelector.setText("");
			viewer.setInput(new DeltaCloudInstance[0]);
		}
		cloudSelector.addModifyListener(cloudModifyListener);
	}

	public void listChanged(DeltaCloud cloud, DeltaCloudInstance[] list) {
		// Run following under Display thread since this can be
		// triggered by a non-display thread notifying listeners.
		final DeltaCloudInstance[] finalList = list;
		if (cloud.getName().equals(currCloud.getName())) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					currCloud.removeInstanceListListener(parentView);
					viewer.setInput(finalList);
					currCloud.addInstanceListListener(parentView);
					viewer.refresh();
					refreshToolbarCommandStates();
				}
			});
		}
	}

	/**
	 * Refresh the states of the commands in the toolbar.
	 */
	private void refreshToolbarCommandStates() {
		IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(
				IEvaluationService.class);
		evaluationService.requestEvaluation("org.jboss.tools.deltacloud.ui.commands.isStopped");
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
