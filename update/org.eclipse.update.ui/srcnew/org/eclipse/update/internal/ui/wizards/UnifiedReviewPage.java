/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.views.*;

public class UnifiedReviewPage extends BannerPage2 {
	// NL keys
	private static final String KEY_TITLE =
		"MultiInstallWizard.MultiReviewPage.title";
	private static final String KEY_DESC =
		"MultiInstallWizard.MultiReviewPage.desc";
	private static final String KEY_C_TASK =
		"MultiInstallWizard.MultiReviewPage.c.task";
	private static final String KEY_C_FEATURE =
		"MultiInstallWizard.MultiReviewPage.c.feature";
	private static final String KEY_C_VERSION =
		"MultiInstallWizard.MultiReviewPage.c.version";
	private static final String KEY_C_PROVIDER =
		"MultiInstallWizard.MultiReviewPage.c.provider";
	private static final String KEY_COUNTER =
		"MultiInstallWizard.MultiReviewPage.counter";
	private static final String KEY_FILTER_CHECK =
		"MultiInstallWizard.MultiReviewPage.filterCheck";

	private PendingOperationAdapter[] jobs;
	private Label counterLabel;
	private CheckboxTableViewer tableViewer;
	private IStatus validationStatus;
	private PropertyDialogAction propertiesAction;
	private Text descLabel;
	private Button statusButton;
	private Button moreInfoButton;
	private Button propertiesButton;
	private Button filterCheck;
	private ContainmentFilter filter = new ContainmentFilter();
	private SearchRunner2 searchRunner;
	class JobsContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return jobs != null ? jobs : new Object[0];
		}
	}

	class JobsLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		public String getColumnText(Object obj, int column) {
			PendingOperationAdapter job = (PendingOperationAdapter) obj;
			IFeature feature = job.getFeature();

			switch (column) {
				case 0 :
					return feature.getLabel();
				case 1 :
					return feature
						.getVersionedIdentifier()
						.getVersion()
						.toString();
				case 2 :
					return feature.getProvider();
			}
			return "";
		}
		public Image getColumnImage(Object obj, int column) {
			if (column == 0) {
				PendingOperationAdapter job = (PendingOperationAdapter) obj;
				IFeature feature = job.getFeature();
				boolean patch = feature.isPatch();
				UpdateLabelProvider provider =
					UpdateUI.getDefault().getLabelProvider();
				if (patch)
					return provider.get(UpdateUIImages.DESC_EFIX_OBJ);
				else
					return provider.get(UpdateUIImages.DESC_FEATURE_OBJ);
			}
			return null;
		}
	}

	class ContainmentFilter extends ViewerFilter {
		public boolean select(Viewer v, Object parent, Object child) {
			return !isContained((PendingOperationAdapter) child);
		}
		private boolean isContained(PendingOperationAdapter job) {
			if (job.getJob().getJobType() != PendingOperation.INSTALL)
				return false;
			VersionedIdentifier vid = job.getFeature().getVersionedIdentifier();
			Object[] selected = tableViewer.getCheckedElements();
			for (int i = 0; i < selected.length; i++) {
				PendingOperationAdapter candidate = (PendingOperationAdapter) selected[i];
				if (candidate.equals(job))
					continue;
				IFeature feature = candidate.getFeature();
				if (includes(feature, vid))
					return true;
			}
			return false;
		}
		private boolean includes(IFeature feature, VersionedIdentifier vid) {
			try {
				IFeatureReference[] irefs =
					feature.getIncludedFeatureReferences();
				for (int i = 0; i < irefs.length; i++) {
					IFeatureReference iref = irefs[i];
					IFeature ifeature = iref.getFeature(null);
					VersionedIdentifier ivid =
						ifeature.getVersionedIdentifier();
					if (ivid.equals(vid))
						return true;
					if (includes(ifeature, vid))
						return true;
				}
			} catch (CoreException e) {
			}
			return false;
		}
	}

	/**
	 * Constructor for ReviewPage2
	 */
	public UnifiedReviewPage(SearchRunner2 searchRunner) {
		super("UnifiedMultiReview");
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
		UpdateUI.getDefault().getLabelProvider().connect(this);
		this.searchRunner = searchRunner;
		setBannerVisible(false);
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && searchRunner.isNewSearchNeeded()) {
			setJobs(searchRunner.runSearch());
		}
	}

	private void setJobs(PendingOperation[] jobs) {
		this.jobs = new PendingOperationAdapter[jobs.length];
		for (int i=0; i<jobs.length; i++)
			this.jobs[i] = new PendingOperationAdapter(jobs[i]);
		if (tableViewer != null) {
			tableViewer.refresh();
			tableViewer.getTable().layout(true);
		}
		pageChanged();
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		client.setLayout(layout);
		Label label = new Label(client, SWT.NULL);
		label.setText("&You are about to execute the following tasks:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		Control table = createTable(client);
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		Composite buttonContainer = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonContainer.setLayout(layout);

		Button button = new Button(buttonContainer, SWT.PUSH);
		button.setText("&Select All");
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSelectAll(true);
			}
		});

		button = new Button(buttonContainer, SWT.PUSH);
		button.setText("&Deselect All");
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSelectAll(false);
			}
		});

		moreInfoButton = new Button(buttonContainer, SWT.PUSH);
		moreInfoButton.setText("&More Info");
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		moreInfoButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(moreInfoButton);
		moreInfoButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMoreInfo();
			}
		});
		moreInfoButton.setEnabled(false);
		
		propertiesButton = new Button(buttonContainer, SWT.PUSH);
		propertiesButton.setText("&Properties");
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		propertiesButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(propertiesButton);
		propertiesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleProperties();
			}
		});
		propertiesButton.setEnabled(false);

		statusButton = new Button(buttonContainer, SWT.PUSH);
		statusButton.setText("&Show Status...");
		gd =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		statusButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(statusButton);
		statusButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showStatus();
			}
		});

		descLabel = new Text(client, SWT.WRAP);
		descLabel.setEditable(false);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = 48;
		descLabel.setLayoutData(gd);

		counterLabel = new Label(client, SWT.NULL);
		gd = new GridData();
		gd.horizontalSpan = 2;
		counterLabel.setLayoutData(gd);

		filterCheck = new Button(client, SWT.CHECK);
		filterCheck.setText(UpdateUI.getString(KEY_FILTER_CHECK));
		filterCheck.setSelection(true);
		tableViewer.addFilter(filter);
		filterCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (filterCheck.getSelection())
					tableViewer.addFilter(filter);
				else
					tableViewer.removeFilter(filter);
				pageChanged();
			}
		});
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		filterCheck.setLayoutData(gd);
		pageChanged();

		WorkbenchHelp.setHelp(client, "org.eclipse.update.ui.MultiReviewPage2");
		return client;
	}

	private Control createTable(Composite parent) {
		tableViewer =
			CheckboxTableViewer.newCheckList(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_FEATURE));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_VERSION));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_PROVIDER));

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(80, true));
		layout.addColumnData(new ColumnWeightData(30));
		layout.addColumnData(new ColumnWeightData(100, true));

		table.setLayout(layout);

		tableViewer.setContentProvider(new JobsContentProvider());
		tableViewer.setLabelProvider(new JobsLabelProvider());
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				pageChanged();
			}
		});
		tableViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				jobSelected((IStructuredSelection) e.getSelection());
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleProperties();
			}
		});
		tableViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		tableViewer.setAllChecked(true);
		return table;
	}


	private void jobSelected(IStructuredSelection selection) {
		PendingOperationAdapter job = (PendingOperationAdapter) selection.getFirstElement();
		IFeature feature = job != null ? job.getFeature() : null;
		IURLEntry descEntry = feature != null ? feature.getDescription() : null;
		String desc = null;
		if (descEntry != null)
			desc = descEntry.getAnnotation();
		if (desc == null)
			desc = "";
		descLabel.setText(desc);
		propertiesButton.setEnabled(feature != null);
		moreInfoButton.setEnabled(job!=null && getMoreInfoURL(job)!=null);
	}

	private void pageChanged() {
		Object[] checked = tableViewer.getCheckedElements();
		int totalCount = tableViewer.getTable().getItemCount();
		String total = "" + totalCount;
		String selected = "" + checked.length;
		counterLabel.setText(
			UpdateUI.getFormattedMessage(
				KEY_COUNTER,
				new String[] { selected, total }));
		counterLabel.getParent().layout();
		if (checked.length > 0) {
			validateSelection();
		} else {
			setErrorMessage(null);
			setPageComplete(false);
			validationStatus = null;
		}
		statusButton.setEnabled(validationStatus != null);
	}

	private void handleSelectAll(boolean select) {
		tableViewer.setAllChecked(select);
		pageChanged();
	}

	private void handleProperties() {
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		final PendingOperationAdapter selectedJob =
			(PendingOperationAdapter) selection.getFirstElement();
		if (selectedJob == null)
			return;

		if (propertiesAction==null) {
			propertiesAction =
				new PropertyDialogAction(
					getShell(),
					tableViewer);
		}
		
		BusyIndicator
			.showWhile(tableViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				propertiesAction.selectionChanged(new StructuredSelection(selectedJob));
				propertiesAction.run();
			}
		});
	}

	private String getMoreInfoURL(PendingOperationAdapter job) { 
		IFeature feature = job.getFeature();
		IURLEntry desc = feature.getDescription();
		if (desc==null) return null;
		URL url = desc.getURL();
		if (url==null) return null;
		return url.toString();
	}
	
	private void handleMoreInfo() {
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		final PendingOperationAdapter selectedJob =
			(PendingOperationAdapter) selection.getFirstElement();
		if (selectedJob == null)
			return;
			
		final String moreInfoURL = getMoreInfoURL(selectedJob);
		if (moreInfoURL==null) return;

		BusyIndicator
			.showWhile(tableViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				DetailsView.showURL(moreInfoURL, false);
			}
		});
	}

	public PendingOperation[] getSelectedJobs() {
		Object[] selected = tableViewer.getCheckedElements();
		PendingOperation[] jobs = new PendingOperation[selected.length];
		for (int i=0; i<jobs.length; i++)
			jobs[i] = ((PendingOperationAdapter)selected[i]).getJob();
		// TODO we should in the future have the pending operation adapter implement some pending operation interface and
		// use the arraycopy for better performance
		//System.arraycopy(selected, 0, jobs, 0, selected.length);
		return jobs;
	}

	public void validateSelection() {
		PendingOperation[] jobs = getSelectedJobs();
		validationStatus = UpdateManager.getValidator().validatePendingChanges(jobs);
		setPageComplete(validationStatus == null);
		String errorMessage = null;

		if (validationStatus != null) {
			errorMessage =
				"Invalid combination - select \"Show Status...\" for details.";
		}
		setErrorMessage(errorMessage);
	}

	private void showStatus() {
		if (validationStatus != null) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				validationStatus);
		}
	}
}
