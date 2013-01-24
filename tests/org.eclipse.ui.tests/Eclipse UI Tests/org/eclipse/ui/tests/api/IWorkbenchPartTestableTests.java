/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.testing.IWorkbenchPartTestable;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class IWorkbenchPartTestableTests extends UITestCase {

	/**
	 * @param testName
	 */
	public IWorkbenchPartTestableTests(String testName) {
		super(testName);
	}

	/**
	 * Iterate over all parts to ensure that they all return a part testable
	 * that contains a real composite.
	 */
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
		Set encounteredControls = new HashSet();
		testParts(editors, encounteredControls);

		IViewPart views[] = page.getViews();
		testParts(views, encounteredControls);

		IIntroPart intro = page.getWorkbenchWindow().getWorkbench()
				.getIntroManager().showIntro(page.getWorkbenchWindow(), false);
		testParts(new IIntroPart [] { intro }, encounteredControls);

		encounteredControls.clear();
	}

	/**
	 * @param parts
	 * @param encounteredControls
	 */
	private void testParts(Object[] parts, Set encounteredControls) {
		for (int i = 0; i < parts.length; i++) {
			String title = null;
			IWorkbenchPartTestable testable = null;
			if (parts instanceof IWorkbenchPart[]) {
				testable = (IWorkbenchPartTestable) ((IWorkbenchPart) parts[i])
						.getSite().getAdapter(IWorkbenchPartTestable.class);
				title = ((IWorkbenchPart) parts[i]).getTitle();
			} else {
				testable = (IWorkbenchPartTestable) ((IIntroPart) parts[i])
						.getIntroSite()
						.getAdapter(IWorkbenchPartTestable.class);
				title = ((IIntroPart) parts[i]).getTitle();
			}
			assertNotNull(title + " has null testable", testable);
			assertTrue(title + " has previously encountered control",
					encounteredControls.add(testable.getControl()));
		}
	}
}
