/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486889
 *******************************************************************************/
package org.eclipse.text.tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.eclipse.text.tests.link.LinkTestSuite;
import org.eclipse.text.tests.templates.TemplatesTestSuite;

/**
 * Test Suite for org.eclipse.text.
 *
 * @since 3.0
 */
@Suite
@SelectClasses({
		MultiStringMatcherTest.class,
		ConfigurableLineTrackerTest.class,
		LineTrackerTest4.class,
		DocumentExtensionTest.class,
		LineTrackerTest3.class,
		DocumentTest.class,
		FindReplaceDocumentAdapterTest.class,
		PositionUpdatingCornerCasesTest.class,
		ExclusivePositionUpdaterTest.class,
		TextEditTests.class,
		GapTextTest.class,
		GapTextStoreTest.class,
		ChildDocumentTest.class,
		ProjectionDocumentTest.class, 
		ProjectionMappingTest.class,
		LinkTestSuite.class,
		CopyOnWriteTextStoreTest.class,
		TextUtilitiesTest.class,
		AnnotationModelStressTest.class,
		AnnotationModelExtension2Test.class,
		TemplatesTestSuite.class
})
public class EclipseTextTestSuite {

}
