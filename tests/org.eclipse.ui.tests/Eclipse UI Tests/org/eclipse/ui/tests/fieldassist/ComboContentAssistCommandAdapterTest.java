/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM - ongoing development
 ******************************************************************************/
package org.eclipse.ui.tests.fieldassist;

import static org.junit.Assert.assertFalse;

import org.eclipse.jface.tests.fieldassist.AbstractFieldAssistWindow;
import org.eclipse.swt.widgets.Combo;
import org.junit.Test;

public class ComboContentAssistCommandAdapterTest extends
		AbstractContentAssistCommandAdapterTest {

	@Override
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new ComboCommandFieldAssistWindow();
	}

	private Combo getCombo() {
		return (Combo)getFieldAssistWindow().getFieldAssistControl();
	}

	@Test
	public void testBug243612() throws Exception {
		getFieldAssistWindow().open();

		sendFocusInToControl();
		executeContentAssistHandler();

		assertTwoShellsUp();

		assertFalse(getCombo().getListVisible());
	}

}
