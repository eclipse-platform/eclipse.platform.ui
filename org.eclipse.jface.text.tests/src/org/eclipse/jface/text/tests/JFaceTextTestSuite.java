/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.jface.text.tests.codemining.CodeMiningTest;
import org.eclipse.jface.text.tests.codemining.CodeMiningProjectionViewerTest;
import org.eclipse.jface.text.tests.contentassist.AsyncContentAssistTest;
import org.eclipse.jface.text.tests.reconciler.AbstractReconcilerTest;
import org.eclipse.jface.text.tests.rules.DefaultPartitionerTest;
import org.eclipse.jface.text.tests.rules.DefaultPartitionerZeroLengthTest;
import org.eclipse.jface.text.tests.rules.FastPartitionerTest;
import org.eclipse.jface.text.tests.rules.ScannerColumnTest;
import org.eclipse.jface.text.tests.rules.WordRuleTest;
import org.eclipse.jface.text.tests.source.LineNumberRulerColumnTest;
import org.eclipse.jface.text.tests.templates.persistence.TemplatePersistenceDataTest;


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
		AsyncContentAssistTest.class,

		AbstractReconcilerTest.class,

		DefaultPartitionerTest.class,
		DefaultPartitionerZeroLengthTest.class,
		FastPartitionerTest.class,
		ScannerColumnTest.class,
		WordRuleTest.class,

		TemplatePersistenceDataTest.class,
		CodeMiningTest.class,
		CodeMiningProjectionViewerTest.class
})
public class JFaceTextTestSuite {
	// see @SuiteClasses
}
