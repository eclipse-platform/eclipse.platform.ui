/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * The RefreshTestContentProvider is the content
 * provider for refresh tests.
 */
public class RefreshTestContentProvider implements IStructuredContentProvider {

	static int seed = 1;
	static TestElement[] allElements;
	public static int ELEMENT_COUNT = 10000;
	TestElement[] currentElements;

	static{
		allElements = new TestElement[ELEMENT_COUNT];
		for (int i = 0; i < ELEMENT_COUNT; i++) {
			allElements[i] = new TestElement(i);
		}
	}

	void preSortElements(Viewer viewer, ViewerComparator sorter) {
		sorter.sort(viewer,currentElements);

	}

	public RefreshTestContentProvider(int size){
		Assert.isTrue(size <= ELEMENT_COUNT);
		setSize(size);
	}

	/**
	 * Set the size of the amount we are currently displaying
	 * to size.
	 */
	public void setSize(int size) {

		currentElements = new TestElement[size];
		System.arraycopy(allElements, 0, currentElements, 0, currentElements.length);

	}

	@Override
	public Object[] getElements(Object inputElement) {
		return currentElements;
	}

	@Override
	public void dispose() {
		currentElements = null;

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//Do nothing here
	}

	/**
	 * Restore the elements to thier old poorly sorted
	 * state.
	 */
	public void refreshElements() {
		for (int i = 0; i < ELEMENT_COUNT; i++) {
			currentElements[i] = new TestElement(i + seed);
		}
		seed += 257;


	}

	public void cloneElements(){
		currentElements = currentElements.clone();
	}

}
