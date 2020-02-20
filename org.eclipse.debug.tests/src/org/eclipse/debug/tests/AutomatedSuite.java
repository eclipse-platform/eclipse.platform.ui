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
import org.eclipse.debug.tests.console.IOConsoleFixedWidthTests;
import org.eclipse.debug.tests.console.IOConsoleTests;
import org.eclipse.debug.tests.console.ProcessConsoleManagerTests;
import org.eclipse.debug.tests.console.ProcessConsoleTests;
import org.eclipse.debug.tests.console.RuntimeProcessTests;
import org.eclipse.debug.tests.console.StreamsProxyTests;
import org.eclipse.debug.tests.console.TextConsoleViewerTest;
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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for integration and nightly builds.
 *
 * @since 3.6
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		SourceLookupFacilityTests.class, BreakpointOrderingTests.class,
		VirtualViewerDeltaTests.class, VirtualViewerContentTests.class,
		VirtualViewerLazyModeTests.class, VirtualViewerSelectionTests.class,
		VirtualViewerStateTests.class, VirtualViewerUpdateTests.class,
		VirtualViewerFilterTests.class, FilterTransformTests.class,
		ChildrenUpdateTests.class, PresentationContextTests.class,
		MemoryRenderingTests.class, LaunchConfigurationTests.class,
		AcceleratorSubstitutionTests.class, LaunchHistoryTests.class,
		LaunchFavoriteTests.class, LaunchManagerTests.class,
		RefreshTabTests.class, ArgumentParsingTests.class, LaunchTests.class,
		StatusHandlerTests.class,
		StepFiltersTests.class,
		ConsoleDocumentAdapterTests.class, ConsoleManagerTests.class,
		ConsoleTests.class, IOConsoleTests.class,
		IOConsoleFixedWidthTests.class, ProcessConsoleManagerTests.class,
		ProcessConsoleTests.class, StreamsProxyTests.class,
		TextConsoleViewerTest.class, RuntimeProcessTests.class,
		LaunchGroupTests.class })
public class AutomatedSuite {
}
