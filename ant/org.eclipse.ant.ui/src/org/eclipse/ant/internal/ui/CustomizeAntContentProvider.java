/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ant.internal.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.*;

/**
 * Maintains a generic list of objects that are shown in a table viewer.
 */
public class CustomizeAntContentProvider implements IStructuredContentProvider {
	protected final ArrayList elements = new ArrayList();
	protected TableViewer viewer;
	
void add(Object o) {
	elements.add(o);
	viewer.add(o);
}
public void dispose() {
}
public Object[] getElements(Object inputElement) {
	return (Object[]) elements.toArray(new Object[elements.size()]);
}
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	this.viewer = (TableViewer)viewer;
	elements.clear();
	if (newInput != null)
		elements.addAll((List)newInput);
}
void remove(Object o) {
	elements.remove(o);
	viewer.remove(o);
}
}
