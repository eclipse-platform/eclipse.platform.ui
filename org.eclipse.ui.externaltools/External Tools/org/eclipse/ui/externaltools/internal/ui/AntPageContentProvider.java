package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.*;

import org.eclipse.jface.viewers.*;

/**
 * Maintains a generic list of objects that are shown in a table viewer.
 */
public class AntPageContentProvider implements IStructuredContentProvider {
	private final ArrayList elements = new ArrayList();
	private TableViewer viewer;

	/*package*/ void add(Object o) {
		elements.add(o);
		viewer.add(o);
	}
	public void dispose() {
	}
	public Object[] getElements(Object inputElement) {
		return (Object[]) elements.toArray(new Object[elements.size()]);
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		elements.clear();
		if (newInput != null)
			elements.addAll((List) newInput);
	}
	/*package*/ void remove(Object o) {
		elements.remove(o);
		viewer.remove(o);
	}
}