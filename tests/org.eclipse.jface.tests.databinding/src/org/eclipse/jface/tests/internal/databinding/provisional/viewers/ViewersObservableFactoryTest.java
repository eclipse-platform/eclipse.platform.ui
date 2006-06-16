/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.provisional.viewers;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValue;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersProperties;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for ViewerObservableFactory.
 * 
 * @since 1.1
 */
public class ViewersObservableFactoryTest extends TestCase {
	private Shell shell;
	private ViewersObservableFactory factory;
	
	protected void setUp() throws Exception {
		shell = new Shell();
		factory = new ViewersObservableFactory();
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
	}
	
	public void testGetObservableForSelectionProvider() throws Exception {
		ISelectionProvider selectionProvider = new SelectionProviderStub();
		IObservable observable = factory.createObservable(new Property(selectionProvider, ViewersProperties.SINGLE_SELECTION));
		
		assertNotNull(observable);
		assertTrue(observable instanceof SelectionProviderSingleSelectionObservableValue);
	}
	
	public void testGetObservableForTableViewer() throws Exception {		
		TableViewer viewer = new TableViewer(shell);
		IObservable observable = factory.createObservable(new Property(viewer, ViewersProperties.SINGLE_SELECTION));
		
		assertNotNull(observable);
		assertTrue(observable instanceof SelectionProviderSingleSelectionObservableValue);
	}
	
	public void testGetObservableForListViewer() throws Exception {
		ListViewer viewer = new ListViewer(shell);
		IObservable observable = factory.createObservable(new Property(viewer, ViewersProperties.SINGLE_SELECTION));
		
		assertNotNull(observable);
		assertTrue(observable instanceof SelectionProviderSingleSelectionObservableValue);
	}
	
	/**
	 * Empty stub to satisfy the requirement that we have a type of ISelectionProvider that is not a viewer.
	 */
	private class SelectionProviderStub implements ISelectionProvider {
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public ISelection getSelection() {
			return null;
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public void setSelection(ISelection selection) {
		}		
	}
}
