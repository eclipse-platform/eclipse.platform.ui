/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Prefence dialog for the workbench including the ability to load/save
 * preferences.
 */
public class WorkbenchPreferenceDialog extends FilteredPreferenceDialog {
	/**
	 * There can only ever be one instance of the workbench's preference dialog.
	 * This keeps a handle on this instance, so that attempts to create a second
	 * dialog should just fail (or return the original instance).
	 * 
	 * @since 3.1
	 */
	private static WorkbenchPreferenceDialog instance = null;
	
	/**
	 * Creates a workbench preference dialog to a particular preference page. It
	 * is the responsibility of the caller to then call <code>open()</code>.
	 * The call to <code>open()</code> will not return until the dialog
	 * closes, so this is the last chance to manipulate the dialog.
	 * 
	 * @param shell
	 * 			The Shell to parent the dialog off of if it is not
	 * 			already created. May be <code>null</code>
	 * 			in which case the active workbench window will be used
	 * 			if available.
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @return The selected preference page.
	 * @since 3.1
	 */
	public static final WorkbenchPreferenceDialog createDialogOn(Shell shell, final String preferencePageId) {
		final WorkbenchPreferenceDialog dialog;

		if (instance == null) {
			/*
			 * There is no existing preference dialog, so open a new one with
			 * the given selected page.
			 */

			Shell parentShell = shell;
			if (parentShell == null) {
				// Determine a decent parent shell.
				final IWorkbench workbench = PlatformUI.getWorkbench();
				final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					parentShell = workbenchWindow.getShell();
				} else {
					parentShell = null;
				}
			}

			// Create the dialog
			final PreferenceManager preferenceManager = PlatformUI.getWorkbench()
					.getPreferenceManager();
			dialog = new WorkbenchPreferenceDialog(parentShell, preferenceManager);
			if (preferencePageId != null) {
				dialog.setSelectedNode(preferencePageId);
			}
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					dialog.getShell(),
					IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

		} else {
			/*
			 * There is an existing preference dialog, so let's just select the
			 * given preference page.
			 */
			dialog = instance;
			if (preferencePageId != null) {
				dialog.setCurrentPageId(preferencePageId);
			}

		}

