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
import java.util.*;

public class WorkbenchStatusList extends WorkbenchAdapter implements IAdaptable {
	private ArrayList statii = new ArrayList(10);
public void add(IStatus status) {
	statii.add(new WorkbenchStatus(status));
}
public void clear() {
	statii.clear();
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
	return statii.toArray();
}
public void remove(WorkbenchStatus status) {
	statii.remove(status);
}
}
