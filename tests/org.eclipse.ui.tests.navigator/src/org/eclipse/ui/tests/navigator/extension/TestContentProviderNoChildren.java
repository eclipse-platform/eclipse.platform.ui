/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

public class TestContentProviderNoChildren implements ITreeContentProvider {
	   
	public Object[] getChildren(Object parentElement) { 
		return new Object[] { } ;
	}

	public Object getParent(Object element) { 
		return null;
	}

	public boolean hasChildren(Object element) { 
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) { 
		return null;
	}

	public void dispose() { 
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 
	}

	public void init(ICommonContentExtensionSite config) {

	}

	public void restoreState(IMemento memento) {
		
	}

	public void saveState(IMemento memento) {
		
	}

}
