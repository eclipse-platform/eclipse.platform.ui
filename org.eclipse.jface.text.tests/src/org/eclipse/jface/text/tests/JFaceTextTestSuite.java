/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.jface.text.tests.reconciler.AbstractReconcilerTest;
import org.eclipse.jface.text.tests.rules.DefaultPartitionerTest;
import org.eclipse.jface.text.tests.rules.DefaultPartitionerZeroLengthTest;
import org.eclipse.jface.text.tests.rules.FastPartitionerTest;
import org.eclipse.jface.text.tests.rules.ScannerColumnTest;
import org.eclipse.jface.text.tests.rules.WordRuleTest;
import org.eclipse.jface.text.tests.source.LineNumberRulerColumnTest;


/**
 * Test Suite for org.eclipse.jface.text.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
		LineNumberRulerColumnTest.class,
		HTML2TextReaderTest.class,
		TextHoverPopupTest.class,
		TextPresentationTest.class,
		DefaultUndoManagerTest.class,
		TextViewerTest.class,
		TextViewerUndoManagerTest.class,
		DefaultPairMatcherTest.class,
		DefaultPairMatcherTest2.class,

		AbstractReconcilerTest.class,

		DefaultPartitionerTest.class,
		DefaultPartitionerZeroLengthTest.class,
		FastPartitionerTest.class,
		ScannerColumnTest.class,
		WordRuleTest.class
})
public class JFaceTextTestSuite {
	// see @SuiteClasses
}
