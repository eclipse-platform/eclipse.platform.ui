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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class NameSorter extends ViewerSorter {
	public int compare(Viewer viewer, Object e1, Object e2) {
		return compare((IResource)e1, (IResource)e2);
	}

	protected int compare(IResource resource, IResource resource2) {
		String property1= getProperty(resource);
		String property2= getProperty(resource2);
		return collator.compare(property1, property2);
	}

	protected String getProperty(IResource resource) {
		return resource.getName();
	}
}
