package org.eclipse.search.internal.workingsets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.IWorkingSet;
import org.eclipse.search.internal.ui.util.*;


public class WorkingSetDialog extends InputDialog {

	private static class WorkingSetNameInputValidator implements IInputValidator {
		
		String fInitalName;
		
		public WorkingSetNameInputValidator(String initialName) {
			Assert.isNotNull(initialName, "initial name must not be null");
			fInitalName= initialName;
		}
		
		public String isValid(String newText) {
			if (newText == null ||newText.equals(""))
				return "The name must not be empty.";
			IWorkingSet[] workingSets= WorkingSet.getWorkingSets();
			if (newText.equals(fInitalName))
				return null;
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName()))
					return "A workspace with that name already exists.";
			}
			return null;
		}
	}

	// Widgets constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 300;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 300;

	private IWorkingSet fWorkingSet;
	private CheckboxTreeViewer fTree;
	
	public WorkingSetDialog(Shell parentShell) {
		this(parentShell, "");
	}

	public WorkingSetDialog(Shell parentShell, IWorkingSet workingSet) {
		this(parentShell, workingSet.getName());
		fWorkingSet= workingSet;
	}

	private WorkingSetDialog(Shell parentShell, String initialSelection) {
		super(parentShell, "Working Set", "Define the working set name:", initialSelection, new WorkingSetNameInputValidator(initialSelection));
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

	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}
}
