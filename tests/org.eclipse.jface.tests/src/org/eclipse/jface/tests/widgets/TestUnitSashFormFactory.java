/*******************************************************************************
 * Copyright (c) 2019 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.widgets.SashFormFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.junit.Test;

public class TestUnitSashFormFactory extends AbstractFactoryTest {

	@Test
	public void createsSashForm() {
		SashForm sashForm = SashFormFactory.newSashForm(SWT.HORIZONTAL).create(shell);

		assertEquals(shell, sashForm.getParent());
		assertEquals(SWT.HORIZONTAL, sashForm.getStyle() & SWT.HORIZONTAL);
	}

	@Test
	public void setsSashWidth() {
		SashForm sashForm = SashFormFactory.newSashForm(SWT.HORIZONTAL).sashWidth(5).create(shell);
		assertEquals(5, sashForm.getSashWidth());
	}
}