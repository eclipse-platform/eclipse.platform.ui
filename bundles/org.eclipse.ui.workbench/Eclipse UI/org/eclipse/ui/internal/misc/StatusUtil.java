/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.ui.internal.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class to create status objects.
 *
 * @private - This class is an internal implementation class and should not be
 *          referenced or subclassed outside of the workbench
 */
public class StatusUtil {
	/**
	 * Answer a flat collection of the passed status and its recursive children
	 */
	protected static List<IStatus> flatten(IStatus aStatus) {
		List<IStatus> result = new ArrayList<>();

		if (aStatus.isMultiStatus()) {
			for (IStatus status : aStatus.getChildren()) {
				if (status.isMultiStatus()) {
					Iterator<IStatus> childStatiiEnum = flatten(status).iterator();
					while (childStatiiEnum.hasNext()) {
						result.add(childStatiiEnum.next());
					}
				} else {
					result.add(status);
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
	protected static IStatus newStatus(IStatus[] stati, String message, Throwable exception) {

		Assert.isTrue(message != null);
		Assert.isTrue(message.trim().length() != 0);

		return new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, stati, message, exception);
	}

	public static IStatus newStatus(String pluginId, Throwable exception) {
		return newStatus(pluginId, getLocalizedMessage(exception), exception);
	}

	/**
	 * Returns a localized message describing the given exception. If the given
	 * exception does not have a localized message, this returns the string "An
	 * error occurred".
	 *
	 * @param exception
	 * @return
	 */
	public static String getLocalizedMessage(Throwable exception) {
		String message = exception.getLocalizedMessage();

		if (message != null) {
			return message;
		}

		// Workaround for the fact that CoreException does not implement a
		// getLocalizedMessage() method.
		// Remove this branch when and if CoreException implements getLocalizedMessage()
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException) exception;
			return ce.getStatus().getMessage();
		}

		return WorkbenchMessages.StatusUtil_errorOccurred;
	}

	/**
	 * Creates a new Status based on the original status, but with a different
	 * message
	 *
	 * @param originalStatus
	 * @param newMessage
	 * @return
	 */
	public static IStatus newStatus(IStatus originalStatus, String newMessage) {
		return new Status(originalStatus.getSeverity(), originalStatus.getPlugin(), originalStatus.getCode(),
				newMessage, originalStatus.getException());
	}

	public static IStatus newStatus(String pluginId, String message, Throwable exception) {
		return new Status(IStatus.ERROR, pluginId, IStatus.OK, message, exception);
	}

	/**
	 * This method must not be called outside the workbench.
	 *
	 * Utility method for creating status.
	 */
	public static IStatus newStatus(int severity, String message, Throwable exception) {

		String statusMessage = message;
		if (message == null || message.trim().isEmpty()) {
			if (exception.getMessage() == null) {
				statusMessage = exception.toString();
			} else {
				statusMessage = exception.getMessage();
			}
		}

		return new Status(severity, WorkbenchPlugin.PI_WORKBENCH, severity, statusMessage, exception);
	}

	/**
	 * This method must not be called outside the workbench.
	 *
	 * Utility method for creating status.
	 */
	public static IStatus newStatus(List<IStatus> children, String message, Throwable exception) {

		List<IStatus> flatStatusCollection = new ArrayList<>();
		Iterator<IStatus> iter = children.iterator();
		while (iter.hasNext()) {
			IStatus currentStatus = iter.next();
			Iterator<IStatus> childrenIter = flatten(currentStatus).iterator();
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
	public static void handleStatus(IStatus status, int hint) {
		StatusManager.getManager().handle(status, hint);
	}

	/**
	 * This method must not be called outside the workbench.
	 *
	 * Utility method for handling status.
	 */
	public static void handleStatus(Throwable e, int hint) {
		StatusManager.getManager().handle(newStatus(WorkbenchPlugin.PI_WORKBENCH, e), hint);
	}

	/**
	 * This method must not be called outside the workbench.
	 *
	 * Utility method for handling status.
	 */
	public static void handleStatus(String message, Throwable e, int hint) {
		StatusManager.getManager().handle(newStatus(WorkbenchPlugin.PI_WORKBENCH, message, e), hint);
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
	public static void handleStatus(String message, int hint) {
		handleStatus(message, null, hint);
	}

}
