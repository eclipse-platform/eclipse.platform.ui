package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ScopePreferenceDialog extends PreferenceDialog {
	private EngineTypeDescriptor [] engineTypes;
	/**
	 * The Add button id.
	 */
	private final static int NEW_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * The Remove button id.
	 */
	private final static int DELETE_ID = IDialogConstants.CLIENT_ID + 2;
	
	public ScopePreferenceDialog(Shell parentShell, PreferenceManager manager, EngineTypeDescriptor [] engineTypes) {
		super(parentShell, manager);
		this.engineTypes = engineTypes;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, NEW_ID, "New...", false);
		Button rbutton = createButton(parent, DELETE_ID, "Delete", false);
		rbutton.setEnabled(false);

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns += 3;
		layout.makeColumnsEqualWidth = false;

		super.createButtonsForButtonBar(parent);
	}
	
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer viewer = super.createTreeViewer(parent);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ssel = (IStructuredSelection)event.getSelection();
				Object obj = ssel.getFirstElement();
				treeSelectionChanged(obj);
			}
		});
		return viewer;
	}
	
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case NEW_ID:
			doNew();
			break;
		case DELETE_ID:
			doDelete();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}
	
	private void treeSelectionChanged(Object obj) {
		boolean removable = false;
		if (obj instanceof ScopePreferenceManager.EnginePreferenceNode) {
			ScopePreferenceManager.EnginePreferenceNode node = (ScopePreferenceManager.EnginePreferenceNode)obj;
			EngineDescriptor desc = node.getDescriptor();
			removable = desc.isRemovable();
		}
		getButton(DELETE_ID).setEnabled(removable);
	}
	
	private void doNew() {
		NewEngineWizard wizard = new NewEngineWizard(engineTypes);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(400, 500);
		if (dialog.open()==WizardDialog.OK) {
			EngineTypeDescriptor etdesc = wizard.getSelectedEngineType();
			EngineDescriptor desc = new EngineDescriptor(null);
			desc.setEngineType(etdesc);
			desc.setUserDefined(true);
			desc.setId(computeNewId(etdesc));
			ScopePreferenceManager mng = (ScopePreferenceManager)getPreferenceManager();
			IPreferenceNode node = mng.addNode(desc);
			getTreeViewer().refresh();
			getTreeViewer().setSelection(new StructuredSelection(node));
		}
	}
	
	private String computeNewId(EngineTypeDescriptor etdesc) {
		return etdesc.getId() +"." + 1;
	}

	private void doDelete() {
	}
}