/*******************************************************************************
 * Copyright (c) 2019 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MultiPageEditorPartTest extends UITestCase {

	public MultiPageEditorPartTest() {
		super(MultiPageEditorPartTest.class.getSimpleName());
	}

	@Override
	protected void doTearDown() throws Exception {
		TestMultiPageEditorThrowingPartInitException.resetSpy();
		super.doTearDown();
	}

	@Test
	public void testDisposalWithoutSuccessfulInitialization() throws Exception {
		IEditorPart editor = openTestWindow().getActivePage().openEditor(new NullEditorInput(),
				"org.eclipse.ui.tests.multipageeditor.TestMultiPageEditorThrowingPartInitException"); //$NON-NLS-1$

		assertThat(editor, instanceOf(ErrorEditorPart.class));
		assertThat("The editor should have been diposed by CompatibilityPart",
				TestMultiPageEditorThrowingPartInitException.disposeCalled, is(true));
		assertThat("No exception should have been thrown while disposing",
				TestMultiPageEditorThrowingPartInitException.exceptionWhileDisposing, is(nullValue()));
	}

}
