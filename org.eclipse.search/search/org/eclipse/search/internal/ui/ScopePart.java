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
package org.eclipse.search.internal.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.search.internal.ui.util.PixelConverter;
import org.eclipse.search.internal.ui.util.SWTUtil;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

public class ScopePart {

	// Settings store
	private static final String DIALOG_SETTINGS_KEY= "SearchDialog.ScopePart"; //$NON-NLS-1$
	private static final String STORE_SCOPE= "scope"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAME= "lastUsedWorkingSetName"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAMES= "lastUsedWorkingSetNames"; //$NON-NLS-1$
	private static IDialogSettings fgSettingsStore;

	static {
		fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (fgSettingsStore == null)
			fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
	}

	private Group fPart;

	// Scope radio buttons
	private Button fUseWorkspace;
	private Button fUseSelection;
	private Button fUseProject;
	private Button fUseWorkingSet;

	private int fScope;
	private boolean fCanSearchEnclosingProjects;
	private Text fWorkingSetText;
	private IWorkingSet[] fWorkingSets;

	// Reference to its search page container (can be null)
	private ISearchPageContainer fSearchPageContainer;

	/**
	 * Returns a new scope part with workspace as initial scope.
	 * The part is not yet created.
	 */
	public ScopePart(ISearchPageContainer searchPageContainer, boolean searchEnclosingProjects) {
		int initialScope= getStoredScope();
		Assert.isLegal(initialScope >= 0 && initialScope <= 3);
		fScope= initialScope;
		fCanSearchEnclosingProjects= searchEnclosingProjects;
		if (!fCanSearchEnclosingProjects && fScope == ISearchPageContainer.SELECTED_PROJECTS_SCOPE)
			fScope= ISearchPageContainer.WORKSPACE_SCOPE;
		fSearchPageContainer= searchPageContainer;
		restoreState();
	}

	private static int getStoredScope() {
		int scope;
		try {
			scope= fgSettingsStore.getInt(STORE_SCOPE);
		} catch (NumberFormatException ex) {
			scope= ISearchPageContainer.WORKSPACE_SCOPE;
		}
		if (scope != ISearchPageContainer.WORKING_SET_SCOPE
			&& scope != ISearchPageContainer.SELECTION_SCOPE
			&& scope != ISearchPageContainer.SELECTED_PROJECTS_SCOPE
			&& scope != ISearchPageContainer.WORKSPACE_SCOPE)
			scope= ISearchPageContainer.WORKSPACE_SCOPE;
		return scope;
	}


	private void restoreState() {
		String[] lruWorkingSetNames= fgSettingsStore.getArray(STORE_LRU_WORKING_SET_NAMES);
		if (lruWorkingSetNames != null) {
			Set existingWorkingSets= new HashSet(lruWorkingSetNames.length);
			for (int i= 0; i < lruWorkingSetNames.length; i++) {
				String name= lruWorkingSetNames[i];
				IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
				if (workingSet != null)
					existingWorkingSets.add(workingSet);
			}
			if (!existingWorkingSets.isEmpty())
				fWorkingSets= (IWorkingSet[]) existingWorkingSets.toArray(new IWorkingSet[existingWorkingSets.size()]);
		} else {
			// Backward compatibility
			String workingSetName= fgSettingsStore.get(STORE_LRU_WORKING_SET_NAME);
			if (workingSetName != null) {
				IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
				if (workingSet != null) {
					fWorkingSets= new IWorkingSet[] { workingSet };
					saveState();
				}
			}
		}
	}


	/**
	 * Returns the scope selected in this part
	 * 
	 * @return the selected scope
	 */
	public int getSelectedScope() {
		return fScope;
	}

