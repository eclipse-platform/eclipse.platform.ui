/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - [484157] Add zoom test
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test Suite for org.eclipse.ui.editors.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
		ChainedPreferenceStoreTest.class,
		EncodingChangeTests.class,
		GotoLineTest.class,
		SegmentedModeTest.class,
		MarkerAnnotationOrderTest.class,
		ZoomTest.class,
		FileDocumentProviderTest.class,
		StatusEditorTest.class
})
public class EditorsTestSuite {
	// see @SuiteClasses
}
