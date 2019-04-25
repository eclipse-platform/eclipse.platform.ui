/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 206839)
 *     Matthew Hall - bug 194734
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.IDecoratingObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.internal.databinding.viewers.ViewerInputProperty;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ViewersObservables
 *
 * @since 1.2
 */
public class ViewersObservablesTest extends AbstractDefaultRealmTestCase {
	TableViewer viewer;
	Realm realm;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		realm = DisplayRealm.getRealm(Display.getCurrent());
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		Shell shell = viewer.getTable().getShell();
		if (!shell.isDisposed())
			shell.dispose();
		shell = null;
		realm = null;
		super.tearDown();
	}

	@Test
	public void testObserveInput_InstanceOfViewerInputObservableValue() {
		// TODO j: It is very weird to be forced to cast to an unrelated viewer here
		IViewerObservableValue<Object> observable = ViewerProperties.<TreeViewer, Object>input().observe(viewer);
		assertTrue(observable.getViewer() == viewer);
		IPropertyObservable<?> propertyObservable = (IPropertyObservable<?>) ((IDecoratingObservable) observable)
				.getDecorated();
		assertTrue(propertyObservable.getProperty() instanceof ViewerInputProperty);
	}
}
