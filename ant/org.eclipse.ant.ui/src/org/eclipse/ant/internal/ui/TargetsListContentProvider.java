/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.ui;

import org.apache.tools.ant.Project;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the Ant dialog: it provides the ListViewer with the targets defined in the xml file.
 */

public class TargetsListContentProvider implements IStructuredContentProvider {
	
	private static TargetsListContentProvider instance;
	
	static {
		instance = new TargetsListContentProvider();
	}
	
// private to ensure that it remains a singleton
private TargetsListContentProvider() {
	super();
}

public static TargetsListContentProvider getInstance() {
	return instance;
}

/**
 * Returns the targets found in the xml file after parsing.
 * 
 * @param groupName the name of the group
 * @return the array of the targets found
 */
public Object[] getElements(Object inputElement) {
	Project project = (Project)inputElement;
	return project.getTargets().values().toArray();
}

public void dispose() {
}

public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
}

}
