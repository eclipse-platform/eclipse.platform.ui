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
package org.eclipse.ui.internal.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * UI manfestation of a status object.
 */
public class WorkbenchStatus extends WorkbenchAdapter implements IAdaptable {
	private IStatus status;
	private Object[] children;
public WorkbenchStatus(IStatus status) {
	this.status = status;
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) return this;
	return null;
}
/**
 * Returns the children of this element.
 */
public Object[] getChildren(Object o) {
	if (children == null) {
		IStatus[] childStatii = status.getChildren();
		children = new Object[childStatii.length];
		for (int i = 0; i < childStatii.length; i++) {
			children[i] = new WorkbenchStatus(childStatii[i]);
		}
	}
	return children;
}
/**
 * @see IWorkbenchAdapter#getLabel
 */
public String getLabel(Object o) {
	return status.getMessage();
}
/**
 * Returns the wrapped status object.
 */
public IStatus getStatus() {
	return status;
}
}
