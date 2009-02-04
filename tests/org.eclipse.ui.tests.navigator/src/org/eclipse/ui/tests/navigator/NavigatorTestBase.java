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
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.filters.UpdateActiveFiltersOperation;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.navigator.extension.TestResourceContentProvider;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

public class NavigatorTestBase extends TestCase {

	public static final String COMMON_NAVIGATOR_RESOURCE_EXT = "org.eclipse.ui.navigator.resourceContent"; //$NON-NLS-1$

	public static final String COMMON_NAVIGATOR_JAVA_EXT = "org.eclipse.jdt.java.ui.javaContent"; //$NON-NLS-1$

	public static final String COMMON_NAVIGATOR_TEST_EXT = "org.eclipse.ui.tests.navigator.testContent"; //$NON-NLS-1$

	public static final String TEST_CONTENT = "org.eclipse.ui.tests.navigator.testContent";
	public static final String TEST_CONTENT1 = "org.eclipse.ui.tests.navigator.testOverriddenContent1";
	public static final String TEST_CONTENT2 = "org.eclipse.ui.tests.navigator.testOverriddenContent2";
	public static final String TEST_OVERRIDE1= "org.eclipse.ui.tests.navigator.testOverride1";
	public static final String TEST_OVERRIDE2 = "org.eclipse.ui.tests.navigator.testOverride2";
	public static final String TEST_SORTER_CONTENT = "org.eclipse.ui.tests.navigator.testSorterContent";

	public static final String TEST_DROP_COPY_CONTENT = "org.eclipse.ui.tests.navigator.testDropCopy";
	
	protected String _navigatorInstanceId;

	protected Set _expectedChildren = new HashSet();

	protected IProject _project;
	protected IProject _p1;
	protected IProject _p2;

	protected static final int _p1Ind = 0;
	protected static final int _p2Ind = 1;
	protected static final int _projectInd = 2;
	
	protected static int _projectCount;
	
	protected CommonViewer _viewer;

	protected CommonNavigator _commonNavigator;

	protected INavigatorContentService _contentService;
	protected NavigatorActionService _actionService;

	protected boolean _initTestData = true;

	protected void setUp() throws Exception {

		if (_navigatorInstanceId == null) {
			throw new RuntimeException(
					"Set the _navigatorInstanceId in the constructor");
		}

		TestResourceContentProvider.resetTest();
		
		if (_initTestData) {

			TestWorkspace.init();

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			_project = root.getProject("Test"); //$NON-NLS-1$

			_expectedChildren.add(_project.getFolder("src")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFolder("bin")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFile(".project")); //$NON-NLS-1$
			_expectedChildren.add(_project.getFile(".classpath")); //$NON-NLS-1$ 
			_expectedChildren.add(_project.getFile("model.properties")); //$NON-NLS-1$
			
			_p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
			_p1.open(null);
			_p2= ResourcesPlugin.getWorkspace().getRoot().getProject("p2");
			_p2.open(null);
			_projectCount = 3;
			
		}

		EditorTestHelper.showView(_navigatorInstanceId, true);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		_commonNavigator = (CommonNavigator) activePage
				.findView(_navigatorInstanceId);
		_commonNavigator.setFocus();
		_viewer = (CommonViewer) _commonNavigator.getAdapter(CommonViewer.class);
		
		refreshViewer();

		_contentService = _viewer.getNavigatorContentService();
		_actionService = _commonNavigator.getNavigatorActionService();

		IUndoableOperation updateFilters = new UpdateActiveFiltersOperation(
				_viewer, new String[0], true);
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
	
	// Need this to workaround a problem of the DecoratingStyledCellLabelProvider. 
	// The method returns early because there is a (background) decoration pending.
	protected void refreshViewer() {
		TreeItem[] rootItems = _viewer.getTree().getItems();
		if (rootItems.length > 0)
			rootItems[0].setText("");
		_viewer.refresh();
	}

}
