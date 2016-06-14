/*******************************************************************************
 * Copyright (c) 2016 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.layout;

import org.eclipse.jface.layout.GridLayoutFactory;

import junit.framework.TestCase;

/**
 * @since 3.3
 */
public class GridLayoutFactoryTest extends TestCase {
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

	public void testToStringNumColumns() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.numColumns(3);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .numColumns(3)\n",
				factory.toString());
	}

	public void testToStringEqualWidth() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.equalWidth(true);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .equalWidth(true)\n",
				factory.toString());
	}

	public void testToStringExtendedMargins() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.extendedMargins(10, 20, 30, 40);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .extendedMargins(10, 20, 30, 40)\n",
				factory.toString());
	}

	public void testToStringMargins() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.margins(30, 50);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .margins(30, 50)\n",
				factory.toString());
	}

	public void testToStringSpacing() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults()
			.spacing(39, 59);

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n    .spacing(39, 59)\n",
				factory.toString());
	}

	public void testToStringNoOverrides() {
		GridLayoutFactory factory = GridLayoutFactory.fillDefaults();

		assertEquals(
				"GridLayoutFactory.fillDefaults()\n",
				factory.toString());
	}
}
