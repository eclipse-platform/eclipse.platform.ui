/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.*;
import java.util.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
		for (int i = 0; i < children.length; i++){
			IStatus currentChild = children[i];
			if (currentChild.isMultiStatus()) {
				Iterator childStatiiEnum = flatten(currentChild).iterator();
				while (childStatiiEnum.hasNext())
					result.add(childStatiiEnum.next());
			} else
				result.add(currentChild);
		}
	} else
		result.add(aStatus);
	
	return result;
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 */
protected static IStatus newStatus(
		IStatus[] stati, 
		String message, 
		Throwable exception) {

	Assert.isTrue(message != null);
	Assert.isTrue(message.trim().length() != 0);

	return new MultiStatus(WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, 
		stati, message, exception);
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 */
public static IStatus newStatus(
	int severity, 
	String message, 
	Throwable exception) {

	String statusMessage = message;
	if(message == null || message.trim().length() == 0){
		if(exception.getMessage() == null)
			statusMessage = exception.toString();
		else
			statusMessage = exception.getMessage();
	}

	return new Status(severity, WorkbenchPlugin.PI_WORKBENCH, severity, statusMessage, exception);
}
/**
 * This method must not be called outside the workbench.
 *
 * Utility method for creating status.
 */
public static IStatus newStatus(
		List children, 
		String message, 
		Throwable exception) {
	
	List flatStatusCollection = new ArrayList();
	Iterator iter = children.iterator();
	while (iter.hasNext()) {
		IStatus currentStatus = (IStatus)iter.next();
		Iterator childrenIter = flatten(currentStatus).iterator();
		while (childrenIter.hasNext())
			flatStatusCollection.add(childrenIter.next());
	}

	IStatus[] stati = new IStatus[flatStatusCollection.size()];
	flatStatusCollection.toArray(stati);
	return newStatus(stati, message, exception);
}
}
