/*******************************************************************************
 * Copyright (c) 2021 SAP SE and others.
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
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.widgets.GroupFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.junit.Test;

public class TestUnitGroupFactory extends AbstractFactoryTest {

	@Test
	public void createsGroup() {
		Group Group = GroupFactory.newGroup(SWT.SHADOW_NONE).create(shell);

		assertEquals(shell, Group.getParent());
		assertEquals(SWT.SHADOW_NONE, Group.getStyle() & SWT.SHADOW_NONE);
	}

	@Test
	public void createsGroupWithText() {
		Group Group = GroupFactory.newGroup(SWT.NONE).text("Test Group").create(shell);

		assertEquals("Test Group", Group.getText());
	}
}