/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.application.IWorkbenchConfigurer;

import org.eclipse.ui.internal.ide.dialogs.InternalErrorDialog;

/**
 * Handles exception while running the event loop.
 * <p>
 * In case of a "simpler" exception such as NPE, log the exception,
 * open a dialog to inform the user and try to keep running.
 * In case of a exception like OutOfMemory and SWTError, log the exception,
 * open a dialog to ask the user to decide if the workbench should 
 * be terminated.
 * </p>
 */
public final class IDEExceptionHandler {

	private int exceptionCount = 0;
	private InternalErrorDialog dialog;
	private Shell defaultParent = new Shell();
	private boolean closing = false;
	private IWorkbenchConfigurer workbenchConfigurer;

	//Pre-load all Strings trying to run as light as possible in case of fatal errors.
	private static String MSG_OutOfMemoryError = IDEWorkbenchMessages.getString("FatalError_OutOfMemoryError"); //$NON-NLS-1$
	private static String MSG_StackOverflowError = IDEWorkbenchMessages.getString("FatalError_StackOverflowError"); //$NON-NLS-1$
	private static String MSG_VirtualMachineError = IDEWorkbenchMessages.getString("FatalError_VirtualMachineError"); //$NON-NLS-1$
	private static String MSG_SWTError = IDEWorkbenchMessages.getString("FatalError_SWTError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR = IDEWorkbenchMessages.getString("FatalError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR_Recursive = IDEWorkbenchMessages.getString("FatalError_RecursiveError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR_RecursiveTitle = IDEWorkbenchMessages.getString("Internal_error"); //$NON-NLS-1$

	/**
	 * Creates the exception handle for the IDE application
	 */
	public IDEExceptionHandler(IWorkbenchConfigurer configurer) {
		super();
		workbenchConfigurer = configurer;
	}
	
	/**
	 * Handles an event loop exception
	 */
	public void handleException(Throwable t) {
		try {
			exceptionCount++;
			if (exceptionCount > 1) {
				if (closing) {
					return;
				}
				Shell parent = defaultParent;
				if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed())
					parent = dialog.getShell();
				MessageBox box = new MessageBox(parent, SWT.ICON_ERROR | SWT.YES | SWT.NO | SWT.SYSTEM_MODAL);
				box.setText(MSG_FATAL_ERROR_RecursiveTitle);
				box.setMessage(MessageFormat.format(MSG_FATAL_ERROR, new Object[] {MSG_FATAL_ERROR_Recursive}));
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
			if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed())
				dialog.close();
			workbenchConfigurer.emergencyClose();
		} catch (RuntimeException re) {
			// Workbench may be in such bad shape (no OS handles left, out of memory, etc)
			// that is cannot even close. Just bail out now.
			System.err.println("Fatal runtime error happened during workbench emergency close."); //$NON-NLS-1$
			re.printStackTrace();
			throw re;
		} catch (Error e) {
			// Workbench may be in such bad shape (no OS handles left, out of memory, etc)
			// that is cannot even close. Just bail out now.
			System.err.println("Fatal error happened during workbench emergency close."); //$NON-NLS-1$
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
					msg = IDEWorkbenchMessages.getString("InternalErrorNoArg"); //$NON-NLS-1$
				} else {
					msg = IDEWorkbenchMessages.format("InternalErrorOneArg", new Object[] { internalError.getMessage()}); //$NON-NLS-1$
				}
				if (Policy.DEBUG_OPEN_ERROR_DIALOG)
					return openQuestion(null, IDEWorkbenchMessages.getString("Internal_error"), msg, internalError, 1); //$NON-NLS-1$
				else
					return false;
			}
			// Always open the dialog in case of major error but do not show the
			// detail button if not in debug mode.
			Throwable detail = internalError;
			if (!Policy.DEBUG_OPEN_ERROR_DIALOG)
				detail = null;
			return InternalErrorDialog.openQuestion(
					null, IDEWorkbenchMessages.getString("Internal_error"),//$NON-NLS-1$
					MessageFormat.format(MSG_FATAL_ERROR, new Object[] {msg}), 
					detail, 1); 
		} catch (Throwable th) {
			// Workbench may be in such bad shape (no OS handles left, out of memory, etc)
			// that is cannot show a message to the user. Just bail out now.
			System.err.println("Error while informing user about event loop exception:"); //$NON-NLS-1$
			internalError.printStackTrace();
			System.err.println("Dialog open exception:"); //$NON-NLS-1$
			th.printStackTrace();
			return true;
		}
	}

	private boolean openQuestion(Shell parent, String title, String message, Throwable detail, int defaultIndex) {
		String[] labels;
		if (detail == null)
			labels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
		else
			labels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.SHOW_DETAILS_LABEL };

			dialog = new InternalErrorDialog(
				parent, 
				title, 
				null, 
				message,
				detail, 
				MessageDialog.QUESTION,
				labels,
				defaultIndex);

		if (detail != null)
			dialog.setDetailButton(2);
		boolean result = dialog.open() == 0;
		dialog = null;
		return result;
	}
}
