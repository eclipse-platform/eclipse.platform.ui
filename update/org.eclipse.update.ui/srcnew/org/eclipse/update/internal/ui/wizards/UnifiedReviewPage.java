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
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.views.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class UnifiedReviewPage
	extends UnifiedBannerPage
	implements IUpdateSearchResultCollector {
	// NL keys
	private static final String KEY_TITLE =
		"MultiInstallWizard.MultiReviewPage.title";
	private static final String KEY_DESC =
		"MultiInstallWizard.MultiReviewPage.desc";
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

	private ArrayList jobs;
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
			return jobs.toArray();
		}
	}

	class JobsLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		public String getColumnText(Object obj, int column) {
			IInstallFeatureOperation job = (IInstallFeatureOperation) obj;
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
				IFeature feature = ((IInstallFeatureOperation) obj).getFeature();
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
			return !isContained((IInstallFeatureOperation) child);
		}
		private boolean isContained(IInstallFeatureOperation job) {
			VersionedIdentifier vid = job.getFeature().getVersionedIdentifier();

			for (int i = 0; i < jobs.size(); i++) {
				IInstallFeatureOperation candidate = (IInstallFeatureOperation) jobs.get(i);
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

	class FeaturePropertyDialogAction extends PropertyDialogAction {
		private IStructuredSelection selection;

		public FeaturePropertyDialogAction(
			Shell shell,
			ISelectionProvider provider) {
			super(shell, provider);
		}

		public IStructuredSelection getStructuredSelection() {
			return selection;
		}

		public void selectionChanged(IStructuredSelection selection) {
			this.selection = selection;
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
		jobs = new ArrayList();
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && searchRunner.isNewSearchNeeded()) {
			jobs.clear();
			tableViewer.refresh();
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					searchRunner.runSearch();
					performPostSearchProcessing();
				}
			});
		}
	}

	private void performPostSearchProcessing() {
		if (tableViewer != null) {
			tableViewer.refresh();
			tableViewer.getTable().layout(true);
			if (searchRunner.getSearchProvider() instanceof UnifiedModeSelectionPage) {
				selectTrueUpdates();
			}
		}
		pageChanged();
	}
	
	private void selectTrueUpdates() {
		ArrayList trueUpdates = new ArrayList();
		for (int i=0; i<jobs.size(); i++) {
			IInstallFeatureOperation job = (IInstallFeatureOperation)jobs.get(i);
			if (!UpdateUI.isPatch(job.getFeature()))
				trueUpdates.add(job);
		}
		tableViewer.setCheckedElements(trueUpdates.toArray()); 
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
		label.setText("Select the &features to install:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		createTable(client);

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

		Dialog.applyDialogFont(parent);

		return client;
	}

	private Control createTable(Composite parent) {
		tableViewer =
			CheckboxTableViewer.newCheckList(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 250;
		table.setLayoutData(gd);

		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_FEATURE));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_VERSION));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_PROVIDER));

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(80, 225, true));
		layout.addColumnData(new ColumnWeightData(30, 80));
		layout.addColumnData(new ColumnWeightData(100, 140, true));

		table.setLayout(layout);

		tableViewer.setContentProvider(new JobsContentProvider());
		tableViewer.setLabelProvider(new JobsLabelProvider());
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				tableViewer
					.getControl()
					.getDisplay()
					.asyncExec(new Runnable() {
					public void run() {
						pageChanged();
					}
				});
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
		tableViewer.setSorter(new FeatureSorter() {
			public int category(Object obj) {
				IInstallFeatureOperation job = (IInstallFeatureOperation) obj;
				if (UpdateUI.isPatch(job.getFeature()))
					return 1;
				return 0;
			}
		});
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		table.setMenu(menuMgr.createContextMenu(table));
		
		tableViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		tableViewer.setAllChecked(true);
		return table;
	}
	
	private void fillContextMenu(IMenuManager manager) {
		if (tableViewer.getSelection().isEmpty()) return;
		Action action = new Action("&Properties") {
			public void run() {
				handleProperties();
			}
		};
		manager.add(action);
	}

	public void accept(final IFeature feature) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IInstallFeatureOperation job = (IInstallFeatureOperation)OperationsManager.getOperationFactory().createInstallOperation(null, null, feature,null, null, null);
				ViewerFilter[] filters = tableViewer.getFilters();
				boolean visible = true;

				for (int i = 0; i < filters.length; i++) {
					ViewerFilter filter = filters[i];
					if (!filter.select(tableViewer, null, job)) {
						visible = false;
						break;
					}
				}
				if (visible) {
					tableViewer.add(job);
					updateItemCount(0, -1);
				}
				jobs.add(job);
			}
		});
	}

	private void jobSelected(IStructuredSelection selection) {
		IInstallFeatureOperation job = (IInstallFeatureOperation) selection.getFirstElement();
		IFeature feature = job != null ? job.getFeature() : null;
		IURLEntry descEntry = feature != null ? feature.getDescription() : null;
		String desc = null;
		if (descEntry != null)
			desc = descEntry.getAnnotation();
		if (desc == null)
			desc = "";
		descLabel.setText(desc);
		propertiesButton.setEnabled(feature != null);
		moreInfoButton.setEnabled(job != null && getMoreInfoURL(job) != null);
	}

	private void pageChanged() {
		Object[] checked = tableViewer.getCheckedElements();
		int totalCount = tableViewer.getTable().getItemCount();
		updateItemCount(checked.length, totalCount);
		if (checked.length > 0) {
			validateSelection();
		} else {
			setErrorMessage(null);
			setPageComplete(false);
			validationStatus = null;
		}
		statusButton.setEnabled(validationStatus != null);
	}

	private void updateItemCount(int checkedCount, int totalCount) {
		if (checkedCount == -1) {
			Object[] checkedElements = tableViewer.getCheckedElements();
			checkedCount = checkedElements.length;
		}
		if (totalCount == -1) {
			totalCount = tableViewer.getTable().getItemCount();
		}
		String total = "" + totalCount;
		String selected = "" + checkedCount;
		counterLabel.setText(
			UpdateUI.getFormattedMessage(
				KEY_COUNTER,
				new String[] { selected, total }));
		counterLabel.getParent().layout();
	}

	private void handleSelectAll(boolean select) {
		tableViewer.setAllChecked(select);
		tableViewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				pageChanged();
			}
		});
	}

	private void handleProperties() {
		final IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();

		final IInstallFeatureOperation job =
			(IInstallFeatureOperation) selection.getFirstElement();
		if (propertiesAction == null) {
			propertiesAction =
				new FeaturePropertyDialogAction(getShell(), tableViewer);
		}

		BusyIndicator
			.showWhile(tableViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				SimpleFeatureAdapter adapter =
					new SimpleFeatureAdapter(job.getFeature());
				propertiesAction.selectionChanged(
					new StructuredSelection(adapter));
				propertiesAction.run();
			}
		});
	}

	private String getMoreInfoURL(IInstallFeatureOperation job) {
		IURLEntry desc = job.getFeature().getDescription();
		if (desc != null) {
			URL url = desc.getURL();
			return (url == null) ? null : url.toString();
		}
		return null;
	}

	private void handleMoreInfo() {
		IStructuredSelection selection =
			(IStructuredSelection) tableViewer.getSelection();
		final IInstallFeatureOperation selectedJob =
			(IInstallFeatureOperation) selection.getFirstElement();
		BusyIndicator
			.showWhile(tableViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				Program.launch(getMoreInfoURL(selectedJob));
			}
		});
	}

	public IInstallFeatureOperation[] getSelectedJobs() {
		Object[] selected = tableViewer.getCheckedElements();
		IInstallFeatureOperation[] result = new IInstallFeatureOperation[selected.length];
		System.arraycopy(selected, 0, result, 0, selected.length);
		return result;
	}

	public void validateSelection() {
		IInstallFeatureOperation[] jobs = getSelectedJobs();
		validationStatus =
			UpdateManager.getValidator().validatePendingChanges(jobs);
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
				"Invalid combination",
				null,
				validationStatus);
		}
	}
}
