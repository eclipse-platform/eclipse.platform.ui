/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Test;

public class IEditorSiteTest extends IWorkbenchPartSiteTest {

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartName()
	 */
	@Override
	protected String getTestPartName() throws Throwable {
		return MockEditorPart.NAME;
	}

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartId()
	 */
	@Override
	protected String getTestPartId() throws Throwable {
		return MockEditorPart.ID1;
	}

	/**
	 * @see IWorkbenchPartSiteTest#createTestPart(IWorkbenchPage)
	 */
	@Override
	protected IWorkbenchPart createTestPart(IWorkbenchPage page)
			throws Throwable {
		IProject proj = FileUtil.createProject("createTestPart");
		IFile file = FileUtil.createFile("test1.mock1", proj);
		return IDE.openEditor(page, file, true);
	}

	@Test
	public void testGetActionBarContributor() throws Throwable {
		// From Javadoc: "Returns the editor action bar contributor for
		// this editor.

		IEditorPart editor = (IEditorPart) createTestPart(fPage);
		IEditorSite site = editor.getEditorSite();
		assertNull(site.getActionBarContributor());

		// TBD: Flesh this out with a real contributor.
	}

}

