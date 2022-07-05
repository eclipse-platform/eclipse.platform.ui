/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.intro;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.api.IWorkbenchPartTest;
import org.eclipse.ui.tests.api.MockPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.0
 */
@RunWith(JUnit4.class)
public class NoIntroPartTest extends IWorkbenchPartTest {

	private IntroDescriptor oldDesc;

	public NoIntroPartTest() {
		super(NoIntroPartTest.class.getSimpleName());
	}

	@Override
	protected MockPart openPart(IWorkbenchPage page) throws Throwable {
		return (MockPart) page.getWorkbenchWindow().getWorkbench()
				.getIntroManager().showIntro(page.getWorkbenchWindow(), false);
	}

	@Override
	protected void closePart(IWorkbenchPage page, MockPart part)
			throws Throwable {
		assertTrue(page.getWorkbenchWindow().getWorkbench().getIntroManager()
				.closeIntro((IIntroPart) part));
	}

	//only test open..shouldn't work.
	@Test
	@Override
	public void testOpenAndClose() throws Throwable {
		// Open a part.
		MockPart part = openPart(fPage);
		assertNull(part);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		oldDesc = Workbench.getInstance().getIntroDescriptor();
		Workbench.getInstance().setIntroDescriptor(null);
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		Workbench.getInstance().setIntroDescriptor(oldDesc);
	}

}
