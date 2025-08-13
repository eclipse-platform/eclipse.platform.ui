/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *     Oakland Software Incorporated - Added to CNF tests
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.jst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.NavigatorTestBase;
import org.junit.Before;
import org.junit.Test;

public class JstPipelineTest extends NavigatorTestBase {

	private static final boolean SLEEP_LONG = false;

	public JstPipelineTest() {
		_navigatorInstanceId = TEST_VIEWER_PIPELINE;
	}

	@Override
	@Before
	public void setUp() throws CoreException {
		super.setUp();

		WebJavaContentProvider.staticInit(_contentService.getContentExtensionById(COMMON_NAVIGATOR_JAVA_EXT)
				.getContentProvider().getClass().getClassLoader());
	}

	/*
	 * This sort of approximates the JST/JDT pipeline relationship. The thing this
	 * is mainly testing for is the case where the JST NCE provides JDT objects as
	 * the content, and the JDT label provider does not get invoked for those
	 * objects.
	 */
	@Test
	public void testJstPipeline() throws Exception {

		_contentService.bindExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT, TEST_CONTENT_JST }, false);

		// Note this test will fail showing only one if the JDT stuff
		// is not included in the executing bundles (which it normally is)
		assertEquals("There should be 3 visible extensions for the pipeline viewer.", 3,
				_contentService.getVisibleExtensionIds().length);

		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT, TEST_CONTENT_JST }, true);

		refreshViewer();

		// we do this to force the rendering of the children of items[0]
		_viewer.setSelection(new StructuredSelection(_project.getFile(".project")), true); //$NON-NLS-1$

		TreeItem[] rootItems = _viewer.getTree().getItems();

		assertEquals("There should be " + _projectCount + " item(s).", _projectCount, rootItems.length); //$NON-NLS-1$

		assertTrue("The root object should be an IJavaProject, which is IAdaptable.", //$NON-NLS-1$
				rootItems[0].getData() instanceof IAdaptable);

		IProject adaptedProject = ((IAdaptable) rootItems[_projectInd].getData()).getAdapter(IProject.class);
		assertEquals(_project, adaptedProject);

		IFolder sourceFolder = _project.getFolder(IPath.fromOSString("src"));
		_viewer.add(_project, sourceFolder);

		TreeItem[] projectChildren = rootItems[_projectInd].getItems();

		if (SLEEP_LONG)
			DisplayHelper.sleep(1000000);

		// The code below looks for the below children in the Project Tree: //
		// 1) `Compressed Java`
		// 2) `Compressed Libraries`
		// and 3a) `charset.jar` if the java project uses JRE of java8 or below
		// or 3b) `java.base` if the java project uses JRE of java9 or above

		TreeItem java = findItem(projectChildren, startsWith("Compressed Java"));
		assertNotNull(java);

		_viewer.setExpandedState(java.getData(), true);

		TreeItem compressedLibrary = findItem(java.getItems(), startsWith("Compressed Libraries"));
		assertNotNull(compressedLibrary);

		TreeItem javaLibrary = findItem(java.getItems(), startsWith("charsets.jar").or(startsWith("java.base")));
		assertNotNull(javaLibrary);
	}

	private Predicate<TreeItem> startsWith(String prefix) {
		return item -> item.getText().startsWith(prefix);
	}

	private TreeItem findItem(TreeItem[] items, Predicate<TreeItem> filter) {
		return Arrays.stream(items).filter(filter).findFirst().get();
	}
}
