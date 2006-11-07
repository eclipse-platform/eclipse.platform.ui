/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.internal.ide.dialogs.InternalErrorDialog;
import org.eclipse.ui.statushandling.StatusHandlingState;
import org.eclipse.ui.statushandling.WorkbenchErrorHandler;

import com.ibm.icu.text.MessageFormat;

/**
 * This is the IDE workbench error handler. The instance of this handler is
 * returned from {@link IDEWorkbenchAdvisor#getWorkbenchErrorHandler()}. All
 * handled statuses are checked against severity and logged using logging
 * facility (by superclass).
 * 
 * @since 3.3
 */
public class IDEWorkbenchErrorHandler extends WorkbenchErrorHandler {

	private int exceptionCount = 0;

	private InternalErrorDialog dialog;

	private Shell defaultParent = new Shell();

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

	private static String MSG_FATAL_ERROR_RecursiveTitle = IDEWorkbenchMessages.Internal_error;

	/**
	 * @param configurer
	 */
	public IDEWorkbenchErrorHandler(IWorkbenchConfigurer configurer) {
		workbenchConfigurer = configurer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.errors.AbstractErrorHandler#handle(org.eclipse.ui.errors.HandlingStatusState)
	 */
	public void handle(StatusHandlingState handlingState) {
		if (handlingState.getStatus().getException() != null) {
			handleException(handlingState.getStatus().getException());
		}

		super.handle(handlingState);
	}

	/**
	 * Handles an event loop exception
	 * 
	 * @param t
	 *            the exception to handle
	 */
	public void handleException(Throwable t) {
		try {
			exceptionCount++;
			if (exceptionCount > 1) {
				if (closing) {
					return;
				}
				Shell parent = defaultParent;
				if (dialog != null && dialog.getShell() != null
						&& !dialog.getShell().isDisposed()) {
					parent = dialog.getShell();
				}
				MessageBox box = new MessageBox(parent, SWT.ICON_ERROR
						| SWT.YES | SWT.NO | SWT.SYSTEM_MODAL);
				box.setText(MSG_FATAL_ERROR_RecursiveTitle);
				box.setMessage(MessageFormat.format(MSG_FATAL_ERROR,
						new Object[] { MSG_FATAL_ERROR_Recursive }));
				int result = box.open();
				if (result == SWT.YES) {
					closeWorkbench();
				}
			} else {
				if (openQuestionDialog(t)) {
					closeWorkbench();
				}
			}
		} finally {
			exceptionCount--;
		}
	}

	/**
	 * Close the workbench and make sure all exceptions are handled.
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

	/**
	 * Inform the user about a fatal error. Return true if the user decide to
	 * exit workbench or if another fatal error happens while reporting it.
	 */
	private boolean openQuestionDialog(Throwable internalError) {
		try {
			String msg = null;
			if (internalError instanceof OutOfMemoryError) {
				msg = MSG_OutOfMemoryError;
			} else if (internalError instanceof StackOverflowError) {
				msg = MSG_StackOverflowError;
			} else if (internalError instanceof VirtualMachineError) {
				msg = MSG_VirtualMachineError;
			} else if (internalError instanceof SWTError) {
				msg = MSG_SWTError;
			} else {
				if (internalError.getMessage() == null) {
					msg = IDEWorkbenchMessages.InternalErrorNoArg;
				} else {
					msg = NLS.bind(IDEWorkbenchMessages.InternalErrorOneArg,
							internalError.getMessage());
				}
				// if (Policy.DEBUG_OPEN_ERROR_DIALOG) {
				// return openQuestion(null,
				// IDEWorkbenchMessages.Internal_error, msg,
				// internalError, 1);
				// }
				return false;
			}
			// Always open the dialog in case of major error but do not show the
			// detail button if not in debug mode.
			Throwable detail = internalError;
			if (!Policy.DEBUG_OPEN_ERROR_DIALOG) {
				detail = null;
			}
			return InternalErrorDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					IDEWorkbenchMessages.Internal_error, MessageFormat.format(
							MSG_FATAL_ERROR, new Object[] { msg }), detail, 1);
		} catch (Throwable th) {
			// Workbench may be in such bad shape (no OS handles left, out of
			// memory, etc)
			// that is cannot show a message to the user. Just bail out now.
			System.err
					.println("Error while informing user about event loop exception:"); //$NON-NLS-1$
			internalError.printStackTrace();
			System.err.println("Dialog open exception:"); //$NON-NLS-1$
			th.printStackTrace();
			return true;
		}
	}

	// private boolean openQuestion2(Shell parent, String title, String message,
	// Throwable detail, int defaultIndex) {
	// String[] labels;
	// if (detail == null) {
	// labels = new String[] { IDialogConstants.YES_LABEL,
	// IDialogConstants.NO_LABEL };
	// } else {
	// labels = new String[] { IDialogConstants.YES_LABEL,
	// IDialogConstants.NO_LABEL,
	// IDialogConstants.SHOW_DETAILS_LABEL };
	// }
	//
	// dialog = new InternalErrorDialog(parent, title, null, message, detail,
	// MessageDialog.QUESTION, labels, defaultIndex);
	//
	// if (detail != null) {
	// dialog.setDetailButton(2);
	// }
	// boolean result = dialog.open() == Window.OK;
	// dialog = null;
	// return result;
	// }
}
