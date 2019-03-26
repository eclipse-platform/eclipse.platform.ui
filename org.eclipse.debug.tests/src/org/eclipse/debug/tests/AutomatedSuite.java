/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
 *     Freescale Semiconductor - Bug 293618, Breakpoints view sorts up to first colon only
 *     Anton Kosyakov (Itemis AG) - Bug 438621 - [step filtering] Provide an extension point to enhance methods step filtering.
 *******************************************************************************/
package org.eclipse.debug.tests;

import org.eclipse.debug.tests.breakpoint.BreakpointOrderingTests;
import org.eclipse.debug.tests.console.ConsoleDocumentAdapterTests;
import org.eclipse.debug.tests.console.ConsoleManagerTests;
import org.eclipse.debug.tests.console.ConsoleTests;
import org.eclipse.debug.tests.console.IOConsoleTests;
import org.eclipse.debug.tests.console.ProcessConsoleManagerTests;
import org.eclipse.debug.tests.console.ProcessConsoleTests;
import org.eclipse.debug.tests.console.StreamsProxyTests;
import org.eclipse.debug.tests.launching.AcceleratorSubstitutionTests;
import org.eclipse.debug.tests.launching.ArgumentParsingTests;
import org.eclipse.debug.tests.launching.LaunchConfigurationTests;
import org.eclipse.debug.tests.launching.LaunchFavoriteTests;
import org.eclipse.debug.tests.launching.LaunchGroupTests;
import org.eclipse.debug.tests.launching.LaunchHistoryTests;
import org.eclipse.debug.tests.launching.LaunchManagerTests;
import org.eclipse.debug.tests.launching.LaunchTests;
import org.eclipse.debug.tests.launching.RefreshTabTests;
import org.eclipse.debug.tests.sourcelookup.SourceLookupFacilityTests;
import org.eclipse.debug.tests.statushandlers.StatusHandlerTests;
import org.eclipse.debug.tests.stepfilters.StepFiltersTests;
import org.eclipse.debug.tests.view.memory.MemoryRenderingTests;
import org.eclipse.debug.tests.viewer.model.ChildrenUpdateTests;
import org.eclipse.debug.tests.viewer.model.FilterTransformTests;
import org.eclipse.debug.tests.viewer.model.PresentationContextTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerContentTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerDeltaTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerFilterTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerLazyModeTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerSelectionTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerStateTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerUpdateTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
  * Tests for integration and nightly builds.
 *
 * @since 3.6
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to use the JUnit Launcher.
	 *
	 * @return the test suite
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Constructs the automated test suite. Adds all tests.
	 */
	public AutomatedSuite() {
		// Source lookup tests
		addTest(new TestSuite(SourceLookupFacilityTests.class));
		// BP tests
		addTest(new TestSuite(BreakpointOrderingTests.class));
	    // Note: jface viewer tests were moved out of nightly tests
	    // due to frequent problems on nightly build machines.
	    // (Bug 343308).

		// Virtual viewer tests
		addTest(new TestSuite(VirtualViewerDeltaTests.class));
        addTest(new TestSuite(VirtualViewerContentTests.class));
		addTest(new TestSuite(VirtualViewerLazyModeTests.class));
		addTest(new TestSuite(VirtualViewerSelectionTests.class));
		addTest(new TestSuite(VirtualViewerStateTests.class));
		addTest(new TestSuite(VirtualViewerUpdateTests.class));
        addTest(new TestSuite(VirtualViewerFilterTests.class));

		// Viewer neutral tests
		addTest(new TestSuite(FilterTransformTests.class));
		addTest(new TestSuite(ChildrenUpdateTests.class));
		addTest(new TestSuite(PresentationContextTests.class));

		// Memory view
		addTest(new TestSuite(MemoryRenderingTests.class));

		// Launch framework
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(AcceleratorSubstitutionTests.class));
		addTest(new TestSuite(LaunchHistoryTests.class));
		addTest(new TestSuite(LaunchFavoriteTests.class));
		addTest(new TestSuite(LaunchManagerTests.class));
		addTest(new TestSuite(RefreshTabTests.class));
		addTest(new TestSuite(ArgumentParsingTests.class));
		addTest(new TestSuite(LaunchTests.class));

		// Status handlers
		addTest(new TestSuite(StatusHandlerTests.class));

		// Step filters
		addTest(new TestSuite(StepFiltersTests.class));

		// Console view
		addTest(new TestSuite(ConsoleDocumentAdapterTests.class));
		addTest(new TestSuite(ConsoleManagerTests.class));
		addTest(new TestSuite(ConsoleTests.class));
		addTest(new TestSuite(IOConsoleTests.class));
		addTest(new TestSuite(ProcessConsoleManagerTests.class));
		addTest(new TestSuite(ProcessConsoleTests.class));
		addTest(new TestSuite(StreamsProxyTests.class));

		// Launch Groups
		addTest(new TestSuite(LaunchGroupTests.class));
	}
}
