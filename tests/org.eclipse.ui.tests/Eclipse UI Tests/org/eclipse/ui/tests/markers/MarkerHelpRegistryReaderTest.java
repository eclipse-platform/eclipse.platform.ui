/*******************************************************************************
 * Copyright (c) 2019 Tim Neumann <tim.neumann@advantest.com> and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tim Neumann <tim.neumann@advantest.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.internal.ide.registry.MarkerQuery;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * The test class for {@link MarkerHelpRegistryReader}.
 */
public class MarkerHelpRegistryReaderTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	/**
	 * Tests if the matchChildren flag of the contributions to the markerHelp
	 * extension point is correctly read from the plugin.xml by
	 * {@link MarkerHelpRegistryReader#addHelp(org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry)}
	 */
	@Test
	public void testAddHelpForMarkerHelpMatchChildren() {
		// Mock registry to check if the addHelpQuery is properly used
		MarkerHelpRegistry registry = mock(MarkerHelpRegistry.class);

		// this will parse our contributions and trigger addHelpQuery() calls
		new MarkerHelpRegistryReader().addHelp(registry);

		// Check that MarkerHelpRegistry.addHelpQuery() is
		// called with arguments matching contributed to the plugin.xml

		verify(registry).addHelpQuery(eq(new MarkerQuery("org.eclipse.ui.tests.testmarker", new String[0], true)),
				any(), any());
		verify(registry).addHelpQuery(eq(new MarkerQuery("org.eclipse.ui.tests.testmarker2", new String[0], false)),
				any(), any());
		// Test for backwards compatibility: When no matchChild flag is given.
		verify(registry).addHelpQuery(
				eq(new MarkerQuery("org.eclipse.ui.tests.testmarker_child", new String[0], false)), any(), any());
	}

}
