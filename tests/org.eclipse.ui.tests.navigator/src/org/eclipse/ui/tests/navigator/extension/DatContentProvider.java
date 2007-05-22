/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DatContentProvider implements ITreeContentProvider {
	
	private TestExtensionTreeData child = new TestExtensionTreeData(null, "Child", null, null);

	public Object[] getChildren(Object parentElement) { 
		return new Object[] { child } ;
	}

	public Object getParent(Object element) { 
		return null;
	}

	public boolean hasChildren(Object element) { 
		return false;
	}

	public Object[] getElements(Object inputElement) { 
		return null;
	}

	public void dispose() { 

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 

	}

}
