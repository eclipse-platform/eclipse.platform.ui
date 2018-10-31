/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 ******************************************************************************/
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.junit.Test;

public class TestUnitLabelFactory extends AbstractFactoryTest {

	@Test
	public void createsLabel() {
		Label label = LabelFactory.newLabel(SWT.WRAP).create(shell);

		assertEquals(shell, label.getParent());
		assertEquals(SWT.WRAP, label.getStyle() & SWT.WRAP);
	}

	@Test
	public void createsLabelWithAllProperties() {
		Label label = LabelFactory.newLabel(SWT.NONE).text("Test Label").image(image).align(SWT.RIGHT).create(shell);

		assertEquals("Test Label", label.getText());
		assertEquals(image, label.getImage());
		assertEquals(SWT.RIGHT, label.getAlignment() & SWT.RIGHT);
	}
}
