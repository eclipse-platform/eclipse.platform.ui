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
package org.eclipse.ui.internal;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.InternalErrorDialog;
import org.eclipse.ui.internal.misc.Policy;

/**
 * Handles exceptions or errors caught in the event loop.
 * In case of a "simpler" exception such as NPE, log the exception,
 * open a dialog to inform the user and try to keep running.
 * In case of a exception like OutOfMemory and SWTError, log the exception,
 * open a dialog to ask the user to decide with the workbench should 
 * be terminated.
 */
class ExceptionHandler implements Window.IExceptionHandler {
	
	private int exceptionCount = 0; //To avoid recursive errors
	private InternalErrorDialog dialog;
	//Workaround. MessageDialog should accept null as parent;
	private Shell defaultParent = new Shell();
	private boolean closing = false;
	private Workbench workbench;
		
	//Pre-load all Strings trying to run as light as possible in case of fatal errors.
	private static String MSG_UNHANDLED_EXCEPTION = WorkbenchMessages.getString("Unhandled_exception"); //$NON-NLS-1$
	private static String MSG_OutOfMemoryError = WorkbenchMessages.getString("FatalError_OutOfMemoryError"); //$NON-NLS-1$
	private static String MSG_StackOverflowError = WorkbenchMessages.getString("FatalError_StackOverflowError"); //$NON-NLS-1$
	private static String MSG_VirtualMachineError = WorkbenchMessages.getString("FatalError_VirtualMachineError"); //$NON-NLS-1$
	private static String MSG_SWTError = WorkbenchMessages.getString("FatalError_SWTError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR = WorkbenchMessages.getString("FatalError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR_Recursive = WorkbenchMessages.getString("FatalError_RecursiveError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR_RecursiveTitle = WorkbenchMessages.getString("Internal_error"); //$NON-NLS-1$


/**
 * Initializes a new ExceptionHandler with its workbench.
 */
public ExceptionHandler(Workbench w) {
	workbench = w;
}
/**
 * @See IExceptionHandler
 */
public void handleException(Throwable t) {
	try {
		exceptionCount++;
		if(exceptionCount > 2) {
			//Avoid recursive error.
			if(t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw (Error)t;
		}
		if(t instanceof ThreadDeath) {
			// Don't catch ThreadDeath as this is a normal occurrence when the thread dies
			throw (ThreadDeath)t;
		} if(exceptionCount == 2) {
			if(closing)
				return;
			log(t);
			Shell parent = defaultParent;
			if(dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed())
				parent = dialog.getShell();
			MessageBox box = new MessageBox(parent,SWT.ICON_ERROR | SWT.YES | SWT.NO | SWT.SYSTEM_MODAL);
			box.setText(MSG_FATAL_ERROR_RecursiveTitle);
			box.setMessage(MSG_FATAL_ERROR_Recursive + MSG_FATAL_ERROR);
			int result = box.open();
			if(result == SWT.YES) {
				if(!closing)
					closeWorkbench();
			}
		} else {
			log(t);
			if(openQuestionDialog(t)) {
				if(!closing)
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
	try {
		closing = true;
		if(dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed())
			dialog.close();
		workbench.close(IPlatformRunnable.EXIT_OK,true);
	} catch (RuntimeException th) {
		/* It may not be possible to show the inform the user about this exception we may not 
		 * have more memory or OS handles etc. */
		System.err.println("Another fatal error happened while closing the workbench."); //$NON-NLS-1$
		th.printStackTrace();
		throw th;
	} catch (Error th) {
		/* It may not be possible to show the inform the user about this exception we may not 
		 * have more memory or OS handles etc. */
		System.err.println("Another fatal error happened while closing the workbench."); //$NON-NLS-1$
		th.printStackTrace();
		throw th;
	}
}
/**
 * Log the specified exception and make sure all exceptions are handled..
 */
private void log(Throwable t) {
	try {
		// For the status object, use the exception's message, or the exception name if no message.
		String msg = t.getMessage() == null ? t.toString() : t.getMessage();
		WorkbenchPlugin.log(MSG_UNHANDLED_EXCEPTION, new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, t));
	
		// special case for SWTException and SWTError to handle workaround for bug 6312
		Throwable nested = null;
		if (t instanceof SWTException)
			nested = ((SWTException)t).throwable;
		else if(t instanceof SWTError)
			nested = ((SWTError)t).throwable;	
		if (nested != null) {
			msg = nested.getMessage() == null ? nested.toString() : nested.getMessage();
			WorkbenchPlugin.log("\n*** Stack trace of contained exception ***", new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, nested)); //$NON-NLS-1$
		}
		if (WorkbenchPlugin.DEBUG) {
			t.printStackTrace();
		}
	} catch (Throwable th) {
		/* Probably: a log listener is crashing while we are handling exceptions.
		We can't do much here. Core should have already logged the exception since it
		should be the first log listener */
		System.err.println("A fatal error happened while logging a fatal error."); //$NON-NLS-1$
		t.printStackTrace();
		System.err.println("New exception."); //$NON-NLS-1$
		th.printStackTrace();
	}
}
/**
 * Inform the user about a fatal error. Return true if the user decide to 
 * exit workspace or if another faltal error happens while reporting it.
 */
private boolean openQuestionDialog(Throwable internalError) {
	try {
		String msg = null;
		if(internalError instanceof OutOfMemoryError) {
			msg = MSG_OutOfMemoryError;
		} else if(internalError instanceof StackOverflowError) {
			msg = MSG_StackOverflowError;
		} else if(internalError instanceof VirtualMachineError) {
			msg = MSG_VirtualMachineError;
		} else if(internalError instanceof SWTError) {
			msg = MSG_SWTError;
		} else {
			if (internalError.getMessage() == null) {
				msg = WorkbenchMessages.getString("InternalErrorNoArg");  //$NON-NLS-1$
			} else {
				msg = WorkbenchMessages.format("InternalErrorOneArg", new Object[] {internalError.getMessage()}); //$NON-NLS-1$
			}
			if(Policy.DEBUG_OPEN_ERROR_DIALOG) 
				return openQuestion(null, WorkbenchMessages.getString("Internal_error"), msg,internalError,1); //$NON-NLS-1$
			else
				return false;
	    }
	    //Allways open the dialog in case of major error but does not show the detail button
	    //if OPEN_DIALOG is false.
	    Throwable detail = internalError;
	    if(!Policy.DEBUG_OPEN_ERROR_DIALOG)
	    	detail = null;
		return InternalErrorDialog.openQuestion(null, WorkbenchMessages.getString("Internal_error"), msg + MSG_FATAL_ERROR,detail,1); //$NON-NLS-1$
	} catch (Throwable th) {
		/* It may not be possible to show the inform the user about this exception we may not 
		 * have more memory or OS handles etc. */
		System.err.println("A fatal error happened while informing the user about a fatal error."); //$NON-NLS-1$
		internalError.printStackTrace();
		System.err.println("New exception."); //$NON-NLS-1$
		th.printStackTrace();
		return true;
	}	
}

private boolean openQuestion(Shell parent, String title, String message, Throwable detail,int defaultIndex) {
	String[] labels;
	if(detail == null)
		labels = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL};
    else
		labels = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,IDialogConstants.SHOW_DETAILS_LABEL};

	dialog = new InternalErrorDialog(
		parent,
		title, 
		null,	// accept the default window icon
		message,
		detail,
		InternalErrorDialog.QUESTION, 
		labels, 
		defaultIndex);
		
	if(detail != null)
	    dialog.setDetailButton(2);
	boolean result = dialog.open() == 0;
	dialog = null;
	return result;
}
}
