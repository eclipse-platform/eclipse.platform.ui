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

package org.eclipse.jface.tests.databinding.swt;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class SWTObservablesTest extends TestCase {
	private Shell shell;

	protected void setUp() {
		shell = new Shell();
		RealmTester.setDefault(SWTObservables.getRealm(shell.getDisplay()));
	}

	protected void tearDown() throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}

		RealmTester.setDefault(null);
	}

	public void testObserveForeground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeForeground(shell);
		assertNotNull(value);
		assertEquals(Color.class, value.getValueType());
	}

	public void testObserveBackground() throws Exception {
		ISWTObservableValue value = SWTObservables.observeBackground(shell);
		assertNotNull(value);
		assertEquals(Color.class, value.getValueType());
	}

	public void testObserveFont() throws Exception {
		ISWTObservableValue value = SWTObservables.observeFont(shell);
		assertNotNull(value);
		assertEquals(Font.class, value.getValueType());
	}
}