	/**
	 * Sets the selected scope.
	 * This method must only be called on a created part.
	 * 
	 * @param scope the scope to be selected in this part
	 */
	public void setSelectedScope(int scope) {
		Assert.isLegal(scope >= 0 && scope <= 3);
		Assert.isNotNull(fUseWorkspace);
		Assert.isNotNull(fUseSelection);
		Assert.isNotNull(fUseWorkingSet);
		Assert.isNotNull(fUseProject);
		fScope= scope;
		if (fScope == ISearchPageContainer.SELECTED_PROJECTS_SCOPE) {
			if (!fCanSearchEnclosingProjects) {
				SearchPlugin.log(new Status(IStatus.WARNING, NewSearchUI.PLUGIN_ID, IStatus.WARNING, "Enclosing projects scope set on search page that does not support it", null)); //$NON-NLS-1$
				fScope= ISearchPageContainer.WORKSPACE_SCOPE;
			} else if (!fUseProject.isEnabled()) {
				fScope= ISearchPageContainer.WORKSPACE_SCOPE;
			}
		} else if (fScope == ISearchPageContainer.SELECTION_SCOPE) {
			if (!fUseSelection.isEnabled()) {
				fScope= ISearchPageContainer.WORKSPACE_SCOPE;
			}
		}
		switch (fScope) {
			case ISearchPageContainer.WORKSPACE_SCOPE :
				fUseWorkspace.setSelection(true);
				fUseSelection.setSelection(false);
				fUseProject.setSelection(false);
				fUseWorkingSet.setSelection(false);
				break;
			case ISearchPageContainer.SELECTION_SCOPE :
				fUseWorkspace.setSelection(false);
				fUseSelection.setSelection(true);
				fUseProject.setSelection(false);
				fUseWorkingSet.setSelection(false);
				break;
			case ISearchPageContainer.WORKING_SET_SCOPE :
				fUseWorkspace.setSelection(false);
				fUseSelection.setSelection(false);
				fUseProject.setSelection(false);
				fUseWorkingSet.setSelection(true);
				break;
			case ISearchPageContainer.SELECTED_PROJECTS_SCOPE :
				fUseWorkspace.setSelection(false);
				fUseSelection.setSelection(false);
				fUseProject.setSelection(true);
				fUseWorkingSet.setSelection(false);
				break;
		}

		updateSearchPageContainerActionPerformedEnablement();
		fgSettingsStore.put(STORE_SCOPE, fScope);
		
	}

	private void updateSearchPageContainerActionPerformedEnablement() {
		boolean newState= fScope != ISearchPageContainer.WORKING_SET_SCOPE || fWorkingSets != null;
		if (fSearchPageContainer instanceof SearchDialog)
			 ((SearchDialog) fSearchPageContainer).setPerformActionEnabledFromScopePart(newState);
		else if (fSearchPageContainer != null)
			fSearchPageContainer.setPerformActionEnabled(newState);
	}

	/**
	 * Returns the selected working set of this part.
	 * 
	 * @return the selected working set or null
	 * 			- if the scope is not WORKING_SET_SCOPE
	 * 			- if there is no working set selected
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (getSelectedScope() == ISearchPageContainer.WORKING_SET_SCOPE)
			return fWorkingSets;
		else
			return null;
	}

	/**
	 * Sets the selected working set for this part.
	 * This method must only be called on a created part.
	 * 
	 * @param workingSet the working set to be selected
	 */
	public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
		Assert.isNotNull(workingSets);
		setSelectedScope(ISearchPageContainer.WORKING_SET_SCOPE);
		fWorkingSets= null;
		Set existingWorkingSets= new HashSet(workingSets.length);
		for (int i= 0; i < workingSets.length; i++) {
			String name= workingSets[i].getName();
			IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
			if (workingSet != null)
				existingWorkingSets.add(workingSet);
		}
		if (!existingWorkingSets.isEmpty())
			fWorkingSets= (IWorkingSet[]) existingWorkingSets.toArray(new IWorkingSet[existingWorkingSets.size()]);

		saveState();

