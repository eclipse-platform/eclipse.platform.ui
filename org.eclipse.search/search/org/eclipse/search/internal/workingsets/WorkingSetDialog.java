/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;

import org.eclipse.search.ui.IWorkingSet;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;


class WorkingSetDialog extends InputDialog {

	private static class WorkingSetNameInputValidator implements IInputValidator {
		
		private String fInitalName;
		private boolean fFirstCheck= true;
		private CheckboxTreeViewer fTree;
		
		public WorkingSetNameInputValidator(String initialName) {
			Assert.isNotNull(initialName, "initial name must not be null"); //$NON-NLS-1$
			fInitalName= initialName;
		}
		
		private void setTree(CheckboxTreeViewer tree) {
			fTree= tree;
		}
		
		public String isValid(String newText) {
			if (newText == null || newText.equals("")) { //$NON-NLS-1$
				if (fInitalName.equals("") && fFirstCheck) { //$NON-NLS-1$
					fFirstCheck= false;
					return " "; //$NON-NLS-1$
				}
				return WorkingSetMessages.getString("WorkingSetDialog.warning.nameMustNotBeEmpty"); //$NON-NLS-1$
			}
			if (!newText.equals(fInitalName)) {
				IWorkingSet[] workingSets= WorkingSet.getWorkingSets();
				for (int i= 0; i < workingSets.length; i++) {
					if (newText.equals(workingSets[i].getName()))
						return WorkingSetMessages.getString("WorkingSetDialog.warning.workingSetExists"); //$NON-NLS-1$
				}
			}
			
			if (fTree.getCheckedElements().length == 0)
				return WorkingSetMessages.getString("WorkingSetDialog.warning.resourceMustBeChecked"); //$NON-NLS-1$
			
			return null;
		}
	}

	// Widgets constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 50;
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
		fTree.setUseHashlookup(true);
		fTree.setContentProvider(new WorkbenchContentProvider());
		fTree.setLabelProvider(createLabelProvider());
		fTree.setInput(SearchPlugin.getWorkspace().getRoot());
		fTree.setSorter(new WorkbenchViewerSorter());

		GridData gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		gd.widthHint= SIZING_SELECTION_WIDGET_WIDTH;
		fTree.getControl().setLayoutData(gd);

		fTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});
		
		fTree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
			}
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element= event.getElement();
				if (!fTree.getGrayed(element))
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
						public void run() {
							setSubtreeChecked((IContainer)element, fTree.getChecked(element), false);
						}
					});
			}
		});
		
		initializeCheckedState();
		((WorkingSetNameInputValidator)getValidator()).setTree(fTree);
		disableClosedProjects();

		return composite;
	}

	private ILabelProvider createLabelProvider() {
		ILabelDecorator decorationMgr= PlatformUI.getWorkbench().getDecoratorManager();
		return new DecoratingLabelProvider(new WorkbenchLabelProvider(), decorationMgr);
	}

	private void disableClosedProjects() {
		IProject[] projects= SearchPlugin.getWorkspace().getRoot().getProjects();
		for (int i= 0; i < projects.length; i++) {
			if (!projects[i].isOpen())
				fTree.setGrayed(projects[i], true);
		}
	}

	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		ArrayList resources= new ArrayList(10);
		findCheckedResources(resources, (IContainer)fTree.getInput());
		if (fWorkingSet == null)
			fWorkingSet= new WorkingSet(getText().getText(), resources.toArray());
		else if (fWorkingSet instanceof WorkingSet) {
			// Add not accessible resources
			IResource[] oldResources= fWorkingSet.getResources();
			for (int i= 0; i < oldResources.length; i++)
				if (!oldResources[i].isAccessible())
					resources.add(oldResources[i]);

			((WorkingSet)fWorkingSet).setName(getText().getText());
			((WorkingSet)fWorkingSet).setResources(resources.toArray());
		}
		super.okPressed();
	}

	IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}

	private void findCheckedResources(List checkedResources, IContainer container) {
		IResource[] resources= null;
		try {
			resources= container.members();
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, getShell(), WorkingSetMessages.getString("WorkingSetDialog.error"), WorkingSetMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i= 0; i < resources.length; i++) {
			if (fTree.getGrayed(resources[i]))
				findCheckedResources(checkedResources, (IContainer)resources[i]);
			else if (fTree.getChecked(resources[i]))
				checkedResources.add(resources[i]);
		}
	}

	//--- Checked state handling --------------------------	

	void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IResource resource= (IResource)event.getElement();
				if (!resource.isAccessible()) {
					MessageDialog.openInformation(getShell(), WorkingSetMessages.getString("WorkingSetDialog.projectClosedDialog.title"), WorkingSetMessages.getString("WorkingSetDialog.projectClosedDialog.message")); //$NON-NLS-2$ //$NON-NLS-1$
					fTree.setChecked(resource, false);
					fTree.setGrayed(resource, true);
					return;
				}
				boolean state= event.getChecked();		
				fTree.setGrayed(resource, false);
				if (resource instanceof IContainer)
					setSubtreeChecked((IContainer)resource, state, true);
					
				updateParentState(resource, state);
			}
		});
		
		String errorMessage= getValidator().isValid(getText().getText());
		if (errorMessage == null)
			errorMessage= "";
		getErrorMessageLabel().setText(errorMessage);
		getOkButton().setEnabled(errorMessage.length() == 0);
	}

	private void setSubtreeChecked(IContainer container, boolean state, boolean checkExpandedState) {
		if (!container.isAccessible() || !fTree.getExpandedState(container) && checkExpandedState)
			return;
		
		IResource[] members= null;
		try {
			members= container.members();
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, getShell(), WorkingSetMessages.getString("WorkingSetDialog.error"), WorkingSetMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i= members.length - 1; i >= 0; i--) {
			IResource element= members[i];
			if (state) {
				fTree.setChecked(element, true);
				fTree.setGrayed(element, false);
			}
			else
				fTree.setGrayChecked(element, false);
			if (element instanceof IContainer)
				setSubtreeChecked((IContainer)element, state, true);
		}
	}

	private void updateParentState(IResource child, boolean baseChildState) {

		if (child == null || child.getParent() == null || !child.isAccessible())
			return;

		IContainer parent= child.getParent();
		boolean allSameState= true;
		IResource[] members= null;
		try {
			members= parent.members();
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, getShell(), WorkingSetMessages.getString("WorkingSetDialog.error"), WorkingSetMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i= members.length -1; i >= 0; i--) {
			if (fTree.getChecked(members[i]) != baseChildState || fTree.getGrayed(members[i])) {
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

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IResource[] resources= fWorkingSet.getResources();
				fTree.setCheckedElements(resources);
				for (int i= 0; i < resources.length; i++) {
					if (resources[i] instanceof IContainer)
						setSubtreeChecked((IContainer)resources[i], true, true);
					updateParentState(resources[i], true);
				}
			}
		});
	}
}
