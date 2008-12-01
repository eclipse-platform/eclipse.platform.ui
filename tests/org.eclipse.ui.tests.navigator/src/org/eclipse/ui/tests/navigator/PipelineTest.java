/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;

public class PipelineTest extends NavigatorTestBase {

	public PipelineTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.PipelineTestView";
	}

	public void testNavigatorRootContents() throws Exception {

		assertEquals(
				"There should be no visible extensions for the pipeline viewer.",
				0, contentService.getVisibleExtensionIds().length);

		contentService.bindExtensions(new String[] {
				COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT },
				false);

		assertEquals(
				"There should be two visible extension for the pipeline viewer.",
				2, contentService.getVisibleExtensionIds().length);

		contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT,
						COMMON_NAVIGATOR_JAVA_EXT }, true);

		viewer.refresh();

		// we do this to force the rendering of the children of items[0]
		viewer.setSelection(
				new StructuredSelection(project.getFile(".project")), true); //$NON-NLS-1$

		TreeItem[] rootItems = viewer.getTree().getItems();

		assertEquals("There should be one item.", 1, rootItems.length); //$NON-NLS-1$		

		assertTrue(
				"The root object should be an IJavaProject, which is IAdaptable.", rootItems[0].getData() instanceof IAdaptable); //$NON-NLS-1$

		IProject adaptedProject = (IProject) ((IAdaptable) rootItems[0]
				.getData()).getAdapter(IProject.class);
		assertEquals(project, adaptedProject);

		IFolder sourceFolder = project.getFolder(new Path("src"));
		viewer.add(project, sourceFolder);

		TreeItem[] projectChildren = rootItems[0].getItems();

		assertTrue("There should be some items.", projectChildren.length > 0); //$NON-NLS-1$

		for (int i = 0; i < projectChildren.length; i++) {
			if (projectChildren[i].getData() == sourceFolder)
				fail("The src folder should not be added as an IFolder.");
		}

		// a new project without a Java nature should add without an issue.
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("New Project");
		viewer.add(viewer.getInput(), newProject);

		rootItems = viewer.getTree().getItems();

		assertEquals("There should be two items.", 2, rootItems.length); //$NON-NLS-1$

		boolean found = false;
		for (int i = 0; i < rootItems.length && !found; i++) {
			if (rootItems[i].getData() instanceof IProject) {
				IProject newProjectFromTree = (IProject) rootItems[i].getData();
				if (newProject.equals(newProjectFromTree))
					found = true;
			}
		}
		assertTrue(found);
	}


}
