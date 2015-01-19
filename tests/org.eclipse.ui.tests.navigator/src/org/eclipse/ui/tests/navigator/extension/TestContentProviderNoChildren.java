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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

public class TestContentProviderNoChildren implements IPipelinedTreeContentProvider2 {

	
	public static boolean _hasChildrenTrue;
	
	@Override
	public Object[] getChildren(Object parentElement) {
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		throw new RuntimeException("Should not be called");
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void init(ICommonContentExtensionSite config) {

	}

	@Override
	public void restoreState(IMemento memento) {

	}

	@Override
	public void saveState(IMemento memento) {

	}

	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		theCurrentChildren.clear();
	}

	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		theCurrentElements.clear();
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		return null;
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
		return null;
	}

	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
		return null;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
		return false;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		return false;
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		if (_hasChildrenTrue)
			return true;
		return false;
	}

}
