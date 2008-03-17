/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 ******************************************************************************/
package org.eclipse.jface.tests.databinding.viewers;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class ObservableListContentProviderTest extends
		AbstractDefaultRealmTestCase {
	private Shell shell;
	private TableViewer viewer;
	private ObservableListContentProvider contentProvider;
	private IObservableList input;

	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);

		contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		input = new WritableList();
		viewer.setInput(input);
	}

	protected void tearDown() throws Exception {
		shell.dispose();
		viewer = null;
		input = null;
		super.tearDown();
	}

	public void testViewerUpdate_RemoveElementAfterMutation() {
		Mutable element = new Mutable(1);
		input.add(element);

		assertEquals(1, viewer.getTable().getItemCount());

		element.id++;
		input.remove(element);

		assertEquals(0, viewer.getTable().getItemCount());
	}

	static class Mutable {
		public int id;

		public Mutable(int id) {
			this.id = id;
		}

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

		public int hashCode() {
			return id;
		}
	}
}