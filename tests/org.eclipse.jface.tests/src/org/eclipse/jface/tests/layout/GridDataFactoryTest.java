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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.junit.Test;

/**
 * @since 3.5
 */
public class GridDataFactoryTest {
	@Test
	public void testToStringWithAllOverrides() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.grab(true, false)
				.align(SWT.LEFT, SWT.FILL)
				.indent(15, 23)
				.span(1, 2)
				.minSize(20, 30)
				.hint(100, SWT.DEFAULT);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .grab(true, false)\n    .align(SWT.LEFT, SWT.FILL)\n    .indent(15, 23)\n    .span(1, 2)\n    .minSize(20, 30)\n    .hint(100, SWT.DEFAULT)\n",
				factory.toString());
	}

	@Test
	public void testToStringGrab() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.grab(false, true);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .grab(false, true)\n",
				factory.toString());
	}

	@Test
	public void testToStringAlign() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.BOTTOM);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .align(SWT.FILL, SWT.BOTTOM)\n",
				factory.toString());
	}

	@Test
	public void testToStringIndent() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.indent(10, 39);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .indent(10, 39)\n",
				factory.toString());
	}

	@Test
	public void testToStringSpan() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.span(2, 3);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .span(2, 3)\n",
				factory.toString());
	}

	@Test
	public void testToStringMinSize() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.minSize(30, 10);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .minSize(30, 10)\n",
				factory.toString());
	}

	@Test
	public void testToStringHint() {
		GridDataFactory factory = GridDataFactory.fillDefaults()
				.hint(SWT.DEFAULT, 310);

		assertEquals(
				"GridDataFactory.fillDefaults()\n    .hint(SWT.DEFAULT, 310)\n",
				factory.toString());
	}

	@Test
	public void testToNoOverrides() {
		GridDataFactory factory = GridDataFactory.fillDefaults();

		assertEquals(
				"GridDataFactory.fillDefaults()\n",
				factory.toString());
	}

	@Test
	public void testGridDataCreate() {
		GridData actual = GridDataFactory.create(GridData.FILL_HORIZONTAL).create();
		GridData expected = new GridData(GridData.FILL_HORIZONTAL);
		// equals is not implemented in GridData. Use toString() to compare both
		assertEquals(expected.toString(), actual.toString());
	}
}
