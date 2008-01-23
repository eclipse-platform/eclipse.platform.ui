/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 206839)
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.internal.databinding.internal.viewers.ViewerInputObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for ViewersObservables
 * 
 * @since 1.2
 */
public class ViewersObservablesTest extends AbstractDefaultRealmTestCase {
	TableViewer viewer;
	Realm realm;

	protected void setUp() throws Exception {
		super.setUp();
		realm = SWTObservables.getRealm(Display.getCurrent());
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);
	}

	protected void tearDown() throws Exception {
		Shell shell = viewer.getTable().getShell();
		if (!shell.isDisposed())
			shell.dispose();
		shell = null;
		realm = null;
		super.tearDown();
	}

	public void testObserveInput_InstanceOfViewerInputObservableValue() {
		IObservableValue observable = ViewersObservables.observeInput(viewer);
		assertTrue(observable instanceof ViewerInputObservableValue);
	}
}
