package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.dialogs.InternalErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;

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
	private Workbench workbench;
	
	//Pre-load all Strings trying to run as light as possible in case of fatal errors.
	private static String MSG_UNHANDLED_EXCEPTION = WorkbenchMessages.getString("Unhandled_exception");
	private static String MSG_OutOfMemoryError = WorkbenchMessages.getString("FatalError_OutOfMemoryError"); //$NON-NLS-1$
	private static String MSG_StackOverflowError = WorkbenchMessages.getString("FatalError_StackOverflowError"); //$NON-NLS-1$
	private static String MSG_VirtualMachineError = WorkbenchMessages.getString("FatalError_VirtualMachineError"); //$NON-NLS-1$
	private static String MSG_SWTError = WorkbenchMessages.getString("FatalError_SWTError"); //$NON-NLS-1$
	private static String MSG_FATAL_ERROR = WorkbenchMessages.getString("FatalError"); //$NON-NLS-1$

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
			//Avoid recurcive error.
			if(t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw (Error)t;
		}
		if(t instanceof ThreadDeath) {
			// Don't catch ThreadDeath as this is a normal occurrence when the thread dies
			throw (ThreadDeath)t;
		} else {
			log(t);
			if(openQuestionDialog(t))
				closeWorkbench();
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
		workbench.close();
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
		WorkbenchPlugin.log(MSG_UNHANDLED_EXCEPTION, new Status(IStatus.ERROR, IWorkbenchConstants.PLUGIN_ID, 0, msg, t));
	
		// special case for SWTException and SWTError to handle workaround for bug 6312
		Throwable nested = null;
		if (t instanceof SWTException)
			nested = ((SWTException)t).throwable;
		else if(t instanceof SWTError)
			nested = ((SWTError)t).throwable;	
		if (nested != null) {
			msg = nested.getMessage() == null ? nested.toString() : nested.getMessage();
			WorkbenchPlugin.log("*** Stack trace of contained exception ***", new Status(IStatus.ERROR, IWorkbenchConstants.PLUGIN_ID, 0, msg, nested)); //$NON-NLS-1$
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
private boolean openQuestionDialog(Throwable t) {
	try {
		String msg = null;
		if(t instanceof OutOfMemoryError) {
			msg = MSG_OutOfMemoryError;
		} else if(t instanceof StackOverflowError) {
			msg = MSG_StackOverflowError;
		} else if(t instanceof VirtualMachineError) {
			msg = MSG_VirtualMachineError;
		} else if(t instanceof SWTError) {
			msg = MSG_SWTError;
		} else {
			if (t.getMessage() == null) {
				msg = WorkbenchMessages.getString("InternalErrorNoArg");  //$NON-NLS-1$
			} else {
				msg = WorkbenchMessages.format("InternalErrorOneArg", new Object[] {t.getMessage()}); //$NON-NLS-1$
			} 
			return InternalErrorDialog.openQuestion(null, WorkbenchMessages.getString("Internal_error"), msg,t,1); //$NON-NLS-1$
	    }	
		return InternalErrorDialog.openQuestion(null, WorkbenchMessages.getString("Internal_error"), msg + MSG_FATAL_ERROR,t,1);
	} catch (Throwable th) {
		/* It may not be possible to show the inform the user about this exception we may not 
		 * have more memory or OS handles etc. */
		System.err.println("A fatal error happened while informing the user about a fatal error."); //$NON-NLS-1$
		t.printStackTrace();
		System.err.println("New exception."); //$NON-NLS-1$
		th.printStackTrace();
		return true;
	}	
}
}
