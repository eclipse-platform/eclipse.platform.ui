/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * Base class that sets the default realm to be the SWT realm.
 *
 * @since 3.3
 */
public class AbstractJUnit4RealmTestCase {
	private Realm previousRealm;

	/**
	 * Sets the default realm to be the realm for the default display.
	 *
	 * @see junit.framework.TestCase#setUp()
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
