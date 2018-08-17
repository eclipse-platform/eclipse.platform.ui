/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.layout;

import junit.framework.TestCase;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @since 3.3
 *
 */
public class GeometryTest extends TestCase {

	public void testNewGeometryMethods() {
		// Test the new Geometry methods
		Rectangle margins = Geometry.createDiffRectangle(0, 10, 40, 80);

		Rectangle testRectangle = new Rectangle(100, 100, 100, 100);
		Rectangle expandedRectangle = Geometry.add(testRectangle, margins);
		Rectangle expectedResult = new Rectangle(100, 60, 110, 220);

		assertEquals(expectedResult, expandedRectangle);

		Rectangle difference = Geometry.subtract(expandedRectangle,
				testRectangle);

		assertEquals(margins, difference);

		Geometry.expand(testRectangle, margins);
		assertEquals(expectedResult, testRectangle);
	}

}
