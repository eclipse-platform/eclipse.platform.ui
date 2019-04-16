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
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public class TestUnitCompositeFactory extends AbstractFactoryTest {
	@Test
	public void createsComposite() {
		Composite composite = CompositeFactory.newComposite(SWT.MULTI).create(shell);

		assertEquals(shell, composite.getParent());
		assertEquals(SWT.MULTI, composite.getStyle() & SWT.MULTI);
	}

	@Test
	public void createCompositeWithAllProperties() {
		GridLayout gridLayout = new GridLayout();
		Composite composite = CompositeFactory.newComposite(SWT.NONE).layout(gridLayout).create(shell);

		assertEquals(gridLayout, composite.getLayout());
	}
}