		// Get the selected node, and return it.
		return dialog;
	}

	/**
	 * The preference page history.
	 * 
	 * @since 3.1
	 */
	private PreferencePageHistory history;

    //The title for pageContent area
	private Label title;


	private Composite toolBarComposite;

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public WorkbenchPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		Assert.isTrue((instance == null),
				"There cannot be two preference dialogs at once in the workbench."); //$NON-NLS-1$
		instance = this;
		history = new PreferencePageHistory(this);
	}

	/**
	 * Closes the preference dialog. This clears out the singleton instance
	 * before calling the super implementation.
	 * 
	 * @return <code>true</code> if the dialog is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 * @since 3.1
	 */
	public final boolean close() {
		instance = null;
		history.dispose();
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		toolBarComposite = new Composite(composite, SWT.NONE);
		GridLayout toolBarLayout = new GridLayout();
		toolBarLayout.marginHeight = 0;
		toolBarLayout.marginWidth = 0;
		toolBarComposite.setLayout(toolBarLayout);
		toolBarComposite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		toolBarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		createDialogContents(composite);

		applyDialogFont(composite);

		return composite;

	}

	/**
	 * Create the contents area of the dialog
	 * 
	 * @param parent
	 */
	void createDialogContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		int columns = 3;
		layout.numColumns = columns;
		composite.setLayout(layout);
		GridData compositeData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(compositeData);
		applyDialogFont(composite);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite, treeControl);

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));

		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout pageAreaLayout = new GridLayout();
		pageAreaLayout.marginHeight = 0;
		pageAreaLayout.marginWidth = 0;
		pageAreaLayout.horizontalSpacing = 0;
		pageAreaLayout.numColumns = 2;
		pageAreaComposite.setLayout(pageAreaLayout);
		
		Label versep = new Label(pageAreaComposite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData verGd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
		verGd.horizontalSpan = columns;
		versep.setLayoutData(verGd);
		versep.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		// Build the Page container
		setPageContainer(createPageContainer(pageAreaComposite));
		getPageContainer().setLayoutData(new GridData(GridData.FILL_BOTH));
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = columns;
		separator.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createPageContainer(Composite parent) {

		Composite outer=new Composite(parent, SWT.NULL);
		GridData compositeData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		outer.setLayoutData(compositeData);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		outer.setLayout(layout);
		
		Composite top=new Composite(outer, SWT.NULL);
		GridData topLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		top.setLayoutData(topLayoutData);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight =0;
		topLayout.verticalSpacing = 0;
				
		int columns = 2;
		topLayout.numColumns = columns;
		top.setLayout(topLayout);
		
		Composite titleArea = new Composite(top, SWT.NULL);
		title=new Label(titleArea, SWT.NONE);
		GridLayout titleLayout = new GridLayout();
		titleLayout.marginWidth = 0;
		titleLayout.verticalSpacing = 0;
		titleArea.setLayout(titleLayout);
        GridData titleData =new GridData(SWT.FILL, SWT.FILL, true, false);
        titleArea.setLayoutData(titleData);
        title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		title.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		
		Composite toolbarArea=new Composite(top, SWT.NULL);
		GridLayout toolbarLayout = new GridLayout();
		toolbarArea.setLayout(toolbarLayout);
		toolbarArea.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		Control topBar = getContainerToolBar(toolbarArea);
		topBar.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
				
		Label separator = new Label(outer, SWT.HORIZONTAL | SWT.SEPARATOR);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = columns;
		separator.setLayoutData(gd);
		
		Composite bottom = new Composite(outer, SWT.NULL);
		GridLayout bottomLayout = new GridLayout();
		bottom.setLayout(bottomLayout);
		bottom.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		Composite result = new Composite(bottom, SWT.NULL);
		
		GridData resultData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
				
		result.setLayout(getPageLayout());
		result.setLayoutData(resultData);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageControl(org.eclipse.jface.preference.IPreferencePage,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	protected void createPageControl(IPreferencePage page, Composite parent) {
		if (page instanceof PreferencePage)
			((PreferencePage) page).createControl(parent);
		else
			super.createPageControl(page, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createSash(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.swt.widgets.Control)
	 */
	protected Sash createSash(Composite composite, Control rightControl) {
		Sash sash = super.createSash(composite, rightControl);
		sash.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		return sash;
	}


	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to
	 * <code>WorkbenchActivityHelper.filterItem()</code> then
	 * <code>null</code> is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node))
			return null;
		return node;
	}

	/**
	 * Get the toolbar for the container
	 * 
	 * @return Control
	 */
	private Control getContainerToolBar(Composite composite) {

		ToolBar historyBar = new ToolBar(composite, SWT.HORIZONTAL | SWT.FLAT);
		ToolBarManager historyManager = new ToolBarManager(historyBar);

		history.createHistoryControls(historyBar, historyManager);
		
		historyManager.update(false);

		return historyBar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#selectSavedItem()
	 */
	protected void selectSavedItem() {
		getTreeViewer().setInput(getPreferenceManager());
		super.selectSavedItem();
		if(getTreeViewer().getTree().getItemCount() > 1) {
			//unfortunately super will force focus to the list but we want the type ahead combo to get it.
			filteredTree.getFilterControl().setFocus();
		}
	}

	/*
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 * @since 3.1
	 */
	protected boolean showPage(IPreferenceNode node) {
		final boolean success = super.showPage(node);
		if (success) {
			history.addHistoryEntry(new PreferenceHistoryEntry(node.getId(), node.getLabelText(),
					null));
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateMessage()
	 */
	public void updateMessage() {
		// No longer required as the pages do this.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTitle()
	 */
	public void updateTitle() {
		title.setText(getCurrentPage().getTitle());
		title.setImage(getCurrentPage().getImage());
	}

}
