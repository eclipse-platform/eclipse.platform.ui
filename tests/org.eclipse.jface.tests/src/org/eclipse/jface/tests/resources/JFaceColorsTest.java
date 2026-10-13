/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Test;

public class JFaceColorsTest {

	private static final RGB BLACK = new RGB(0, 0, 0);
	private static final RGB WHITE = new RGB(255, 255, 255);

	@Test
	public void testBlendEndpoints() {
		RGB c1 = new RGB(10, 20, 30);
		RGB c2 = new RGB(200, 150, 100);
		assertEquals(c1, JFaceColors.blend(c1, c2, 1));
		assertEquals(c2, JFaceColors.blend(c1, c2, 0));
	}

	@Test
	public void testBlendHalfway() {
		assertEquals(new RGB(127, 127, 127), JFaceColors.blend(WHITE, BLACK, 0.5));
		assertEquals(new RGB(15, 25, 35), JFaceColors.blend(new RGB(10, 20, 30), new RGB(21, 31, 41), 0.5));
	}

	@Test
	public void testBlendMatchesIntegerPercentage() {
		for (int ratio = 0; ratio <= 100; ratio++) {
			for (int v1 = 0; v1 < 256; v1 += 5) {
				for (int v2 = 0; v2 < 256; v2 += 3) {
					RGB c1 = new RGB(v1, 255 - v1, v1 / 2);
					RGB c2 = new RGB(v2, 255 - v2, v2 / 2);
					RGB expected = new RGB(blend(v1, v2, ratio), blend(255 - v1, 255 - v2, ratio),
							blend(v1 / 2, v2 / 2, ratio));
					assertEquals(expected, JFaceColors.blend(c1, c2, ratio / 100.0));
				}
			}
		}
	}

	@Test
	public void testBlendDoesNotTruncateExactResultsDown() {
		// 0.29 * 100 evaluates to 28.999999999999996 in double arithmetic
		assertEquals(new RGB(29, 29, 29), JFaceColors.blend(new RGB(100, 100, 100), BLACK, 0.29));
	}

	@Test
	public void testBlendClampsOutOfRangeRatio() {
		assertEquals(WHITE, JFaceColors.blend(WHITE, BLACK, 1.5));
		assertEquals(BLACK, JFaceColors.blend(WHITE, BLACK, -0.5));
	}

	private static int blend(int v1, int v2, int percent) {
		return (percent * v1 + (100 - percent) * v2) / 100;
	}
}
