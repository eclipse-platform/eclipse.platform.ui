/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.dialogs.SelectionDialog;
import java.util.Arrays;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.Assert;

import org.eclipse.search.internal.ui.util.SWTUtil;
import org.eclipse.search.internal.workingsets.WorkingSet;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.IWorkingSet;
import org.eclipse.search.ui.SearchUI;

public class ScopePart {

	// The possible scopes
	public static final int WORKSPACE_SCOPE= 0;
	public static final int SELECTION_SCOPE= 1;
	public static final int WORKING_SET_SCOPE= 2;

	private static String	fgLRUsedWorkingSetName;

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
		fWorkingSet= SearchUI.findWorkingSet(fgLRUsedWorkingSetName);
		if (fWorkingSet == null && WorkingSet.getWorkingSets().length > 0)
			fWorkingSet= WorkingSet.getWorkingSets()[0];
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
		workingSet= SearchUI.findWorkingSet(name);
		if (workingSet != null) {
			fWorkingSet= workingSet;
			fgLRUsedWorkingSetName= name;
		} else {
			name= "";
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
		Group group= new Group(parent, SWT.NONE);
		group.setText(SearchMessages.getString("ScopePart.group.text")); //$NON-NLS-1$

		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fUseWorkspace= new Button(group, SWT.RADIO);
		fUseWorkspace.setData(new Integer(WORKSPACE_SCOPE));
		fUseWorkspace.setText(SearchMessages.getString("ScopePart.workspaceScope.text")); //$NON-NLS-1$

		fUseSelection= new Button(group, SWT.RADIO);
		fUseSelection.setData(new Integer(SELECTION_SCOPE));
		fUseSelection.setText(SearchMessages.getString("ScopePart.selectedResourcesScope.text")); //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 8;
		fUseSelection.setLayoutData(gd);

		fUseWorkingSet= new Button(group, SWT.RADIO);
		fUseWorkingSet.setData(new Integer(WORKING_SET_SCOPE));
		fUseWorkingSet.setText(SearchMessages.getString("ScopePart.workingSetScope.text")); //$NON-NLS-1$
		fWorkingSetText= new Text(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		Button chooseWorkingSet= new Button(group, SWT.PUSH);
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

		return group;
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
		SelectionDialog dialog= SearchUI.createWorkingSetDialog(fUseSelection.getShell());
		if (fWorkingSet != null)
			dialog.setInitialSelections(new IWorkingSet[] {fWorkingSet});
		if (dialog.open() == dialog.OK) {
			IWorkingSet workingSet= (IWorkingSet)dialog.getResult()[0];
			setSelectedWorkingSet(workingSet);
			fgLRUsedWorkingSetName= workingSet.getName();
			return true;
		} else {
			// test if selected working set has been removed
			if (!Arrays.asList(WorkingSet.getWorkingSets()).contains(fWorkingSet)) {
				fWorkingSetText.setText(""); //$NON-NLS-1$
				fWorkingSet= null;
			}
		}
		return false;
	}
}
