/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software Incorporated - Added to CNF tests
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.jst;

import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.NavigatorTestBase;

public class JstPipelineTest extends NavigatorTestBase {

	private static final boolean SLEEP_LONG = false;


	public JstPipelineTest() {
		_navigatorInstanceId = TEST_VIEWER_PIPELINE;
	}

	public void setUp() throws Exception {
		super.setUp();

		WebJavaContentProvider.staticInit(_contentService
				.getContentExtensionById(COMMON_NAVIGATOR_JAVA_EXT)
				.getContentProvider().getClass().getClassLoader());
	}

	/*
	 * This sort of approximates the JST/JDT pipeline relationship. The thing
	 * this is mainly testing for is the case where the JST NCE provides JDT
	 * objects as the content, and the JDT label provider does not get invoked
	 * for those objects.
	 */
	public void testJstPipeline() throws Exception {

		_contentService.bindExtensions(new String[] {
				COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT,
				TEST_CONTENT_JST }, false);

		// Note this test will fail showing only one if the JDT stuff
		// is not included in the executing bundles (which it normally is)
		assertEquals(
				"There should be two visible extensions for the pipeline viewer.",
				3, _contentService.getVisibleExtensionIds().length);

		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT,
						COMMON_NAVIGATOR_JAVA_EXT, TEST_CONTENT_JST }, true);

		refreshViewer();

		// we do this to force the rendering of the children of items[0]
		_viewer.setSelection(new StructuredSelection(_project
				.getFile(".project")), true); //$NON-NLS-1$

		TreeItem[] rootItems = _viewer.getTree().getItems();

		assertEquals(
				"There should be " + _projectCount + " item(s).", _projectCount, rootItems.length); //$NON-NLS-1$		

		assertTrue(
				"The root object should be an IJavaProject, which is IAdaptable.", rootItems[0].getData() instanceof IAdaptable); //$NON-NLS-1$

		IProject adaptedProject = (IProject) ((IAdaptable) rootItems[_projectInd]
				.getData()).getAdapter(IProject.class);
		assertEquals(_project, adaptedProject);

		IFolder sourceFolder = _project.getFolder(new Path("src"));
		_viewer.add(_project, sourceFolder);

		TreeItem[] projectChildren = rootItems[_projectInd].getItems();

		if (SLEEP_LONG)
			DisplayHelper.sleep(1000000);

		boolean foundJava = false;
		boolean foundLib = false;
		boolean foundCharset = false;

		for (int i = 0; i < projectChildren.length; i++) {
			if (projectChildren[i].getText().startsWith("Compressed Java")) {
				foundJava = true;
				_viewer.setExpandedState(projectChildren[i].getData(), true);

				TreeItem[] srcChildren = projectChildren[i].getItems();
				for (int j = 0; j < srcChildren.length; j++) {
					if (srcChildren[j].getText().startsWith(
							"Compressed Libraries"))
						foundLib = true;
					if (srcChildren[j].getText().startsWith("charsets.jar"))
						foundCharset = true;
				}
			}
		}

		assertTrue(foundJava);
		assertTrue(foundLib);
		assertTrue(foundCharset);
	}

}
