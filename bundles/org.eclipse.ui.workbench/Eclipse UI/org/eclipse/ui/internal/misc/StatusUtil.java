/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class to create status objects.
 *
 * @private - This class is an internal implementation class and should
 * not be referenced or subclassed outside of the workbench
 */
public class StatusUtil {
    /**
     *	Answer a flat collection of the passed status and its recursive children
     */
    protected static List flatten(IStatus aStatus) {
        List result = new ArrayList();

        if (aStatus.isMultiStatus()) {
            IStatus[] children = aStatus.getChildren();
            for (int i = 0; i < children.length; i++) {
                IStatus currentChild = children[i];
                if (currentChild.isMultiStatus()) {
                    Iterator childStatiiEnum = flatten(currentChild).iterator();
                    while (childStatiiEnum.hasNext()) {
						result.add(childStatiiEnum.next());
					}
                } else {
					result.add(currentChild);
				}
            }
        } else {
			result.add(aStatus);
		}

        return result;
    }

    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for creating status.
     */
    protected static IStatus newStatus(IStatus[] stati, String message,
            Throwable exception) {

        Assert.isTrue(message != null);
        Assert.isTrue(message.trim().length() != 0);

        return new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR,
                stati, message, exception);
    }
    
    public static IStatus newStatus(String pluginId, Throwable exception) {
        return newStatus(pluginId, getLocalizedMessage(exception), exception);
    }

    /**
     * Returns a localized message describing the given exception. If the given exception does not
     * have a localized message, this returns the string "An error occurred".
     *
     * @param exception
     * @return
     */
    public static String getLocalizedMessage(Throwable exception) {
        String message = exception.getLocalizedMessage();
        
        if (message != null) {
            return message;
        }
        
        // Workaround for the fact that CoreException does not implement a getLocalizedMessage() method.
        // Remove this branch when and if CoreException implements getLocalizedMessage() 
        if (exception instanceof CoreException) {
            CoreException ce = (CoreException)exception;
            return ce.getStatus().getMessage();
        }
        
        return WorkbenchMessages.StatusUtil_errorOccurred;
    }
    
    /**
     * Creates a new Status based on the original status, but with a different message
     *
     * @param originalStatus
     * @param newMessage
     * @return
     */
    public static IStatus newStatus(IStatus originalStatus, String newMessage) {
        return new Status(originalStatus.getSeverity(), 
                originalStatus.getPlugin(), originalStatus.getCode(), newMessage, originalStatus.getException());
    }
    
    public static IStatus newStatus(String pluginId, String message, Throwable exception) {        
        return new Status(IStatus.ERROR, pluginId, IStatus.OK, 
                message, getCause(exception));
    }    
    
    public static Throwable getCause(Throwable exception) {
        // Figure out which exception should actually be logged -- if the given exception is
        // a wrapper, unwrap it
        Throwable cause = null;
        if (exception != null) {
            if (exception instanceof CoreException) {
                // Workaround: CoreException contains a cause, but does not actually implement getCause(). 
                // If we get a CoreException, we need to manually unpack the cause. Otherwise, use
                // the general-purpose mechanism. Remove this branch if CoreException ever implements
                // a correct getCause() method.
                CoreException ce = (CoreException)exception;
                cause = ce.getStatus().getException();
            } else {
            	// use reflect instead of a direct call to getCause(), to allow compilation against JCL Foundation (bug 80053)
            	try {
            		Method causeMethod = exception.getClass().getMethod("getCause", new Class[0]); //$NON-NLS-1$
            		Object o = causeMethod.invoke(exception, new Object[0]);
            		if (o instanceof Throwable) { 
            			cause = (Throwable) o;
            		}
            	}
            	catch (NoSuchMethodException e) {
            		// ignore
            	} catch (IllegalArgumentException e) {
            		// ignore
				} catch (IllegalAccessException e) {
            		// ignore
				} catch (InvocationTargetException e) {
            		// ignore
				}
            }
            
            if (cause == null) {
                cause = exception;
            }
        }

        return cause;
    }
        
    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for creating status.
     */
    public static IStatus newStatus(int severity, String message,
            Throwable exception) {

        String statusMessage = message;
        if (message == null || message.trim().length() == 0) {
            if (exception.getMessage() == null) {
				statusMessage = exception.toString();
			} else {
				statusMessage = exception.getMessage();
			}
        }

        return new Status(severity, WorkbenchPlugin.PI_WORKBENCH, severity,
                statusMessage, getCause(exception));
    }

    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for creating status.
     */
    public static IStatus newStatus(List children, String message,
            Throwable exception) {

        List flatStatusCollection = new ArrayList();
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            IStatus currentStatus = (IStatus) iter.next();
            Iterator childrenIter = flatten(currentStatus).iterator();
            while (childrenIter.hasNext()) {
				flatStatusCollection.add(childrenIter.next());
			}
        }

        IStatus[] stati = new IStatus[flatStatusCollection.size()];
        flatStatusCollection.toArray(stati);
        return newStatus(stati, message, exception);
    }
    
    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
    public static void handleStatus(IStatus status, int hint, Shell shell) {
    	StatusManager.getManager().handle(status, hint);
	}
    
    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
    public static void handleStatus(Throwable e, int hint) {
		StatusManager.getManager().handle(
				newStatus(WorkbenchPlugin.PI_WORKBENCH, e), hint);
	}
    
    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(String message, Throwable e, int hint) {
		StatusManager.getManager().handle(
				newStatus(WorkbenchPlugin.PI_WORKBENCH, message, e), hint);
	}
	
	/**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(String message, Throwable e, int hint,
			Shell shell) {
		StatusManager.getManager().handle(
				newStatus(WorkbenchPlugin.PI_WORKBENCH, message, e), hint);
	}
	
	/**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(IStatus status, String message, int hint) {
		StatusManager.getManager().handle(newStatus(status, message), hint);
	}
	
	/**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(IStatus status, String message, int hint,
			Shell shell) {
		StatusManager.getManager().handle(newStatus(status, message), hint);
	}

	/**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(String message, int hint) {
		handleStatus(message, null, hint);
	}
	
	/**
     * This method must not be called outside the workbench.
     *
     * Utility method for handling status.
     */
	public static void handleStatus(String message, int hint, Shell shell) {
		handleStatus(message, null, hint);
	}
}
