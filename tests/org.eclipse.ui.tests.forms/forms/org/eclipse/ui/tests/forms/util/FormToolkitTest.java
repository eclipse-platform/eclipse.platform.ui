/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.Test;

public class FormToolkitTest {

	/*
	 * Verify that calling dispose twice does not cause an NPE
	 */
	@Test
	public void testDispose() {
		Display display = Display.getCurrent();
		FormToolkit toolkit = new FormToolkit(display);
		toolkit.dispose();
		toolkit.dispose();
    }

}
