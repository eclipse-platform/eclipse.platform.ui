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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * The test class for {@link MarkerHelpRegistry}.
 */
public class MarkerHelpRegistryTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	static final String ATT_HELP_CONTEXT = "helpContext";
	static final String ATT_HAS_HELP = "hasHelp";
	static final String HELP_CONTEXT_STRING = "helpContextIdHere";
	private static final String TEST_MARKER_TYPE = "org.eclipse.ui.tests.testmarker3";

	private IMarker markerWithHelp;
	private IMarker markerNoHelp;
	private IMarker markerWhichMayHaveHelp;

	@Before
	public void setUp() throws Exception {
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

		markerWithHelp = wsRoot.createMarker(TEST_MARKER_TYPE);
		markerNoHelp = wsRoot.createMarker(TEST_MARKER_TYPE);
		markerWhichMayHaveHelp = wsRoot.createMarker(TEST_MARKER_TYPE);

		markerWithHelp.setAttribute(ATT_HELP_CONTEXT, HELP_CONTEXT_STRING);
		markerWithHelp.setAttribute(ATT_HAS_HELP, true);

		markerNoHelp.setAttribute(ATT_HELP_CONTEXT, null);
		markerNoHelp.setAttribute(ATT_HAS_HELP, false);

		markerWhichMayHaveHelp.setAttribute(ATT_HELP_CONTEXT, null);
		markerWhichMayHaveHelp.setAttribute(ATT_HAS_HELP, true);

		TestMarkerHelpContextProvider.init();
	}

	@After
	public void tearDown() throws Exception {
		markerWithHelp.delete();
		markerNoHelp.delete();
		markerWhichMayHaveHelp.delete();
	}

	/**
	 * Tests if the helpContextProvider is used as expected with the contributions
	 * to the markerHelp extension point in the plugin.xml by
	 * {@link MarkerHelpRegistry#getHelp(org.eclipse.core.resources.IMarker)}
	 */
	@Test
	public void testGetHelpForHelpContextProvider() {
		MarkerHelpRegistry markerHelpRegistry = new MarkerHelpRegistry();
		new MarkerHelpRegistryReader().addHelp(markerHelpRegistry);

		List<IMarker> expectedParamsHasHc = new ArrayList<>();
		List<IMarker> expectedParamsGetHc = new ArrayList<>();

		assertEquals("Expected to get help", HELP_CONTEXT_STRING, markerHelpRegistry.getHelp(markerWithHelp));
		expectedParamsHasHc.add(markerWithHelp);
		expectedParamsGetHc.add(markerWithHelp);

		assertNull("Expected to get no help", markerHelpRegistry.getHelp(markerNoHelp));
		expectedParamsHasHc.add(markerNoHelp);

		assertNull("Expected to get no help", markerHelpRegistry.getHelp(markerWhichMayHaveHelp));
		expectedParamsHasHc.add(markerWhichMayHaveHelp);
		expectedParamsGetHc.add(markerWhichMayHaveHelp);

		assertEquals("Unexpected params for hasHelpContextForMarker", expectedParamsHasHc,
				TestMarkerHelpContextProvider.getParamsHasHc());
		assertEquals("Unexpected params for getHelpContextForMarker", expectedParamsGetHc,
				TestMarkerHelpContextProvider.getParamsGetHc());
	}
}
