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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.filters.UpdateActiveFiltersOperation;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

public class NavigatorTestBase extends TestCase {

	public static final String COMMON_NAVIGATOR_RESOURCE_EXT = "org.eclipse.ui.navigator.resourceContent"; //$NON-NLS-1$

	public static final String COMMON_NAVIGATOR_JAVA_EXT = "org.eclipse.jdt.java.ui.javaContent"; //$NON-NLS-1$

	public static final String COMMON_NAVIGATOR_TEST_EXT = "org.eclipse.ui.tests.navigator.testContent"; //$NON-NLS-1$

	protected String _navigatorInstanceId;

	protected Set expectedChildren = new HashSet();

	protected IProject project;

	protected CommonViewer viewer;

	protected CommonNavigator _commonNavigator;

	protected INavigatorContentService contentService;
	protected NavigatorActionService _actionService;

	protected boolean _initTestData = true;

	protected void setUp() throws Exception {

		if (_navigatorInstanceId == null) {
			throw new RuntimeException(
					"Set the _navigatorInstanceId in the constructor");
		}

		if (_initTestData) {

			TestWorkspace.init();

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			project = root.getProject("Test"); //$NON-NLS-1$

			expectedChildren.add(project.getFolder("src")); //$NON-NLS-1$
			expectedChildren.add(project.getFolder("bin")); //$NON-NLS-1$
			expectedChildren.add(project.getFile(".project")); //$NON-NLS-1$
			expectedChildren.add(project.getFile(".classpath")); //$NON-NLS-1$ 
			expectedChildren.add(project.getFile("model.properties")); //$NON-NLS-1$
		}

		EditorTestHelper.showView(_navigatorInstanceId, true);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		_commonNavigator = (CommonNavigator) activePage
				.findView(_navigatorInstanceId);
		_commonNavigator.setFocus();
		viewer = (CommonViewer) _commonNavigator.getAdapter(CommonViewer.class);

		contentService = viewer.getNavigatorContentService();
		_actionService = _commonNavigator.getNavigatorActionService();

		IUndoableOperation updateFilters = new UpdateActiveFiltersOperation(
				viewer, new String[0], true);
		updateFilters.execute(null, null);
	}

	protected void tearDown() throws Exception {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			projects[i].delete(true, null);
		}
		// Hide it, we want a new one each time
		EditorTestHelper.showView(_navigatorInstanceId, false);
	}

}
