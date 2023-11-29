/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ui.tests.performance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.osgi.framework.Bundle;


/**
 * This class/test was originally written for WTP bug 106158, and adapted to a
 * generic "platform" test case, since some of the problem was due to bug
 * 107121
 *
 * Thanks for Jeffrey Liu (jeffliu@ca.ibm.com) who wrote the test for WTP.
 *
 * And, thanks to Eric Glass (ericglass@maximus.com) for opening bug 106158
 * (100% CPU for over 3 minutes opening a directory in the Navigator view with
 * over 2000 HTML files) https://bugs.eclipse.org/bugs/show_bug.cgi?id=106158
 * and for providing 2500 "generic" HTML files for the original test case in
 * WTP.
 *
 * modified by David Williams for platform level test that does not depend on
 * WTP or, for that matter, any meaningful content type. The content type
 * assumed there is designed simply to "take a while" to complete, if asked
 * for its properties.
 *
 * Note, since this test companion "ContentDescriberForTestsOnly", simply uses
 * "sleep" to simulate computations, it only effects Elapsed Time (not CPU
 * Time).
 */
public class OpenProjectExplorerFolderTest extends PerformanceTestCase {
	/*
	 * performance testcase for bug 106158
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106158
	 */
	public void testOpenNavigatorFolder() {
		IProject project = createProject("testViewAndContentTypeProject");
		Bundle bundle = Platform.getBundle("org.eclipse.ui.tests.performance");
		URL url = bundle.getEntry("data/testContentType.zip");
		try (ZipInputStream zis = new ZipInputStream(url.openStream())) {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				byte[] content = new byte[0];
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					byte[] b = new byte[2048];
					int read = zis.read(b);
					while (read != -1) {
						baos.write(b, 0, read);
						read = zis.read(b);
					}
					content = baos.toByteArray();
				}
				catch (IOException e) {
					fail(e.getMessage());
				}
				IFile file = project.getFile(entry.getName());
				try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
					if (!file.exists())
						file.create(bais, true, new NullProgressMonitor());
					else
						file.setContents(bais, true, false, new NullProgressMonitor());
				}
				catch (CoreException e) {
					fail(e.getMessage());
				}
				entry = zis.getNextEntry();
			}
		}
		catch (IOException e) {
			fail(e.getMessage());
		}
		startMeasuring();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = null;
		try {
			view = activePage.showView(ProjectExplorer.VIEW_ID);
		}
		catch (PartInitException e) {
			fail(e.getMessage());
		}
		ProjectExplorer projectExplorer = null;
		try {
			projectExplorer = (ProjectExplorer) view;
		}
		catch (ClassCastException e) {
			fail(e.getMessage());
		}
		projectExplorer.getCommonViewer().expandAll();
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

	private IProject createProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (!project.exists()) {
			try {
				project.create(new NullProgressMonitor());
				project.open(new NullProgressMonitor());
			}
			catch (CoreException e) {
				fail(e.getMessage());
			}
		}
		return project;
	}
}
