package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.util.ResourceDeltaVisitor;

public class ChangeListener extends ResourceDeltaVisitor {

	static class ConfirmFolderDeleteDialog extends MessageDialog {
		private Button doNotShowOption;
		
		ConfirmFolderDeleteDialog(Shell parentShell) {
			super(
				parentShell, 
				getTitle(), 
				null,	// accept the default window icon
				getMessage(),
				MessageDialog.INFORMATION, 
				new String[] {IDialogConstants.OK_LABEL},
				0); 	// yes is the default
		}
		
		static String getTitle() {
			return Policy.bind("ChangeListener.Deleting_CVS_folders_1"); //$NON-NLS-1$
		}
		
		static String getMessage() {
			return Policy.bind("ChangeListener.Folders_cannot_be_deleted_from_a_CVS_server_from_the_client._To_delete_the_folder_2");	 //$NON-NLS-1$
		}
		
		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			doNotShowOption = new Button(composite, SWT.CHECK);
			doNotShowOption.addSelectionListener(selectionListener);
			
			doNotShowOption.setText(Policy.bind("ChangeListener.Do_not_ask_me_about_this_again_3")); //$NON-NLS-1$

			// set initial state
			doNotShowOption.setSelection(false);
			return composite;
		}
		
		private SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {					
				}
			}
		};
	}

	/*
	 * @see ResourceDeltaVisitor#handleAdded(IResource[])
	 */
	protected void handleAdded(IResource[] resources) {
	}

	/*
	 * @see ResourceDeltaVisitor#handleRemoved(IResource[])
	 */
	protected void handleRemoved(IResource[] resources) {
		List folderDeletions = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			if(resources[i].getType()==IResource.FOLDER) {
				folderDeletions.add(resources[i]);
			}
		}
		
		if(!folderDeletions.isEmpty()) {
			// prompt user
		}
	}

	/*
	 * @see ResourceDeltaVisitor#handleChanged(IResource[])
	 */
	protected void handleChanged(IResource[] resources) {
	}

	/*
	 * @see ResourceDeltaVisitor#finished()
	 */
	protected void finished() {
	}
}
