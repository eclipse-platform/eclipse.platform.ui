/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;

import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.ui.IWorkingSet;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;


class WorkingSetDialog extends InputDialog {

	private static class WorkingSetNameInputValidator implements IInputValidator {
		
		String fInitalName;
		
		public WorkingSetNameInputValidator(String initialName) {
			Assert.isNotNull(initialName, "initial name must not be null"); //$NON-NLS-1$
			fInitalName= initialName;
		}
		
		public String isValid(String newText) {
			if (newText == null ||newText.equals("")) //$NON-NLS-1$
				return WorkingSetMessages.getString("WorkingSetDialog.warning.nameMustNotBeEmpty"); //$NON-NLS-1$
			IWorkingSet[] workingSets= WorkingSet.getWorkingSets();
			if (newText.equals(fInitalName))
				return null;
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName()))
					return WorkingSetMessages.getString("WorkingSetDialog.warning.workspaceExists"); //$NON-NLS-1$
			}
			return null;
		}
	}

	// Widgets constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 100;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 200;

	private IWorkingSet fWorkingSet;
	private CheckboxTreeViewer fTree;
	
	WorkingSetDialog(Shell parentShell) {
		this(parentShell, ""); //$NON-NLS-1$
	}

	WorkingSetDialog(Shell parentShell, IWorkingSet workingSet) {
		this(parentShell, workingSet.getName());
		fWorkingSet= workingSet;
	}

	private WorkingSetDialog(Shell parentShell, String initialSelection) {
		super(parentShell, WorkingSetMessages.getString("WorkingSetDialog.title"), WorkingSetMessages.getString("WorkingSetDialog.message"), initialSelection, new WorkingSetNameInputValidator(initialSelection)); //$NON-NLS-2$ //$NON-NLS-1$
	}

	/*
	 * Overrides method from Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite= (Composite)super.createDialogArea(parent);

		fTree= new CheckboxTreeViewer(composite);
		fTree.setContentProvider(new WorkbenchContentProvider());
		fTree.setLabelProvider(new WorkbenchLabelProvider());
		fTree.setInput(SearchPlugin.getWorkspace().getRoot());

		GridData gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		fTree.getControl().setLayoutData(gd);

//		fTree.addCheckStateListener(new ICheckStateListener() {
//			public void checkStateChanged(CheckStateChangedEvent event) {
//				handleCheckStateChange(event);
//			}
//		});
		
		
		if (fWorkingSet != null)
			fTree.setCheckedElements(fWorkingSet.getResources());

		return composite;
	}

	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		if (fWorkingSet == null)
			fWorkingSet= new WorkingSet(getText().getText(), fTree.getCheckedElements());
		else {
			if (fWorkingSet instanceof WorkingSet) {
				((WorkingSet)fWorkingSet).setName(getText().getText());
				((WorkingSet)fWorkingSet).setResources(fTree.getCheckedElements());
			}
		}

		super.okPressed();
	}

	IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}

	//--- Checked state handling --------------------------	

	void handleCheckStateChange(CheckStateChangedEvent event) {
		boolean state= event.getChecked();
		IResource resource= (IResource)event.getElement();
		fTree.setGrayed(resource, false);
		if (resource instanceof IContainer)
			setSubtreeChecked((IContainer)resource, state);
		updateParentState(resource, state);
	}

	private void setSubtreeChecked(IContainer container, boolean state) {
		IResource[] members= null;
		try {
			members= container.members();
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, getShell(), "Error", "Error during update of checked state");
		}
		for (int i= members.length - 1; i >= 0; i--) {
			IResource element= members[i];
			fTree.setChecked(element, state);
			fTree.setGrayed(element, false);
			if (element instanceof IContainer)
				setSubtreeChecked((IContainer)element, state);
		}
	}

	private void updateParentState(IResource child, boolean baseChildState) {

		if (child == null || child.getParent() == null)
			return;

		IContainer parent= child.getParent();		
		boolean allSameState= true;
		IResource[] members= null;
		try {
			members= parent.members();
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, getShell(), "Error", "Error during update of checked state");
		}
		for (int i= members.length -1; i >= 0; i--) {
			if (fTree.getChecked(members[i]) != baseChildState) {
				allSameState= false;
				break;
			}
		}
	
		fTree.setGrayed(parent, !allSameState);
		fTree.setChecked(parent, !allSameState || baseChildState);
		
		updateParentState(parent, baseChildState);
	}

	private void initializeCheckedState() {
		if (fWorkingSet == null)
			return;

		IResource[] resources= fWorkingSet.getResources();
		fTree.setCheckedElements(resources);
		for (int i= 0; i < resources.length; i++) {
			if (resources[i] instanceof IContainer)
				setSubtreeChecked((IContainer)resources[i], true);
			updateParentState(resources[i], true);
		}
	}
}
