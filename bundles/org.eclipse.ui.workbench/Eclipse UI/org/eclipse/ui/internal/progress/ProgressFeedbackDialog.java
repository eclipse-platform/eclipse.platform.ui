/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;

import org.eclipse.ui.progress.ProgressFeedbackManager;
import org.eclipse.ui.internal.progress.ProgressMessages;

/**
 * The ProgressFeedbackDialog is a dialog that pops up to
 * show the user all of the pending UI feedback requests 
 * currently waiting.
 */
public class ProgressFeedbackDialog extends Dialog {

	private IStructuredContentProvider provider;
	ListViewer viewer;

	/**
	 * Create a new instance of the receiver with no 
	 * parent shell as this non modal and not tied to a window.
	 * @param IStructuredContentProvider
	 */
	public ProgressFeedbackDialog(IStructuredContentProvider contentProvider) {
		super(null);
		provider = contentProvider;
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}

	/**
	 * Get the label provider used to show the pending jobs.
	 * @return ILabelProvider
	 */
	private ILabelProvider getLabelProvider() {
		LabelProvider labelProvider = new LabelProvider() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((AwaitingFeedbackInfo) element).getMessage();
			}
		};

		return labelProvider;

	}

	/**
	 * Refresh the viewers contents.
	 *
	 */
	public void refreshViewer() {
		viewer.refresh(true);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ProgressMessages.getString("ProgressFeedbackDialog.DialogTitle")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		setBlockOnOpen(false);
		Composite mainArea = (Composite) super.createDialogArea(parent);

		viewer = new ListViewer(mainArea, SWT.BORDER);

		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		data.heightHint = convertVerticalDLUsToPixels(100);

		viewer.getControl().setLayoutData(data);
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(getLabelProvider());
		viewer.setInput(ProgressFeedbackManager.getFeedbackManager());

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection) selection;
					if (structured.size() > 0) {
						AwaitingFeedbackInfo info = (AwaitingFeedbackInfo) structured.getFirstElement();
						info.getJob().schedule();
					}
				}

			}
		});

		return mainArea;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		//Only create the OK button
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

}
