/*******************************************************************************
 * Copyright (c) 2009, 2010 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Oakland Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.util.Set;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionContentProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

public class TestContentProviderPipelined extends ResourceExtensionContentProvider implements
		IPipelinedTreeContentProvider2 {

	public static boolean _throw;

	public static void resetTest() {
		_throw = false;
	}

	public TestContentProviderPipelined() {
	}

	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		if (_throw)
			throw new RuntimeException("did not work out");
		System.out.println("parent: " + aParent);
		System.out.println("chilren: " + theCurrentChildren);

	}

	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		if (_throw)
			throw new RuntimeException("did not work out");
		getPipelinedChildren(anInput, theCurrentElements);
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return currentHasChildren;
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return aSuggestedParent;
	}

	@Override
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return anAddModification;
	}

	@Override
	public boolean interceptRefresh(
			PipelinedViewerUpdate aRefreshSynchronization) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return true;
	}

	@Override
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification aRemoveModification) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return aRemoveModification;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		if (_throw)
			throw new RuntimeException("did not work out");
		return false;
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {

	}

	@Override
	public void restoreState(IMemento aMemento) {

	}

	@Override
	public void saveState(IMemento aMemento) {

	}


}
