/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import org.eclipse.search.ui.ISearchPageContainer;

import org.eclipse.search.internal.ui.util.SWTUtil;

public class ScopePart {

	// The possible scopes
	public static final int WORKSPACE_SCOPE= 0;
	public static final int SELECTION_SCOPE= 1;
	public static final int WORKING_SET_SCOPE= 2;

	private static String	fgLRUsedWorkingSetName;

	// Settings store
	private static final String DIALOG_SETTINGS_KEY= "SearchDialog.ScopePart"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAME= "lastUsedWorkingSetName"; //$NON-NLS-1$
	private static IDialogSettings fgSettingsStore;

	private Group fPart;

	// Scope radio buttons
	private Button fUseWorkspace;
	private Button fUseSelection;
	private Button fUseWorkingSet;


	private int			fScope;
	private Text			fWorkingSetText;
	private IWorkingSet	fWorkingSet;

	// Reference to its search page container (can be null)
	private ISearchPageContainer fSearchPageContainer;
	
	/**
	 * Returns a new scope part with workspace as initial scope.
	 * The part is not yet created.
	 */
	public ScopePart() {
		this(WORKSPACE_SCOPE);
	}

	/**
	 * Returns a new scope part with workspace as initial scope.
	 * The part is not yet created.
	 */
	public ScopePart(ISearchPageContainer searchPageContainer) {
		this(WORKSPACE_SCOPE);
		fSearchPageContainer= searchPageContainer;
	}

	/**
	 * Returns a new scope part with an initial scope.
	 * The part is not yet created.
	 * 
	 * @see #createPart(Composite)
	 * @param initialScope the initial scope
	 */
	public ScopePart(int initialScope) {
		Assert.isLegal(initialScope >= 0 && initialScope <= 3);
		fScope= initialScope;
		fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (fgSettingsStore == null)
			fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
		String lruWorkingSetName= fgSettingsStore.get(STORE_LRU_WORKING_SET_NAME);
		fWorkingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(lruWorkingSetName);
	}

	/**
	 * Returns a new scope part with an initial working set.
	 * The part is not yet created.
	 * 
	 * @see #createPart(Composite)
	 * @param workingSet the initial working set
	 */
	public ScopePart(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet);
		fScope= WORKING_SET_SCOPE;
		fWorkingSet= workingSet;
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
		fScope= scope;
		switch (fScope) {
			case WORKSPACE_SCOPE:
				fUseWorkspace.setSelection(true);
				fUseSelection.setSelection(false);
				fUseWorkingSet.setSelection(false);
				break;
			case SELECTION_SCOPE:
				fUseWorkspace.setSelection(false);
				fUseSelection.setSelection(true);				
				fUseWorkingSet.setSelection(false);
				break;
			case WORKING_SET_SCOPE:
				fUseWorkspace.setSelection(false);
				fUseSelection.setSelection(false);
				fUseWorkingSet.setSelection(true);
				break;
		}

