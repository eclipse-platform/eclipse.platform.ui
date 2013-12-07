/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.e4.ui.tests.application.Bug299755Test;
import org.eclipse.e4.ui.tests.application.Bug308220Test;
import org.eclipse.e4.ui.tests.application.Bug320857Test;
import org.eclipse.e4.ui.tests.application.ModelElementTest;
import org.eclipse.e4.ui.tests.application.ModelRobustnessTest;
import org.eclipse.e4.ui.tests.application.ResourceHandlerTest;
import org.eclipse.e4.ui.tests.application.StartupTestSuite;
import org.eclipse.e4.ui.tests.application.UIEventTypesTest;
import org.eclipse.e4.ui.tests.reconciler.ModelReconcilerTestSuite;
import org.eclipse.e4.ui.tests.workbench.Bug308317Test;
import org.eclipse.e4.ui.tests.workbench.ContextTest;
import org.eclipse.e4.ui.tests.workbench.HandlerTest;
import org.eclipse.e4.ui.tests.workbench.InjectionEventTest;
import org.eclipse.e4.ui.tests.workbench.MMenuItemTest;
import org.eclipse.e4.ui.tests.workbench.MPartSashContainerTest;
import org.eclipse.e4.ui.tests.workbench.MPartTest;
import org.eclipse.e4.ui.tests.workbench.MSashTest;
import org.eclipse.e4.ui.tests.workbench.MSaveablePartTest;
import org.eclipse.e4.ui.tests.workbench.MToolItemTest;
import org.eclipse.e4.ui.tests.workbench.MWindowTest;
import org.eclipse.e4.ui.tests.workbench.PartFocusTest;
import org.eclipse.e4.ui.tests.workbench.PartRenderingEngineTests;
import org.eclipse.e4.ui.tests.workbench.SashRendererTest;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRendererTest;
import org.eclipse.e4.ui.workbench.renderers.swt.TabStateHandlerTest;
import org.eclipse.e4.ui.workbench.renderers.swt.ThemeDefinitionChangedHandlerTest;

//import org.eclipse.e4.ui.workbench.renderers.swt.StackRendererTest;

/**
 *
 */
public class UIAllTests extends TestSuite {
	public static Test suite() {
		return new UIAllTests();
	}

	public UIAllTests() {
		// Hack: bug 422676, run InjectionEventTest first
		addTestSuite(InjectionEventTest.class);
		addTest(StartupTestSuite.suite());
		addTestSuite(UIEventTypesTest.class);
		addTestSuite(Bug299755Test.class);
		addTestSuite(Bug308220Test.class);
		addTestSuite(Bug320857Test.class);
		addTestSuite(PartRenderingEngineTests.class);
		addTestSuite(SashRendererTest.class);
		addTestSuite(MMenuItemTest.class);
		addTestSuite(MPartTest.class);
		addTestSuite(MPartSashContainerTest.class);
		addTestSuite(MSaveablePartTest.class);
		addTestSuite(MToolItemTest.class);
		addTestSuite(MWindowTest.class);
		addTestSuite(MSashTest.class);
		addTestSuite(HandlerTest.class);
		addTestSuite(ContextTest.class);
		addTest(ModelReconcilerTestSuite.suite());
		addTestSuite(Bug308317Test.class);
		addTestSuite(ModelRobustnessTest.class);
		addTestSuite(ResourceHandlerTest.class);
		addTestSuite(PartFocusTest.class);
		addTestSuite(ModelElementTest.class);
		addTestSuite(StackRendererTest.class);
		addTestSuite(TabStateHandlerTest.class);
		addTestSuite(ThemeDefinitionChangedHandlerTest.class);
		// addTestSuite(SWTPartRendererTest.class);
	}
}
