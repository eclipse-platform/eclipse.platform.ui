/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The BlockedJobsDialog class displays a dialog that provides information on the running jobs.
 */
public class BlockedJobsDialog extends IconAndMessageDialog {

	/**
	 * The running jobs progress tree.
	 * 
	 * @see /org.eclipse.ui.workbench/Eclipse UI/org/eclipse/ui/internal/progress/ProgressTreeViewer.java (org.eclipse.ui.internal.progress)
	 */
	private ProgressTreeViewer viewer;

	/**
	 * Name to use for task when normal task name is empty string.
	 */
	private static String DEFAULT_TASKNAME = JFaceResources.getString("ProgressMonitorDialog.message"); //$NON-NLS-1$

	/**
	 * The Cancel button control.
	 */
	protected Button cancel;

	/**
	 * The cursor for the Cancel button.
	 */
	private Cursor arrowCursor;

	/**
	 * The cursor for the Shell.
	 */
	private Cursor waitCursor;

	/**
	 * Creates a progress monitor dialog under the given shell. It also sets the dialog's\ message. <code>open</code> is non-blocking.
	 * 
	 * @param parentShell
	 *            The parent shell, or <code>null</code> to create a top-level shell.
	 */
	public BlockedJobsDialog(Shell parentShell) {
		super(parentShell);
		setMessage(DEFAULT_TASKNAME);
		setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		// no close button
		setBlockOnOpen(false);
	}

	/**
	 * This method creates the dialog area under the parent composite.
	 * 
	 * @param parent
	 * 			The parent Composite.
	 * 
	 * @return parent
	 * 			The parent Composite.
	 */
	protected Control createDialogArea(Composite parent) {
		setMessage(message);
		createMessageArea(parent);
		showJobDetails(parent);

		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getMessageLabelStyle()
	 */
	protected int getMessageLabelStyle() {
		return super.getMessageLabelStyle() | SWT.CENTER;
	}

	/**
	 * This method creates a dialog area in the parent composite and displays a progress tree viewer of the running jobs.
	 * 
	 * @param parent
	 *            The parent Composite.
	 */
	void showJobDetails(Composite parent) {

		viewer = new ProgressTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		viewer.setUseHashlookup(true);
		viewer.setSorter(new ViewerSorter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		});

		IContentProvider provider = new ProgressTreeContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
		viewer.setLabelProvider(new ProgressLabelProvider());

		GridData data = new GridData();
		data.horizontalSpan = 2;
		int heightHint = convertHeightInCharsToPixels(10);
		data.heightHint = heightHint;
		data.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(data);
	}
	
	/**
	 * This method overrides the IconAndMessageDialog's method to create a Cancel button.
	 * 
	 * @param parent
	 * 			The parent Composite.
	 * 
	 * @return parent
	 * 			The parent Composite.
	 */
	protected Control createButtonBar(Composite parent) {
		cancel = new Button(parent, SWT.PUSH);
		cancel.setText(ProgressMessages.getString("CancelJobsButton.title")); //$NON-NLS-1$

		cancel.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ISelection selection = viewer.getSelection();
				viewer.deleteJob(selection);

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// Method used to set the default selection.
			}

		});

		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.END;
		data.verticalAlignment = GridData.END;
		cancel.setLayoutData(data);

		if (arrowCursor == null)
			arrowCursor = new Cursor(cancel.getDisplay(), SWT.CURSOR_ARROW);
		cancel.setCursor(arrowCursor);
		
		return parent;
	}

	/**
	 * This method clears the cursors in the dialog.
	 */
	protected void clearCursors() {
		if (cancel != null && !cancel.isDisposed()) {
			cancel.setCursor(null);
		}
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.setCursor(null);
		}
		if (arrowCursor != null)
			arrowCursor.dispose();
		if (waitCursor != null)
			waitCursor.dispose();
		arrowCursor = null;
		waitCursor = null;
	}

	/**
	 * This method complements the Window's class' configureShell method by adding a title, and setting
	 * the appropriate cursor.
	 * 
	 * @param shell
	 *            The dialog's shell.
	 * 
	 * @see /org.eclipse.jface/src/org/eclipse/jface/window/Window.java (org.eclipse.jface.window;)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(JFaceResources.getString("ProgressMonitorDialog.title")); //$NON-NLS-1$
		if (waitCursor == null)
			waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(waitCursor);
	}


	/**
	 * This method sets the message in the message label.
	 */
	private void setMessage(String messageString) {
		//must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.setText(message);
	}

	/**
	 * This method returns the dialog's lock image.
	 */
	protected Image getImage() {
		return JFaceResources.getImageRegistry().get(Dialog.DLG_IMG_LOCKED);
	}

	/**
	 * This method sets the dialog's message status.
	 * 
	 * @param reason
	 *            The status representing the outcome of the operation.
	 * 
	 * @see /org.eclipse.core.runtime/src-runtime/org/eclipse/core/runtime/IStatus.java (org.eclipse.core.runtime)
	 */
	public void setStatus(IStatus reason) {
		setMessage(reason.getMessage());
	}

}
