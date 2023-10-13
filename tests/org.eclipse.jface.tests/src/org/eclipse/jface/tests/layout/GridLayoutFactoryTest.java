/*******************************************************************************
 * Copyright (c) 2016 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.layout;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.junit.Test;

/**
 * @since 3.3
 */
public class GridLayoutFactoryTest {
	@Test
	public void testToStringAll() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.numColumns(3)
			.equalWidth(true)
			.extendedMargins(1, 2, 3, 4)
			.margins(30, 50)
			.spacing(39, 59);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .numColumns(3)\n    .equalWidth(true)\n    .extendedMargins(1, 2, 3, 4)\n    .margins(30, 50)\n    .spacing(39, 59)\n",
				factory.toString());
	}

	@Test
	public void testToStringNumColumns() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.numColumns(3);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .numColumns(3)\n",
				factory.toString());
	}

	@Test
	public void testToStringEqualWidth() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.equalWidth(true);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .equalWidth(true)\n",
				factory.toString());
	}

	@Test
	public void testToStringExtendedMargins() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.extendedMargins(10, 20, 30, 40);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .extendedMargins(10, 20, 30, 40)\n",
				factory.toString());
	}

	@Test
	public void testToStringMargins() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.margins(30, 50);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .margins(30, 50)\n",
				factory.toString());
	}

	@Test
	public void testToStringSpacing() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.spacing(39, 59);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .spacing(39, 59)\n",
				factory.toString());
	}

	@Test
	public void testToStringNoOverrides() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults();

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n",
				factory.toString());
	}
}
