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
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.eclipse.e4.ui.internal.css.swt.FontWeights;
import org.junit.jupiter.api.Test;

public class FontWeightsTest {

	@Test
	void testLightWeightsTryLighterFacesFirst() {
		assertArrayEquals(new int[] { 1, 2, 3 }, FontWeights.stepsToTry(1));
		assertArrayEquals(new int[] { 2, 1, 3 }, FontWeights.stepsToTry(2));
		assertArrayEquals(new int[] { 3, 2, 1 }, FontWeights.stepsToTry(3));
	}

	@Test
	void testMediumFallsBackToRegular() {
		assertArrayEquals(new int[] { 5 }, FontWeights.stepsToTry(5));
	}

	@Test
	void testSemiBoldFallsBackToBold() {
		assertArrayEquals(new int[] { 6 }, FontWeights.stepsToTry(6));
	}

	@Test
	void testHeavyWeightsTryHeavierFacesFirst() {
		assertArrayEquals(new int[] { 8, 9 }, FontWeights.stepsToTry(8));
		assertArrayEquals(new int[] { 9, 8 }, FontWeights.stepsToTry(9));
	}
}
