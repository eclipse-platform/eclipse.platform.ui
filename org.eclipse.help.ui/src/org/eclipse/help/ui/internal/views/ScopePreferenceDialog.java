package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ScopePreferenceDialog extends PreferenceDialog {
	/**
	 * The Add button id.
	 */
	private final static int NEW_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * The Remove button id.
	 */
	private final static int DELETE_ID = IDialogConstants.CLIENT_ID + 2;
	
	public ScopePreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
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
	}

	private void doDelete() {
	}
}