/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bug 263956
 ******************************************************************************/
package org.eclipse.jface.tests.databinding.viewers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ObservableListContentProviderTest extends AbstractDefaultRealmTestCase {
	private Shell shell;
	private TableViewer viewer;
	private ObservableListContentProvider contentProvider;
	private IObservableList input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);

		contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		input = new WritableList();
		viewer.setInput(input);
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		viewer = null;
		input = null;
		super.tearDown();
	}

	public void testKnownElements_Realm() throws Exception {
		assertSame("realm for the known elements should be the SWT realm", DisplayRealm.getRealm(Display.getDefault()),
				contentProvider.getKnownElements().getRealm());
	}

	public void testRealizedElements_Realm() {
		assertSame("realm for the realized elements should be the SWT realm",
				DisplayRealm.getRealm(Display.getDefault()), contentProvider.getRealizedElements().getRealm());
	}

	public void testKnownElementsAfterSetInput() {
		assertEquals(0, contentProvider.getKnownElements().size());
		Set<String> newElements = new HashSet<String>(Arrays.asList(new String[] { "one", "two", "three" }));
		WritableList newInput = new WritableList();
		newInput.addAll(newElements);
		viewer.setInput(newInput);
		assertEquals(newElements, contentProvider.getKnownElements());
	}

	public void testViewerUpdate_RemoveElementAfterMutation() {
		Mutable element = new Mutable(1);
		input.add(element);

		assertEquals(1, viewer.getTable().getItemCount());

		element.id++;
		input.clear();

		assertEquals(0, viewer.getTable().getItemCount());
	}

	public void testInputChanged_ClearsKnownElements() {
		Object element = new Object();
		input.add(element);

		IObservableSet knownElements = contentProvider.getKnownElements();
		assertEquals(Collections.singleton(element), knownElements);
		viewer.setInput(Observables.emptyObservableList());
		assertEquals(Collections.EMPTY_SET, knownElements);
	}

	public void testInputChanged_ClearsRealizedElements() {
		// Realized elements must be allocated before adding the element
		// otherwise we'd have to spin the event loop to see the new element
		IObservableSet realizedElements = contentProvider.getRealizedElements();

		Object element = new Object();
		input.add(element);

		assertEquals(Collections.singleton(element), realizedElements);
		viewer.setInput(Observables.emptyObservableList());
		assertEquals(Collections.EMPTY_SET, realizedElements);
	}

	static class Mutable {
		public int id;

		public Mutable(int id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Mutable that = (Mutable) obj;
			return this.id == that.id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}
}