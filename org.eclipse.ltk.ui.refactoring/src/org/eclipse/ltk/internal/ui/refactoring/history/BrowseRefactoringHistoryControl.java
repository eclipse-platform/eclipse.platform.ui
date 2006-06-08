/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryContentProvider;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryLabelProvider;

/**
 * Control which is capable of browsing elements of a refactoring history. The
 * refactoring history can be sorted by project or by timestamps.
 * 
 * @since 3.2
 */
public class BrowseRefactoringHistoryControl extends RefactoringHistoryControl {

	/** The empty descriptors constant */
	private static final RefactoringDescriptorProxy[] EMPTY_DESCRIPTORS= {};

	/** The toolbar sort group */
	private static final String TOOLBAR_SORT_GROUP= "group.sort"; //$NON-NLS-1$

	/** The deselect all button, or <code>null</code> */
	private Button fDeselectAllButton= null;

	/** The select all button, or <code>null</code> */
	private Button fSelectAllButton= null;

	/** The sort projects action */
	private final IAction fSortProjects= new Action(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_project, IAction.AS_RADIO_BUTTON) {

		public final void run() {
			final BrowseRefactoringHistoryContentProvider provider= (BrowseRefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
			provider.setSortProjects(true);
			fHistoryViewer.setSorter(fViewerSorter);
			fHistoryViewer.refresh(false);
			reconcileCheckState();
			reconcileSelectionState();
			fSortProjects.setChecked(true);
			fSortTimestamps.setChecked(false);
		}
	};

	/** The sort time stamps action */
	private final IAction fSortTimestamps= new Action(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_date, IAction.AS_RADIO_BUTTON) {

		public final void run() {
			final BrowseRefactoringHistoryContentProvider provider= (BrowseRefactoringHistoryContentProvider) fHistoryViewer.getContentProvider();
			provider.setSortProjects(false);
			fHistoryViewer.setSorter(null);
			fHistoryViewer.refresh(false);
			reconcileCheckState();
			reconcileSelectionState();
			fSortTimestamps.setChecked(true);
			fSortProjects.setChecked(false);
		}
	};

	/** The toolbar manager, or <code>null</code> */
	private ToolBarManager fToolBarManager= null;

	/** The viewer sorter */
	private final BrowseRefactoringHistoryViewerSorter fViewerSorter= new BrowseRefactoringHistoryViewerSorter();

	/**
	 * Creates a new browse refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public BrowseRefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
		super(parent, configuration);

		addDisposeListener(new DisposeListener() {

			public final void widgetDisposed(final DisposeEvent event) {
				if (fToolBarManager != null) {
					fToolBarManager.removeAll();
					fToolBarManager.dispose();
					fToolBarManager= null;
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createBottomButtonBar(final Composite parent) {
		Assert.isNotNull(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout(2, false);
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		layout.marginTop= 5;
		composite.setLayout(layout);

		final GridData data= new GridData();
		data.grabExcessHorizontalSpace= true;
		data.grabExcessVerticalSpace= false;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.TOP;
		composite.setLayoutData(data);

		createSelectAllButton(composite);
		createDeselectAllButton(composite);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl() {
		super.createControl();
		final GridData data= new GridData();
		data.grabExcessHorizontalSpace= true;
		data.heightHint= new PixelConverter(this).convertHeightInCharsToPixels(22);
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.FILL;
		setLayoutData(data);
	}

	/**
	 * Creates the deselect all button of the control.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createDeselectAllButton(final Composite parent) {
		Assert.isNotNull(parent);
		fDeselectAllButton= new Button(parent, SWT.NONE);
		fDeselectAllButton.setEnabled(false);
		fDeselectAllButton.setText(RefactoringUIMessages.SelectRefactoringHistoryControl_deselect_all_label);
		final GridData data= new GridData();
		data.horizontalAlignment= GridData.END;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeselectAllButton);
		fDeselectAllButton.setLayoutData(data);

		fDeselectAllButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleDeselectAll();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	protected TreeViewer createHistoryViewer(final Composite parent) {
		Assert.isNotNull(parent);
		TreeViewer viewer= null;
		if (fControlConfiguration.isCheckableViewer())
			viewer= new RefactoringHistoryTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		else
			viewer= new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		return viewer;
	}

	/**
	 * Creates the select all button of the control.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createSelectAllButton(final Composite parent) {
		Assert.isNotNull(parent);
		fSelectAllButton= new Button(parent, SWT.NONE);
		fSelectAllButton.setEnabled(false);
		fSelectAllButton.setText(RefactoringUIMessages.SelectRefactoringHistoryControl_select_all_label);
		final GridData data= new GridData();
		data.horizontalAlignment= GridData.END;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fSelectAllButton);
		fSelectAllButton.setLayoutData(data);

		fSelectAllButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleSelectAll();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createToolBar(final ViewForm parent) {
		final ToolBarManager manager= getToolBarManager();
		if (manager != null) {
			manager.removeAll();
			manager.add(new Separator(TOOLBAR_SORT_GROUP));
			fSortProjects.setText(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_project);
			fSortProjects.setToolTipText(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_project);
			fSortProjects.setDescription(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_project_description);
			fSortProjects.setImageDescriptor(RefactoringPluginImages.DESC_ELCL_SORT_PROJECT);
			fSortProjects.setDisabledImageDescriptor(RefactoringPluginImages.DESC_DLCL_SORT_PROJECT);
			fSortTimestamps.setText(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_date);
			fSortTimestamps.setToolTipText(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_date);
			fSortTimestamps.setDescription(RefactoringUIMessages.BrowseRefactoringHistoryControl_sort_date_description);
			fSortTimestamps.setImageDescriptor(RefactoringPluginImages.DESC_ELCL_SORT_DATE);
			fSortTimestamps.setDisabledImageDescriptor(RefactoringPluginImages.DESC_DLCL_SORT_DATE);
			manager.appendToGroup(TOOLBAR_SORT_GROUP, fSortProjects);
			manager.appendToGroup(TOOLBAR_SORT_GROUP, fSortTimestamps);
			manager.update(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected int getContainerColumns() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	protected RefactoringHistoryContentProvider getContentProvider() {
		return new BrowseRefactoringHistoryContentProvider(fControlConfiguration);
	}

	/**
	 * Returns the deselect all button.
	 * 
	 * @return the deselect all button, or <code>null</code>
	 */
	public Button getDeselectAllButton() {
		return fDeselectAllButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected RefactoringHistoryLabelProvider getLabelProvider() {
		return new BrowseRefactoringHistoryLabelProvider(fControlConfiguration);
	}

	/**
	 * Returns the select all button.
	 * 
	 * @return the select all button, or <code>null</code>
	 */
	public Button getSelectAllButton() {
		return fSelectAllButton;
	}

	/**
	 * Returns the toolbar manager of this control
	 * 
	 * @return the toolbar manager
	 */
	protected ToolBarManager getToolBarManager() {
		if (fToolBarManager == null) {
			final ToolBar toolbar= new ToolBar(fHistoryPane, SWT.FLAT);
			fHistoryPane.setTopCenter(toolbar);
			fToolBarManager= new ToolBarManager(toolbar);
		}
		return fToolBarManager;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		final RefactoringHistory history= getInput();
		if (history != null) {
			final int checked= getCheckedDescriptors().length;
			final int total= history.getDescriptors().length;
			if (fSelectAllButton != null)
				fSelectAllButton.setEnabled(checked < total);
			if (fDeselectAllButton != null)
				fDeselectAllButton.setEnabled(checked > 0);
		}
	}

	/**
	 * Handles the deselect all event.
	 */
	protected void handleDeselectAll() {
		setCheckedDescriptors(EMPTY_DESCRIPTORS);
	}

	/**
	 * Handles the select all event.
	 */
	protected void handleSelectAll() {
		final RefactoringHistory history= getInput();
		if (history != null)
			setCheckedDescriptors(history.getDescriptors());
	}

	/**
	 * Is sorting by date enabled?
	 * 
	 * @return <code>true</code> if it is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isSortByDate() {
		return !isSortByProjects();
	}

	/**
	 * Is sorting by projects enabled?
	 * 
	 * @return <code>true</code> if it is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isSortByProjects() {
		final IContentProvider provider= fHistoryViewer.getContentProvider();
		if (provider instanceof BrowseRefactoringHistoryContentProvider) {
			final BrowseRefactoringHistoryContentProvider extended= (BrowseRefactoringHistoryContentProvider) provider;
			return extended.isSortProjects();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setHistoryControlEnablement() {
		super.setHistoryControlEnablement();
		boolean enable= false;
		final RefactoringHistory history= (RefactoringHistory) fHistoryViewer.getInput();
		if (history != null) {
			final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
			if (proxies.length > 0)
				enable= true;
		}
		fSortProjects.setEnabled(enable);
		fSortTimestamps.setEnabled(enable);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		if (fDeselectAllButton != null)
			fDeselectAllButton.setEnabled(false);
		if (fSelectAllButton != null)
			fSelectAllButton.setEnabled(history != null && !history.isEmpty());
	}

	/**
	 * Sorts the refactorings by date.
	 */
	public void sortByDate() {
		fSortTimestamps.run();
	}

	/**
	 * Sorts the refactorings by projects.
	 */
	public void sortByProjects() {
		fSortProjects.run();
	}
}