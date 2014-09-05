/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.FileUtil;

public class IEditorSiteTest extends IWorkbenchPartSiteTest {

    /**
     * Constructor for IEditorSiteTest
     */
    public IEditorSiteTest(String testName) {
        super(testName);
    }

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

    public void testGetActionBarContributor() throws Throwable {
        // From Javadoc: "Returns the editor action bar contributor for 
        // this editor.

        IEditorPart editor = (IEditorPart) createTestPart(fPage);
        IEditorSite site = editor.getEditorSite();
        assertNull(site.getActionBarContributor());

        // TBD: Flesh this out with a real contributor.
    }

}

