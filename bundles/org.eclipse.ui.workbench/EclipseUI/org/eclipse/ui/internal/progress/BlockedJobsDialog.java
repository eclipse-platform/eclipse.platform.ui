/*******************************************************************************
 * Copyright (c) 2004, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The BlockedJobsDialog class displays a dialog that provides information on
 * the running jobs.
 */
public class BlockedJobsDialog extends IconAndMessageDialog {
	/**
	 * The singleton dialog instance. A singleton avoids the possibility of
	 * recursive dialogs being created. The singleton is created when a dialog is
	 * requested, and cleared when the dialog is disposed.
	 */
	protected static BlockedJobsDialog singleton;

	/**
	 * The running jobs progress viewer.
	 */
	private DetailedProgressViewer viewer;

	/**
	 * The Cancel button control.
	 */
	private Button cancelSelected;

	private IProgressMonitor blockingMonitor;

	/**
	 * Creates a progress monitor dialog under the given shell. It also sets the
	 * dialog's message. The dialog is opened automatically after a reasonable
	 * delay. When no longer needed, the dialog must be closed by calling
	 * <code>close(IProgressMonitor)</code>, where the supplied monitor is the same
	 * monitor passed to this factory method.
	 *
	 * @param parentShell    The parent shell, or <code>null</code> to create a
	 *                       top-level shell. If the parentShell is not null we will
	 *                       open immediately as parenting has been determined. If
	 *                       it is <code>null</code> then the dialog will not open
	 *                       until there is no modal shell blocking it.
	 * @param blockedMonitor The monitor that is currently blocked
	 * @param reason         A status describing why the monitor is blocked
	 * @return BlockedJobsDialog
	 */
	public static BlockedJobsDialog createBlockedDialog(Shell parentShell, IProgressMonitor blockedMonitor,
			IStatus reason) {
		// Use an existing dialog if available.
		if (singleton != null) {
			return singleton;
		}
		singleton = new BlockedJobsDialog(parentShell, blockedMonitor, reason);

		/**
		 * If there is no parent shell we have not been asked for a parent so we want to
		 * avoid blocking. If there is a parent then it is OK to open.
		 */
		if (parentShell == null) {
			// Wait for long operation time to prevent a proliferation of
			// dialogs.
			dialogJob.schedule(PlatformUI.getWorkbench().getProgressService().getLongOperationTime());
		} else {
			singleton.open();
		}

		return singleton;
	}

	// Create the job that will open the dialog after a delay.
	private static final WorkbenchJob dialogJob = new WorkbenchJob(
			WorkbenchMessages.EventLoopProgressMonitor_OpenDialogJobName) {
		{
			setSystem(true);
		}
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (singleton == null) {
				return Status.CANCEL_STATUS;
			}
			if (ProgressManagerUtil.rescheduleIfModalShellOpen(this)) {
				return Status.CANCEL_STATUS;
			}
			singleton.open();
			return Status.OK_STATUS;
		}
	};

	/**
	 * The monitor is done. Clear the receiver.
	 *
	 * @param monitor The monitor that is now cleared.
	 */
	public static void clear(IProgressMonitor monitor) {
		if (singleton != null)
			singleton.close(monitor);
	}

	/**
	 * Creates a progress monitor dialog under the given shell. It also sets the
	 * dialog's\ message. <code>open</code> is non-blocking.
	 *
	 * @param parentShell    The parent shell, or <code>null</code> to create a
	 *                       top-level shell.
	 * @param blocking       The monitor that is blocking the job
	 * @param blockingStatus A status describing why the monitor is blocked
	 */
	private BlockedJobsDialog(Shell parentShell, IProgressMonitor blocking, IStatus blockingStatus) {
		super(parentShell == null ? ProgressManagerUtil.getDefaultParent() : parentShell);
		blockingMonitor = blocking;
		setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | getDefaultOrientation());
		// no close button
		setBlockOnOpen(false);
		setMessage(blockingStatus.getMessage());
	}

	/**
	 * Creates the dialog area under the parent composite.
	 *
	 * @param parent The parent Composite.
	 *
	 * @return parent The parent Composite.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage(message);
		createMessageArea(parent);
		showJobDetails(parent);
		return parent;
	}

	/**
	 * Creates a dialog area in the parent composite and displays a progress tree
	 * viewer of the running jobs.
	 *
	 * @param parent The parent Composite.
	 */
	void showJobDetails(Composite parent) {
		viewer = new DetailedProgressViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setComparator(ProgressManagerUtil.getProgressViewerComparator());
		ProgressViewerContentProvider provider = getContentProvider();
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
		viewer.setLabelProvider(new ProgressLabelProvider());
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		int heightHint = convertHeightInCharsToPixels(10);
		data.heightHint = heightHint;
		viewer.getControl().setLayoutData(data);
	}

	/**
	 * Returns the content provider used for the receiver.
	 *
	 * @return ProgressTreeContentProvider
	 */
	private ProgressViewerContentProvider getContentProvider() {
		return new ProgressViewerContentProvider(viewer, true, false);
	}

	/**
	 * Clears the cursors in the dialog.
	 */
	private void clearCursors() {
		clearCursor(cancelSelected);
		clearCursor(getShell());
	}

	/**
	 * Clears the cursor on the supplied control.
	 *
	 * @param control the control where custom cursor is removed
	 */
	private void clearCursor(Control control) {
		if (control != null && !control.isDisposed()) {
			control.setCursor(null);
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(ProgressMessages.BlockedJobsDialog_BlockedTitle);
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
	}

	/**
	 * This method sets the message in the message label.
	 *
	 * @param messageString the String for the message area
	 */
	private void setMessage(String messageString) {
		// must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed()) {
			return;
		}
		messageLabel.setText(message);
	}

	@Override
	protected Image getImage() {
		return getInfoImage();
	}

	/**
	 * Returns the progress monitor being used for this dialog. This allows
	 * recursive blockages to also respond to cancellation.
	 *
	 * @return IProgressMonitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return blockingMonitor;
	}

	/**
	 * Requests that the blocked jobs dialog be closed. The supplied monitor must be
	 * the same one that was passed to the createBlockedDialog method.
	 *
	 * @param monitor the monitor associated with this block dialog. Dialog will not
	 *                close if it is another monitor.
	 * @return <code>true</code> if successfully closed
	 */
	public boolean close(IProgressMonitor monitor) {
		// ignore requests to close the dialog from all but the first monitor
		if (blockingMonitor != monitor) {
			return false;
		}
		return close();
	}

	@Override
	public boolean close() {
		// Clear the singleton first.
		singleton = null;
		clearCursors();
		return super.close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, ProgressMessages.BlockedJobsDialog_CancelButtonText, false);
	}

	@Override
	protected void cancelPressed() {
		setReturnCode(CANCEL);
		blockingMonitor.clearBlocked(); // clearBlocked() results in calling close()
		blockingMonitor.setCanceled(true);
	}
}
