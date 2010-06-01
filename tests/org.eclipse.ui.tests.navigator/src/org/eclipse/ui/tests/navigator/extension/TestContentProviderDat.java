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

import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

public class TestContentProviderDat implements ITreeContentProvider {
	   
	private TestExtensionTreeData child = new TestExtensionTreeData(null, "Child", null, null);

	public Object[] getChildren(Object parentElement) { 
		return new Object[] { child } ;
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

	//
	// IPipelinedTreeContentProvider (not currently implemented)
	//
	
	public void getPipelinedChildren(Object parent, Set theCurrentChildren) {
	}

	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
	}

	public Object getPipelinedParent(Object anObject, Object suggestedParent) {
		return null;
	}

	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) {
		return null;
	}

	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		return false;
	}

	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification removeModification) {
		return null;
	}

	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		return false;
	}

	public void init(ICommonContentExtensionSite config) {

	}

	public void restoreState(IMemento memento) {
		
	}

	public void saveState(IMemento memento) {
		
	}

}
