/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.internal.ide.dialogs.InternalErrorDialog;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;

/**
 * This is the IDE workbench error handler. The instance of this handler is
 * returned from IDEWorkbenchAdvisor#getWorkbenchErrorHandler(). All handled
 * statuses are checked against severity and logged using logging facility (by
 * superclass).
 *
 * @since 3.3
 */
public class IDEWorkbenchErrorHandler extends WorkbenchErrorHandler {

	private int exceptionCount = 0;

	static private FatalErrorDialog dialog;

	private boolean closing = false;

	private IWorkbenchConfigurer workbenchConfigurer;

	// Pre-load all Strings trying to run as light as possible in case of fatal
	// errors.
	private static String MSG_OutOfMemoryError = IDEWorkbenchMessages.FatalError_OutOfMemoryError;

	private static String MSG_StackOverflowError = IDEWorkbenchMessages.FatalError_StackOverflowError;

	private static String MSG_VirtualMachineError = IDEWorkbenchMessages.FatalError_VirtualMachineError;

	private static String MSG_SWTError = IDEWorkbenchMessages.FatalError_SWTError;

	private static String MSG_FATAL_ERROR = IDEWorkbenchMessages.FatalError;

	private static String MSG_FATAL_ERROR_Recursive = IDEWorkbenchMessages.FatalError_RecursiveError;

	private static String MSG_FATAL_ERROR_Title = IDEWorkbenchMessages.InternalError;

	// cache handled statuses
	private final Map<IStatus, ?> map = Collections.synchronizedMap(new WeakHashMap<>());

	/**
	 * @param configurer the workbench configurer
	 */
	public IDEWorkbenchErrorHandler(IWorkbenchConfigurer configurer) {
		workbenchConfigurer = configurer;
	}

	@Override
	public void handle(final StatusAdapter statusAdapter, int style) {

		// if fatal error occurs, we will show the blocking error dialog anyway
		if (isFatal(statusAdapter)) {
			// if we modify the hint, we have to be sure that status picked up
			// from .log will not be handled if it is reported independently via
			// StatusManager
			if (!map.containsKey(statusAdapter.getStatus())) {
				map.put(statusAdapter.getStatus(), null);
			} else {
				return;
			}
			if (statusAdapter
					.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) == Boolean.TRUE) {
				statusAdapter.setProperty(
						IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
						Boolean.FALSE);
			}
			super.handle(statusAdapter, style | StatusManager.BLOCK);
		} else {
			super.handle(statusAdapter, style);
		}

