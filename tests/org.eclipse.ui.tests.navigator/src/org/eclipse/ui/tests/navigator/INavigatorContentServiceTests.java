/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.eclipse.ui.navigator.internal.NavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

public class INavigatorContentServiceTests extends TestCase {

	public static final String COMMON_NAVIGATOR_INSTANCE_ID = "org.eclipse.ui.tests.navigator.TestView";

	public static final String TEST_EXTENSION_ID = "org.eclipse.ui.tests.navigator.testContent";

	public static final String TEST_EXTENSION_2_ID = "org.eclipse.ui.tests.navigator.testContent2";

	public static final String RESOURCE_EXTENSION_ID = "org.eclipse.ui.navigator.resourceContent";

	private final Map expectedChildren = new HashMap();

	private IProject project;

	private INavigatorContentService contentService;

	private CommonViewer viewer;

	protected void setUp() throws Exception {

		TestWorkspace.init();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject("Test");
		Map projectMap = new HashMap();

		expectedChildren.put(project, (projectMap = new HashMap()));

		projectMap.put(project.getFolder("src"), new HashMap());
		projectMap.put(project.getFolder("bin"), new HashMap());
		projectMap.put(project.getFile(".project"), null);
		projectMap.put(project.getFile(".classpath"), null);
		projectMap.put(project.getFile("model.properties"), null);

		EditorTestHelper.showView(COMMON_NAVIGATOR_INSTANCE_ID, true);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		IViewPart commonNavigator = activePage
				.findView(COMMON_NAVIGATOR_INSTANCE_ID);

		viewer = (CommonViewer) commonNavigator.getAdapter(CommonViewer.class);
		viewer.expandAll();

		contentService = viewer.getNavigatorContentService();
	}

	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	public void testFindValidExtensions() {

		contentService.activateExtensions(new String[] { TEST_EXTENSION_ID,
				RESOURCE_EXTENSION_ID }, true);

		ITreeContentProvider contentServiceContentProvider = contentService
				.createCommonContentProvider();

		ILabelProvider contentServiceLabelProvider = contentService
				.createCommonLabelProvider();

		ITreeContentProvider[] rootContentProviders = ((NavigatorContentService) contentService)
				.findRootContentProviders(ResourcesPlugin.getWorkspace()
						.getRoot());

		assertEquals("Ensure there is only one root content provider.", 1,
				rootContentProviders.length);

		ITreeContentProvider[] projectContentProviders = ((NavigatorContentService) contentService)
				.findRelevantContentProviders(project);

		assertEquals("Ensure there are two content providers for an IProject.",
				2, projectContentProviders.length);

		boolean found = false;
		for (int i = 0; i < projectContentProviders.length; i++) {
			if (((NavigatorContentProvider) projectContentProviders[i])
					.getDelegateContentProvider() instanceof TestContentProvider) {

				TestContentProvider testContentProvider = (TestContentProvider) ((NavigatorContentProvider) projectContentProviders[i])
						.getDelegateContentProvider();
				Object[] projectChildren = testContentProvider
						.getChildren(project);
				assertEquals(
						"There should be one test-type child of the project.",
						1, projectChildren.length);
				assertEquals("Parent", contentServiceLabelProvider
						.getText(projectChildren[0]));
				Object[] testRootChildren = contentServiceContentProvider
						.getChildren(projectChildren[0]);
				assertEquals(
						"There should be one test-type child of the root test-type item.",
						3, testRootChildren.length);
				found = true;
			}
		}

		assertTrue("The test content provider was not found.", found);

	}

	public void testDeactivateTestExtension() {

		contentService.activateExtensions(
				new String[] { RESOURCE_EXTENSION_ID }, true);

		ITreeContentProvider contentServiceContentProvider = contentService
				.createCommonContentProvider();

		ILabelProvider contentServiceLabelProvider = contentService
				.createCommonLabelProvider();

		ITreeContentProvider[] rootContentProviders = ((NavigatorContentService) contentService)
				.findRootContentProviders(ResourcesPlugin.getWorkspace()
						.getRoot());

		assertEquals("Ensure there is only one root content provider.", 1,
				rootContentProviders.length);

		ITreeContentProvider[] projectContentProviders = ((NavigatorContentService) contentService)
				.findRelevantContentProviders(project);

		assertEquals("Ensure there is one content provider for an IProject.",
				1, projectContentProviders.length);

	}

	public void testBindTestExtension() {

		INavigatorContentService contentServiceWithProgrammaticBindings = NavigatorContentServiceFactory.INSTANCE
				.createContentService(COMMON_NAVIGATOR_INSTANCE_ID);
		INavigatorContentDescriptor[] boundDescriptors = contentServiceWithProgrammaticBindings
				.bindExtensions(new String[] { TEST_EXTENSION_2_ID }, false);
		contentServiceWithProgrammaticBindings.activateExtensions(new String[] {RESOURCE_EXTENSION_ID, TEST_EXTENSION_ID, TEST_EXTENSION_2_ID}, false);


		assertEquals("One descriptor should have been returned.", 1,
				boundDescriptors.length);

		assertEquals(
				"The declarative content service should have one fewer visible extension ids than the one created programmatically.",
				contentService.getVisibleExtensionIds().length + 1,
				contentServiceWithProgrammaticBindings.getVisibleExtensionIds().length);

		INavigatorContentDescriptor[] visibleDescriptors = contentServiceWithProgrammaticBindings
				.getVisibleExtensions();
		boolean found = false;
		for (int i = 0; i < visibleDescriptors.length; i++)
			if (TEST_EXTENSION_2_ID.equals(visibleDescriptors[i].getId()))
				found = true;
		assertTrue("The programmatically bound extension should be bound.", found);
		
		Set enabledDescriptors = contentServiceWithProgrammaticBindings.findEnabledContentDescriptors(project);
		
		assertEquals("There should be a three extensions.", 3, enabledDescriptors.size());

	}

	public void testTestExtensionVisibility() {
		assertTrue("The test extension should be visible.", contentService
				.getViewerDescriptor().isVisibleContentExtension(
						TEST_EXTENSION_ID));
	}

	public void testResourceExtensionVisibility() {
		assertTrue("The test extension should be visible.", contentService
				.getViewerDescriptor().isVisibleContentExtension(
						RESOURCE_EXTENSION_ID));
	}

	public void testVisibleExtensionIds() {
		String[] visibleIds = contentService.getVisibleExtensionIds();

		assertEquals("There should be two visible extensions.", 2,
				visibleIds.length);

		for (int i = 0; i < visibleIds.length; i++) {
			if (!TEST_EXTENSION_ID.equals(visibleIds[i])
					&& !RESOURCE_EXTENSION_ID.equals(visibleIds[i])) {
				assertTrue("The extension id is invalid:" + visibleIds[i],
						false);
			}
		}

		INavigatorContentDescriptor[] visibleDescriptors = contentService
				.getVisibleExtensions();

		for (int i = 0; i < visibleIds.length; i++) {
			if (!TEST_EXTENSION_ID.equals(visibleDescriptors[i].getId())
					&& !RESOURCE_EXTENSION_ID.equals(visibleDescriptors[i]
							.getId())) {
				assertTrue("The extension id is invalid:"
						+ visibleDescriptors[i].getId(), false);
			}
		}

	}
}