		updateSearchPageContainerActionPerformedEnablement();
	}

	private void updateSearchPageContainerActionPerformedEnablement() {
		if (fSearchPageContainer != null)
			fSearchPageContainer.setPerformActionEnabled(fScope != WORKING_SET_SCOPE || fWorkingSet != null);
	}

	/**
	 * Returns the selected working set of this part.
	 * 
	 * @return the selected working set or null
	 * 			- if the scope is not WORKING_SET_SCOPE
	 * 			- if there is no working set selected
	 */
	public IWorkingSet getSelectedWorkingSet() {
		if (getSelectedScope() == WORKING_SET_SCOPE)
			return fWorkingSet;
		else
			return null;
	}

	/**
	 * Sets the selected working set for this part.
	 * This method must only be called on a created part.
	 * 
	 * @param workingSet the working set to be selected
	 */
	public void setSelectedWorkingSet(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet);
		setSelectedScope(WORKING_SET_SCOPE);
		String name= workingSet.getName();
		workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
		if (workingSet != null) {
			fWorkingSet= workingSet;
			fgSettingsStore.put(STORE_LRU_WORKING_SET_NAME, workingSet.getName());
		} else {
			name= ""; //$NON-NLS-1$
			fWorkingSet= null;
		}
		if (fWorkingSetText != null)
			fWorkingSetText.setText(name);
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
		layout.numColumns= 3;
		fPart.setLayout(layout);
		fPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fUseWorkspace= new Button(fPart, SWT.RADIO);
		fUseWorkspace.setData(new Integer(WORKSPACE_SCOPE));
		fUseWorkspace.setText(SearchMessages.getString("ScopePart.workspaceScope.text")); //$NON-NLS-1$

		fUseSelection= new Button(fPart, SWT.RADIO);
		fUseSelection.setData(new Integer(SELECTION_SCOPE));
		fUseSelection.setText(SearchMessages.getString("ScopePart.selectedResourcesScope.text")); //$NON-NLS-1$
		ISelection selection= fSearchPageContainer.getSelection();
		fUseSelection.setEnabled(selection instanceof IStructuredSelection && !fSearchPageContainer.getSelection().isEmpty());
		
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 8;
		fUseSelection.setLayoutData(gd);

		fUseWorkingSet= new Button(fPart, SWT.RADIO);
		fUseWorkingSet.setData(new Integer(WORKING_SET_SCOPE));
		fUseWorkingSet.setText(SearchMessages.getString("ScopePart.workingSetScope.text")); //$NON-NLS-1$
		fWorkingSetText= new Text(fPart, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		Button chooseWorkingSet= new Button(fPart, SWT.PUSH);
		chooseWorkingSet.setLayoutData(new GridData());
		chooseWorkingSet.setText(SearchMessages.getString("ScopePart.workingSetChooseButton.text")); //$NON-NLS-1$
		SWTUtil.setButtonDimensionHint(chooseWorkingSet);
		chooseWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (handleChooseWorkingSet()) {
					setSelectedScope(WORKING_SET_SCOPE);
				}
			}
		});
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= 8;
		gd.widthHint= SWTUtil.convertWidthInCharsToPixels(30, fWorkingSetText);
		fWorkingSetText.setLayoutData(gd);

		// Add scope change listeners
		SelectionAdapter scopeChangedLister= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleScopeChanged(e);
			}
		};
		fUseWorkspace.addSelectionListener(scopeChangedLister);
		fUseSelection.addSelectionListener(scopeChangedLister);
		fUseWorkingSet.addSelectionListener(scopeChangedLister);

		// Set initial scope
		setSelectedScope(fScope);
		
		// Set initial working set
		if (fWorkingSet != null)
			fWorkingSetText.setText(fWorkingSet.getName());

		return fPart;
	}

	private void handleScopeChanged(SelectionEvent e) {
		Object source= e.getSource();
		if (source instanceof Button) {
			Button button= (Button)source;
			if (button.getSelection())
				setSelectedScope(((Integer)button.getData()).intValue());
		}
	}

	private boolean handleChooseWorkingSet() {
		IWorkingSetSelectionDialog dialog=	PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(fUseSelection.getShell());
		
		if (fWorkingSet != null)
			dialog.setSelection(new IWorkingSet[] {fWorkingSet});
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getSelection();
			if (result.length == 1) {
				setSelectedWorkingSet((IWorkingSet)result[0]);
				return true;
			}
			fWorkingSetText.setText(""); //$NON-NLS-1$
			fWorkingSet= null;
			if (fScope == WORKING_SET_SCOPE)
				setSelectedScope(WORKSPACE_SCOPE);
			return false;
		} else {
			// test if selected working set has been removed
			if (!Arrays.asList(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets()).contains(fWorkingSet)) {
				fWorkingSetText.setText(""); //$NON-NLS-1$
				fWorkingSet= null;
				updateSearchPageContainerActionPerformedEnablement();
			}
		}
		return false;
	}
	
	void setVisible(boolean state) {
		fPart.setVisible(state);
	}
}
