/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	// Settings store
	private static final String DIALOG_SETTINGS_KEY= "SearchDialog.ScopePart"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAME= "lastUsedWorkingSetName"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAMES= "lastUsedWorkingSetNames"; //$NON-NLS-1$
	private static IDialogSettings fgSettingsStore;

	private Group fPart;

	// Scope radio buttons
	private Button fUseWorkspace;
	private Button fUseSelection;
	private Button fUseWorkingSet;


	private int			fScope;
	private Text			fWorkingSetText;
	private IWorkingSet[]	fWorkingSets;

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
		restoreState();
	}

	private void restoreState() {
		fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (fgSettingsStore == null)
			fgSettingsStore= SearchPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
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
				fWorkingSets= (IWorkingSet[])existingWorkingSets.toArray(new IWorkingSet[existingWorkingSets.size()]);
		} 
		else {
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
	 * Returns a new scope part with an initial working set.
	 * The part is not yet created.
	 * 
	 * @see #createPart(Composite)
	 * @param workingSet the initial working set
	 */
	public ScopePart(IWorkingSet[] workingSets) {
		Assert.isNotNull(workingSets);
		fScope= WORKING_SET_SCOPE;
		fWorkingSets= workingSets;
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
		boolean newState= fScope != WORKING_SET_SCOPE || fWorkingSets != null;
		if (fSearchPageContainer instanceof SearchDialog)
			((SearchDialog)fSearchPageContainer).setPerformActionEnabledFromScopePart(newState);
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
		if (getSelectedScope() == WORKING_SET_SCOPE)
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
		setSelectedScope(WORKING_SET_SCOPE);
		fWorkingSets= null;
		Set existingWorkingSets= new HashSet(workingSets.length);
		for (int i= 0; i < workingSets.length; i++) {
			String name= workingSets[i].getName();
			IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
			if (workingSet != null)
				existingWorkingSets.add(workingSet);
		}
		if (!existingWorkingSets.isEmpty())
			fWorkingSets= (IWorkingSet[])existingWorkingSets.toArray(new IWorkingSet[existingWorkingSets.size()]);
		
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
		if (fWorkingSets != null)
			fWorkingSetText.setText(toString(fWorkingSets));

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
		IWorkingSetSelectionDialog dialog=	PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(fUseSelection.getShell(), true);
		
		if (fWorkingSets != null)
			dialog.setSelection(fWorkingSets);
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getSelection();
			if (result.length > 0) {
				setSelectedWorkingSets((IWorkingSet[])result);
				return true;
			}
			fWorkingSetText.setText(""); //$NON-NLS-1$
			fWorkingSets= null;
			if (fScope == WORKING_SET_SCOPE)
				setSelectedScope(WORKSPACE_SCOPE);
			return false;
		} else {
			// test if selected working set has been removed
			if (!Arrays.asList(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets()).contains(fWorkingSets)) {
				fWorkingSetText.setText(""); //$NON-NLS-1$
				fWorkingSets= null;
				updateSearchPageContainerActionPerformedEnablement();
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
					result= SearchMessages.getFormattedString("ScopePart.workingSetConcatenation", new String[] {result, workingSetName}); //$NON-NLS-1$
				else {
					result= workingSetName;
					firstFound= true;
				}
			}
		}
		return result;
	}
}
