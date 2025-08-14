/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.testing.IWorkbenchPartTestable;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class IWorkbenchPartTestableTests extends UITestCase {

	public IWorkbenchPartTestableTests() {
		super(IWorkbenchPartTestableTests.class.getSimpleName());
	}

	/**
	 * Iterate over all parts to ensure that they all return a part testable
	 * that contains a real composite.
	 */
	@Test
	@Ignore
	public void XXXtestGetComposite() throws CoreException {
		IWorkbenchPage page = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID)
				.getActivePage();
		assertNotNull(page);

		IProject proj = FileUtil.createProject("testIWorkbenchPartTestable");

		assertNotNull(IDE
				.openEditor(page, FileUtil.createFile("foo.txt", proj)));
		assertNotNull(IDE.openEditor(page, FileUtil.createFile(
				"foo.properties", proj)));
		assertNotNull(IDE.openEditor(page, FileUtil
				.createFile("foo.java", proj)));
		assertNotNull(IDE
				.openEditor(page, FileUtil.createFile("foo.xml", proj)));

		IEditorPart editors[] = page.getEditors();
		Set<Composite> encounteredControls = new HashSet<>();
		testParts(editors, encounteredControls);

		IViewPart views[] = page.getViews();
		testParts(views, encounteredControls);

		IIntroPart intro = page.getWorkbenchWindow().getWorkbench()
				.getIntroManager().showIntro(page.getWorkbenchWindow(), false);
		testParts(new IIntroPart [] { intro }, encounteredControls);

		encounteredControls.clear();
	}

	private void testParts(Object[] parts, Set<Composite> encounteredControls) {
		for (Object part : parts) {
			String title = null;
			IWorkbenchPartTestable testable = null;
			if (parts instanceof IWorkbenchPart[]) {
				testable = ((IWorkbenchPart) part).getSite().getAdapter(IWorkbenchPartTestable.class);
				title = ((IWorkbenchPart) part).getTitle();
			} else {
				testable = ((IIntroPart) part).getIntroSite().getAdapter(IWorkbenchPartTestable.class);
				title = ((IIntroPart) part).getTitle();
			}
			assertNotNull(title + " has null testable", testable);
			assertTrue(title + " has previously encountered control",
					encounteredControls.add(testable.getControl()));
		}
	}
}
