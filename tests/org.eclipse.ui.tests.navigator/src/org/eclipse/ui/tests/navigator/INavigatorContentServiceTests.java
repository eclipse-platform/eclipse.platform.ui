/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM Corporation - initial API and implementation
 * 		Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.junit.Test;

public class INavigatorContentServiceTests extends NavigatorTestBase {


	public INavigatorContentServiceTests() {
		_navigatorInstanceId = TEST_VIEWER;

	}

	@Test
	public void testFindValidExtensions() {

		_contentService
				.getActivationService()
				.activateExtensions(
						new String[] { TEST_CONTENT, COMMON_NAVIGATOR_RESOURCE_EXT },
						true);

		ITreeContentProvider contentServiceContentProvider = _contentService
				.createCommonContentProvider();

		ILabelProvider contentServiceLabelProvider = _contentService
				.createCommonLabelProvider();

		ITreeContentProvider[] rootContentProviders = ((NavigatorContentService) _contentService)
				.findRootContentProviders(ResourcesPlugin.getWorkspace()
						.getRoot());

		assertEquals("Ensure there is only one root content provider.", 1,
				rootContentProviders.length);

		Set projectContentExtensions = _contentService
				.findContentExtensionsByTriggerPoint(_project);

		assertEquals("Ensure there are two content providers for an IProject.",
				2, projectContentExtensions.size());

		boolean found = false;
		INavigatorContentExtension ext;
		for (Iterator i = projectContentExtensions.iterator(); i.hasNext();) {
			ext = (INavigatorContentExtension) i.next();
			if (ext.getContentProvider() instanceof TestContentProvider) {

				TestContentProvider testContentProvider = (TestContentProvider) ext
						.getContentProvider();
				Object[] projectChildren = testContentProvider
						.getChildren(_project);
				assertEquals(
						"There should be one test-type child of the project.",
						1, projectChildren.length);
				assertEquals("BlueParent", contentServiceLabelProvider
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

	@Test
	public void testDeactivateTestExtension() {

		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		_contentService.createCommonContentProvider();

		_contentService.createCommonLabelProvider();

		Set rootContentProviders = _contentService
				.findRootContentExtensions(ResourcesPlugin.getWorkspace()
						.getRoot());

		assertEquals("Ensure there is only one root content provider.", 1,
				rootContentProviders.size());

		Set projectContentExtensions = _contentService
				.findContentExtensionsByTriggerPoint(_project);

		assertEquals("Ensure there is one content provider for an IProject.",
				1, projectContentExtensions.size());

	}

	@Test
	public void testBindTestExtension() {

		INavigatorContentService contentServiceWithProgrammaticBindings = NavigatorContentServiceFactory.INSTANCE
				.createContentService(TEST_VIEWER);
		INavigatorContentDescriptor[] boundDescriptors = contentServiceWithProgrammaticBindings
				.bindExtensions(new String[] { TEST_CONTENT2 }, false);
		contentServiceWithProgrammaticBindings
				.getActivationService()
				.activateExtensions(
						new String[] { COMMON_NAVIGATOR_RESOURCE_EXT,
								TEST_CONTENT, TEST_CONTENT2 }, false);

		assertEquals("One descriptor should have been returned.", 1,
				boundDescriptors.length);

		assertEquals(
				"The declarative content service should have one fewer visible extension ids than the one created programmatically.",
				_contentService.getVisibleExtensionIds().length + 1,
				contentServiceWithProgrammaticBindings.getVisibleExtensionIds().length);

		INavigatorContentDescriptor[] visibleDescriptors = contentServiceWithProgrammaticBindings
				.getVisibleExtensions();
		boolean found = false;
		for (int i = 0; i < visibleDescriptors.length; i++)
			if (TEST_CONTENT2.equals(visibleDescriptors[i].getId()))
				found = true;
		assertTrue("The programmatically bound extension should be bound.",
				found);

		Set enabledDescriptors = contentServiceWithProgrammaticBindings
				.findContentExtensionsByTriggerPoint(_project);

		assertEquals("There should be a three extensions.", 3,
				enabledDescriptors.size());

	}

	@Test
	public void testTestExtensionVisibility() {
		assertTrue("The test extension should be visible.", _contentService
				.getViewerDescriptor().isVisibleContentExtension(
						TEST_CONTENT));
	}

	@Test
	public void testResourceExtensionVisibility() {
		assertTrue("The test extension should be visible.", _contentService
				.getViewerDescriptor().isVisibleContentExtension(
						COMMON_NAVIGATOR_RESOURCE_EXT));
	}

	@Test
	public void testVisibleExtensionIds() {
		String[] visibleIds = _contentService.getVisibleExtensionIds();

		assertEquals("There should be three visible extensions.", 3,
				visibleIds.length);

		for (int i = 0; i < visibleIds.length; i++) {
			if (!TEST_CONTENT.equals(visibleIds[i])
					&& !COMMON_NAVIGATOR_RESOURCE_EXT.equals(visibleIds[i])
					&& !TEST_CONTENT_HAS_CHILDREN.equals(visibleIds[i])) {
				assertTrue("The extension id is invalid:" + visibleIds[i],
						false);
			}
		}

		INavigatorContentDescriptor[] visibleDescriptors = _contentService
				.getVisibleExtensions();

		for (int i = 0; i < visibleIds.length; i++) {
			if (!TEST_CONTENT.equals(visibleDescriptors[i].getId())
					&& !COMMON_NAVIGATOR_RESOURCE_EXT.equals(visibleDescriptors[i]
							.getId())
						&& !TEST_CONTENT_HAS_CHILDREN.equals(visibleDescriptors[i].getId())) {
				assertTrue("The extension id is invalid:"
						+ visibleDescriptors[i].getId(), false);
			}
		}

	}

	// Bug 267722 [CommonNavigator] ClassCastException when synchronizing
	@Test
	public void testNonCommonViewer() throws Exception {
		EditorTestHelper.showView(TEST_VIEW_NON_COMMONVIEWER, true);

	}


}