		if (fWorkingSetText != null)
			fWorkingSetText.setText(toString(fWorkingSets));
	}

	/**
	 * Saves the last recently used working sets,
	 * if any.
	 */
	private void saveState() {
		if (fWorkingSets != null && fWorkingSets.length > 0) {
			String[] existingWorkingSetNames= new String[fWorkingSets.length];
			for (int i= 0; i < existingWorkingSetNames.length; i++)
				existingWorkingSetNames[i]= fWorkingSets[i].getName();
			fgSettingsStore.put(STORE_LRU_WORKING_SET_NAMES, existingWorkingSetNames);
		}
	}

	/**
	 * Creates this scope part.
	 * 
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 */
	public Composite createPart(Composite parent) {
		fPart= new Group(parent, SWT.NONE);
		fPart.setText(SearchMessages.getString("ScopePart.group.text")); //$NON-NLS-1$

		GridLayout layout= new GridLayout();
		layout.numColumns= 4;
		fPart.setLayout(layout);
		fPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fUseWorkspace= new Button(fPart, SWT.RADIO);
		fUseWorkspace.setData(new Integer(ISearchPageContainer.WORKSPACE_SCOPE));
		fUseWorkspace.setText(SearchMessages.getString("ScopePart.workspaceScope.text")); //$NON-NLS-1$

		fUseSelection= new Button(fPart, SWT.RADIO);
		fUseSelection.setData(new Integer(ISearchPageContainer.SELECTION_SCOPE));
		fUseSelection.setText(SearchMessages.getString("ScopePart.selectedResourcesScope.text")); //$NON-NLS-1$
		ISelection selection= fSearchPageContainer.getSelection();
		fUseSelection.setEnabled((selection instanceof IStructuredSelection && 
				!fSearchPageContainer.getSelection().isEmpty()));
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 8;
		fUseSelection.setLayoutData(gd);

		fUseProject= new Button(fPart, SWT.RADIO);
		fUseProject.setData(new Integer(ISearchPageContainer.SELECTED_PROJECTS_SCOPE));
		fUseProject.setText(SearchMessages.getString("ScopePart.enclosingProjectsScope.text")); //$NON-NLS-1$
		fUseProject.setEnabled((selection instanceof IStructuredSelection && 
								!fSearchPageContainer.getSelection().isEmpty()) ||
								hasFocusEditor());

		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 8;
		fUseProject.setLayoutData(gd);
		if (!fCanSearchEnclosingProjects)
			fUseProject.setVisible(false);

		fUseWorkingSet= new Button(fPart, SWT.RADIO);
		fUseWorkingSet.setData(new Integer(ISearchPageContainer.WORKING_SET_SCOPE));
		fUseWorkingSet.setText(SearchMessages.getString("ScopePart.workingSetScope.text")); //$NON-NLS-1$
		fWorkingSetText= new Text(fPart, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		Button chooseWorkingSet= new Button(fPart, SWT.PUSH);
		chooseWorkingSet.setLayoutData(new GridData());
		chooseWorkingSet.setText(SearchMessages.getString("ScopePart.workingSetChooseButton.text")); //$NON-NLS-1$
		SWTUtil.setButtonDimensionHint(chooseWorkingSet);
		chooseWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (handleChooseWorkingSet()) {
					setSelectedScope(ISearchPageContainer.WORKING_SET_SCOPE);
				}
			}
		});
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= 8;
		gd.horizontalSpan= 2;
		gd.widthHint= new PixelConverter(fWorkingSetText).convertWidthInCharsToPixels(30);
		fWorkingSetText.setLayoutData(gd);

		// Add scope change listeners
		SelectionAdapter scopeChangedLister= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleScopeChanged(e);
			}
		};
		fUseWorkspace.addSelectionListener(scopeChangedLister);
		fUseSelection.addSelectionListener(scopeChangedLister);
		fUseProject.addSelectionListener(scopeChangedLister);
		fUseWorkingSet.addSelectionListener(scopeChangedLister);

		// Set initial scope
		setSelectedScope(fScope);

		// Set initial working set
		if (fWorkingSets != null)
			fWorkingSetText.setText(toString(fWorkingSets));

		return fPart;
	}

	/**
	 * @return Whether an editor has the focus
	 */
	private boolean hasFocusEditor() {
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		if (activePage == null)
			return false;
		if (activePage.getActivePart() instanceof IEditorPart)
			return true;
		return false;
	}

	private void handleScopeChanged(SelectionEvent e) {
		Object source= e.getSource();
		if (source instanceof Button) {
			Button button= (Button) source;
			if (button.getSelection())
				setSelectedScope(((Integer) button.getData()).intValue());
		}
	}

	private boolean handleChooseWorkingSet() {
		IWorkingSetSelectionDialog dialog=
			PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(
				fUseSelection.getShell(),
				true);

		if (fWorkingSets != null)
			dialog.setSelection(fWorkingSets);
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getSelection();
			if (result.length > 0) {
				setSelectedWorkingSets((IWorkingSet[]) result);
				return true;
			}
			fWorkingSetText.setText(""); //$NON-NLS-1$
			fWorkingSets= null;
			if (fScope == ISearchPageContainer.WORKING_SET_SCOPE)
				setSelectedScope(ISearchPageContainer.WORKSPACE_SCOPE);
			return false;
		} else {
			if (fWorkingSets != null) {
				// test if selected working set has been removed
				int i= 0;
				while (i < fWorkingSets.length) {
					if (PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(fWorkingSets[i].getName())
						== null)
						break;
					i++;
				}
				if (i < fWorkingSets.length) {
					fWorkingSetText.setText(""); //$NON-NLS-1$
					fWorkingSets= null;
					updateSearchPageContainerActionPerformedEnablement();
				}
			}
		}
		return false;
	}

	void setVisible(boolean state) {
		fPart.setVisible(state);
	}

	public static String toString(IWorkingSet[] workingSets) {
		String result= ""; //$NON-NLS-1$
		if (workingSets != null && workingSets.length > 0) {
			Arrays.sort(workingSets, new WorkingSetComparator());
			boolean firstFound= false;
			for (int i= 0; i < workingSets.length; i++) {
				String workingSetName= workingSets[i].getName();
				if (firstFound)
					result= SearchMessages.getFormattedString("ScopePart.workingSetConcatenation", new String[] { result, workingSetName }); //$NON-NLS-1$
				else {
					result= workingSetName;
					firstFound= true;
				}
			}
		}
		return result;
	}
}
