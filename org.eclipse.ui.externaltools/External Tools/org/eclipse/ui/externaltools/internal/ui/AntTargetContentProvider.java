package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.externaltools.internal.core.AntTargetList;

/**
 * Content provider for targets within an Ant file
 */
public class AntTargetContentProvider implements IStructuredContentProvider {
	/**
	 * Creates a default instance of the content provider.
	 */
	public AntTargetContentProvider() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IStructuredContentProvider.
	 */
	public Object[] getElements(Object input) {
		AntTargetList targetList = (AntTargetList) input;
		return targetList.getTargets();
	}

	/* (non-Javadoc)
	 * Method declared on IContentProvider.
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}