/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

/**
 * Base class that sets the default realm to be the SWT realm.
 *
 * @since 3.3
 */
public abstract class AbstractDefaultRealmTestCase {
	private Realm previousRealm;

	@Rule
	public BindingTestSetup testSetup = new BindingTestSetup();

	/**
	 * Sets the default realm to be the realm for the default display.
	 */
	@Before
	public void setUp() throws Exception {

		previousRealm = Realm.getDefault();

		Display display = Display.getCurrent() != null
				&& !Display.getCurrent().isDisposed() ? Display.getCurrent()
				: Display.getDefault();
		RealmTester.setDefault(DisplayRealm.getRealm(display));
	}

	/**
	 * Runs all currently-enqueued asynchronous events
	 */
	protected void runAsync() {
		Display display = Display.getCurrent();

		while (display.readAndDispatch()) {
		}
	}

	/**
	 * Removes the default realm.
	 */
	@After
	public void tearDown() throws Exception {

		RealmTester.setDefault(previousRealm);
	}
}