		// if fatal error occurs, we will ask to close the workbench
		if (isFatal(statusAdapter)) {
			UIJob handlingExceptionJob = UIJob.create("IDE Exception Handler", m -> { //$NON-NLS-1$
				handleException(statusAdapter.getStatus().getException());
				return new Status(IStatus.OK, IDEWorkbenchPlugin.IDE_WORKBENCH,
						IDEWorkbenchMessages.IDEExceptionHandler_ExceptionHandledMessage);
			});

			handlingExceptionJob.setSystem(true);
			handlingExceptionJob.schedule();
		}
	}

	private boolean isFatal(final StatusAdapter statusAdapter) {
		if (statusAdapter.getStatus().getException() != null
				&& (statusAdapter.getStatus().getException() instanceof OutOfMemoryError
						|| statusAdapter.getStatus().getException() instanceof StackOverflowError
						|| statusAdapter.getStatus().getException() instanceof VirtualMachineError || statusAdapter
						.getStatus().getException() instanceof SWTError)) {
			return true;
		}
		return false;
	}

	private void handleException(Throwable t) {
		try {
			exceptionCount++;
			if (exceptionCount > 1) {
				try {
					dialog.updateMessage(MessageFormat.format(MSG_FATAL_ERROR, MSG_FATAL_ERROR_Recursive));
					dialog.getShell().forceActive();
				} catch (Exception e) {
					// avoid further explosion of recursive exceptions
					e.printStackTrace();
				}
			} else if (openQuestionDialog(t)) {
				closeWorkbench();
			}
		} finally {
			exceptionCount--;
		}
	}

	/**
	 * Informs the user about a fatal error. Returns true if the user decide to
	 * exit workbench or if another fatal error happens while reporting it.
	 */
	private boolean openQuestionDialog(Throwable t) {
		try {
			String msg = null;
			if (t instanceof OutOfMemoryError) {
				msg = MSG_OutOfMemoryError;
			} else if (t instanceof StackOverflowError) {
				msg = MSG_StackOverflowError;
			} else if (t instanceof VirtualMachineError) {
				msg = MSG_VirtualMachineError;
			} else if (t instanceof SWTError) {
				msg = MSG_SWTError;
			} else if (t.getMessage() == null) {
				msg = IDEWorkbenchMessages.InternalErrorNoArg;
			} else {
				msg = NLS.bind(IDEWorkbenchMessages.InternalErrorOneArg, t
						.getMessage());
			}

			// Always open the dialog in case of major error but do not show the
			// detail button if not in debug mode.
			Throwable detail = t;
			if (!Policy.DEBUG_OPEN_ERROR_DIALOG) {
				detail = null;
			}

			dialog = openInternalQuestionDialog(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					MSG_FATAL_ERROR_Title, MessageFormat.format(MSG_FATAL_ERROR, msg), detail, 1);

			return dialog.open() == 0;
		} catch (Throwable th) {
			// Workbench may be in such bad shape (no OS handles left, out of
			// memory, etc)
			// that is cannot show a message to the user. Just bail out now.
			System.err
					.println("Error while informing user about event loop exception:"); //$NON-NLS-1$
			t.printStackTrace();
			System.err.println("Dialog open exception:"); //$NON-NLS-1$
			th.printStackTrace();
			return true;
		}
	}

	private FatalErrorDialog openInternalQuestionDialog(Shell parent,
			String title, String message, Throwable detail, int defaultIndex) {
		String[] labels;
		if (detail == null) {
			labels = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
		} else {
			labels = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL,
					IDialogConstants.SHOW_DETAILS_LABEL };
		}

		FatalErrorDialog errorDialog = new FatalErrorDialog(parent, title, null, // accept
				// the
				// default
				// window
				// icon
				message, detail, MessageDialog.QUESTION, labels, defaultIndex);
		if (detail != null) {
			errorDialog.setDetailButton(2);
		}
		return errorDialog;
	}

	/**
	 * Closes the workbench and make sure all exceptions are handled.
	 */
	private void closeWorkbench() {
		if (closing) {
			return;
		}

		try {
			closing = true;
			if (dialog != null && dialog.getShell() != null
					&& !dialog.getShell().isDisposed()) {
				dialog.close();
			}
			//@see WorkbenchAdvisor#getWorkbenchConfigurer()
			if (workbenchConfigurer != null)
				workbenchConfigurer.emergencyClose();
		} catch (RuntimeException re) {
			// Workbench may be in such bad shape (no OS handles left, out of
			// memory, etc)
			// that is cannot even close. Just bail out now.
			System.err
					.println("Fatal runtime error happened during workbench emergency close."); //$NON-NLS-1$
			re.printStackTrace();
			throw re;
		} catch (Error e) {
			// Workbench may be in such bad shape (no OS handles left, out of
			// memory, etc)
			// that is cannot even close. Just bail out now.
			System.err
					.println("Fatal error happened during workbench emergency close."); //$NON-NLS-1$
			e.printStackTrace();
			throw e;
		}
	}

	private static class FatalErrorDialog extends InternalErrorDialog {

		public FatalErrorDialog(Shell parentShell, String dialogTitle,
				Image dialogTitleImage, String dialogMessage, Throwable detail,
				int dialogImageType, String[] dialogButtonLabels,
				int defaultIndex) {
			super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
					detail, dialogImageType, dialogButtonLabels, defaultIndex);
		}

		/**
		 * Updates the dialog message
		 *
		 * @param newMessage
		 *            new message
		 */
		public void updateMessage(String newMessage) {
			this.message = newMessage;
			if (messageLabel != null && !messageLabel.isDisposed()) {
				this.messageLabel.setText(newMessage);
				this.messageLabel.update();
			}
		}
	}
}
