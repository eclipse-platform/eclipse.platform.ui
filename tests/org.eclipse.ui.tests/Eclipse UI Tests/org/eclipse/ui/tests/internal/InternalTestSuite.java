/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import org.eclipse.ui.tests.markers.Bug75909Test;
import org.eclipse.ui.tests.markers.DeclarativeFilterActivityTest;
import org.eclipse.ui.tests.markers.DeclarativeFilterDeclarationTest;
import org.eclipse.ui.tests.markers.MarkerSortUtilTest;
import org.eclipse.ui.tests.markers.MarkerSupportRegistryTests;
import org.eclipse.ui.tests.markers.MarkerTesterTest;
import org.eclipse.ui.tests.markers.MarkerViewTests;
import org.eclipse.ui.tests.markers.ResourceMappingMarkersTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI Implementation.
 */
public class InternalTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new InternalTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public InternalTestSuite() {
        addTest(new TestSuite(AnimationEngineTest.class));
        addTest(new TestSuite(EditorActionBarsTest.class));
        addTest(new TestSuite(ActionSetExpressionTest.class));
        addTest(new TestSuite(PopupMenuExpressionTest.class));
        addTest(new TestSuite(Bug41931Test.class));
        addTest(Bug75909Test.suite());
        addTest(new TestSuite(Bug78470Test.class));
        addTest(new TestSuite(DeclarativeFilterActivityTest.class));
        addTest(new TestSuite(DeclarativeFilterDeclarationTest.class));
        addTest(new TestSuite(ResourceMappingMarkersTest.class));
        addTest(new TestSuite(MarkerSupportRegistryTests.class));
        addTest(new TestSuite(MarkerSortUtilTest.class));
        addTest(new TestSuite(MarkerViewTests.class));
        addTest(Bug99858Test.suite());
        addTest(new TestSuite(WorkbenchWindowSubordinateSourcesTests.class));
        addTest(new TestSuite(ReopenMenuTest.class));
        addTest(new TestSuite(UtilTest.class));
		addTest(new TestSuite(MarkerTesterTest.class));
		addTest(new TestSuite(TextHandlerTest.class));
        addTest(new TestSuite(PerspectiveSwitcherTest.class));
        addTest(new TestSuite(StickyViewManagerTest.class));
        addTest(new TestSuite(FileEditorMappingTest.class));
        addTest(new TestSuite(WorkbenchSiteProgressServiceModelTagsTest.class));
		addTest(new TestSuite(WorkbenchPageTest.class));
    }
}
