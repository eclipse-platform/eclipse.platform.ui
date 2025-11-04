/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;

import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @since 3.1
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class GenerateIdentifiersTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	

	private static final int count = 10000;

	@Test
	public void test() throws Throwable {
		final IActivityManager activityManager = getWorkbench().getActivitySupport().getActivityManager();

		exercise(() -> {
			// construct the Identifiers to test
			final String[] ids = new String[count];
			for (int i = 0; i < ids.length; i++) {
				long timestamp = System.currentTimeMillis();
				ids[i] = "org.eclipse.jdt.ui/" + i + timestamp;
			}

			startMeasuring();
			for (String id : ids) {
				activityManager.getIdentifier(id);
			}
			stopMeasuring();
		});
		commitMeasurements();
		assertPerformance();
	}
}
