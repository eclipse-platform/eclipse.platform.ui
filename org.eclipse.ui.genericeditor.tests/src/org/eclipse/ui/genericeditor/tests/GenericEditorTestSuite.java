/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *     Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		CompletionTest.class,
		ContextInfoTest.class,
		StylingTest.class,
		HoverTest.class,
		EditorTest.class,
		FoldingTest.class,
		AutoEditTest.class,
		ReconcilerTest.class,
		HighlightTest.class
})
public class GenericEditorTestSuite {
	// see @SuiteClasses

}
