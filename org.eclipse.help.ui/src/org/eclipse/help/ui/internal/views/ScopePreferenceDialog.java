package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ScopePreferenceDialog extends PreferenceDialog {
	private EngineDescriptorManager descManager;
	private ArrayList pendingOperations;
	
	class PendingOperation {
		int action;
		EngineDescriptor desc;
		public PendingOperation(int action, EngineDescriptor desc) {
			this.action = action;
			this.desc = desc;
		}
	}
	/**
	 * The Add button id.
	 */
	private final static int NEW_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * The Remove button id.
	 */
	private final static int DELETE_ID = IDialogConstants.CLIENT_ID + 2;
	
	public ScopePreferenceDialog(Shell parentShell, PreferenceManager manager, EngineDescriptorManager descManager) {
		super(parentShell, manager);
		this.descManager = descManager;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, NEW_ID, HelpUIResources.getString("ScopePreferenceDialog.new"), false); //$NON-NLS-1$
		Button rbutton = createButton(parent, DELETE_ID, HelpUIResources.getString("ScopePreferenceDialog.delete"), false); //$NON-NLS-1$
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
			removable = desc.isUserDefined();
		}
		getButton(DELETE_ID).setEnabled(removable);
	}
	
	private void doNew() {
		NewEngineWizard wizard = new NewEngineWizard(descManager.getEngineTypes());
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(400, 500);
		if (dialog.open()==WizardDialog.OK) {
			EngineTypeDescriptor etdesc = wizard.getSelectedEngineType();
			EngineDescriptor desc = new EngineDescriptor(descManager);
			desc.setEngineType(etdesc);
			desc.setUserDefined(true);
			desc.setId(computeNewId(etdesc));
			ScopePreferenceManager mng = (ScopePreferenceManager)getPreferenceManager();
			IPreferenceNode node = mng.addNode(desc);
			getTreeViewer().refresh();
			getTreeViewer().setSelection(new StructuredSelection(node));
			scheduleOperation(NEW_ID, desc);
		}
	}
	
	private String computeNewId(EngineTypeDescriptor etdesc) {
		return etdesc.getId() +"." + 1; //$NON-NLS-1$
	}

	private void doDelete() {
		Object obj = ((IStructuredSelection)getTreeViewer().getSelection()).getFirstElement();
		if (obj instanceof ScopePreferenceManager.EnginePreferenceNode) {
			ScopePreferenceManager.EnginePreferenceNode node = (ScopePreferenceManager.EnginePreferenceNode)obj;
			EngineDescriptor desc = node.getDescriptor();
			ScopePreferenceManager mng = (ScopePreferenceManager)getPreferenceManager();
			getTreeViewer().remove(node);
			scheduleOperation(DELETE_ID, desc);
		}
	}
	
	private void scheduleOperation(int action, EngineDescriptor desc) {
		if (pendingOperations==null)
			pendingOperations = new ArrayList();
		pendingOperations.add(new PendingOperation(action, desc));
	}
	protected void okPressed() {
		super.okPressed();
		if (pendingOperations!=null) {
			// process pending operations
			for (int i=0; i<pendingOperations.size(); i++) {
				PendingOperation op = (PendingOperation)pendingOperations.get(i);
				if (op.action==NEW_ID)
					descManager.add(op.desc);
				else
					descManager.remove(op.desc);
			}
			pendingOperations = null;
		}
		descManager.save();
	}
}