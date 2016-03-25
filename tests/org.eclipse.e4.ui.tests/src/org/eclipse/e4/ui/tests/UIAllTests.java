/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 483362, 486804
 ******************************************************************************/

package org.eclipse.e4.ui.tests;

import org.eclipse.e4.ui.tests.application.Bug299755Test;
import org.eclipse.e4.ui.tests.application.Bug308220Test;
import org.eclipse.e4.ui.tests.application.Bug320857Test;
import org.eclipse.e4.ui.tests.application.ModelElementTest;
import org.eclipse.e4.ui.tests.application.ModelRobustnessTest;
import org.eclipse.e4.ui.tests.application.ResourceHandlerTest;
import org.eclipse.e4.ui.tests.application.StartupTestSuite;
import org.eclipse.e4.ui.tests.application.UIEventTypesTest;
import org.eclipse.e4.ui.tests.workbench.Bug308317Test;
import org.eclipse.e4.ui.tests.workbench.ContextTest;
import org.eclipse.e4.ui.tests.workbench.ExtensionsSortTests;
import org.eclipse.e4.ui.tests.workbench.HandlerActivationTest;
import org.eclipse.e4.ui.tests.workbench.HandlerTest;
import org.eclipse.e4.ui.tests.workbench.InjectionEventTest;
import org.eclipse.e4.ui.tests.workbench.MApplicationCommandAccessTest;
import org.eclipse.e4.ui.tests.workbench.MMenuItemTest;
import org.eclipse.e4.ui.tests.workbench.MPartSashContainerTest;
import org.eclipse.e4.ui.tests.workbench.MPartTest;
import org.eclipse.e4.ui.tests.workbench.MSashTest;
import org.eclipse.e4.ui.tests.workbench.MSaveablePartTest;
import org.eclipse.e4.ui.tests.workbench.MToolItemTest;
import org.eclipse.e4.ui.tests.workbench.MWindowTest;
import org.eclipse.e4.ui.tests.workbench.ModelAssemblerFragmentOrderingTests;
import org.eclipse.e4.ui.tests.workbench.ModelAssemblerTests;
import org.eclipse.e4.ui.tests.workbench.PartFocusTest;
import org.eclipse.e4.ui.tests.workbench.PartOnTopManagerTest;
import org.eclipse.e4.ui.tests.workbench.PartRenderingEngineTests;
import org.eclipse.e4.ui.tests.workbench.SashRendererTest;
import org.eclipse.e4.ui.tests.workbench.TopoSortTests;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRendererTest;
import org.eclipse.e4.ui.workbench.renderers.swt.TabStateHandlerTest;
import org.eclipse.e4.ui.workbench.renderers.swt.ThemeDefinitionChangedHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * All E4 UI-related tests
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		// Hack: bug 422676, run InjectionEventTest first
		InjectionEventTest.class,
		StartupTestSuite.class,
		UIEventTypesTest.class,
		Bug299755Test.class,
		Bug308220Test.class,
		Bug320857Test.class,
		PartOnTopManagerTest.class,
		PartRenderingEngineTests.class,
		SashRendererTest.class,
		MApplicationCommandAccessTest.class,
		MMenuItemTest.class,
		MPartTest.class,
		MPartSashContainerTest.class,
		MSaveablePartTest.class,
		MToolItemTest.class,
		MWindowTest.class,
		MSashTest.class,
		HandlerTest.class,
		ContextTest.class,
		Bug308317Test.class,
		ModelRobustnessTest.class,
		ResourceHandlerTest.class,
		PartFocusTest.class,
		ModelElementTest.class,
		StackRendererTest.class,
		TabStateHandlerTest.class,
		ThemeDefinitionChangedHandlerTest.class,
		TopoSortTests.class,
		ExtensionsSortTests.class,
		HandlerActivationTest.class,
		ModelAssemblerTests.class, 
		ModelAssemblerFragmentOrderingTests.class
		// SWTPartRendererTest.class,
})
public class UIAllTests {
}
