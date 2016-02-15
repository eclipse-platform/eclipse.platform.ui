/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486889
 *******************************************************************************/
package org.eclipse.text.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.text.tests.link.LinkTestSuite;
import org.eclipse.text.tests.templates.TemplatesTestSuite;


/**
 * Test Suite for org.eclipse.text.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
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
		ProjectionTestSuite.class,
		LinkTestSuite.class,
		CopyOnWriteTextStoreTest.class,
		TextUtilitiesTest.class,
		AnnotationModelStressTest.class,
		AnnotationModelExtension2Test.class,
		TemplatesTestSuite.class
})
public class EclipseTextTestSuite {
	// see @SuiteClasses
}
