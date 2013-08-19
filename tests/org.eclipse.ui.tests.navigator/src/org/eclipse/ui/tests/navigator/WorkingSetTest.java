/*******************************************************************************
 * Copyright (c) 2008, 2013 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import junit.framework.TestSuite;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.navigator.resources.actions.WorkingSetActionProvider;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class WorkingSetTest extends NavigatorTestBase {

	private static final boolean SLEEP_LONG = false;


	public static TestSuite suite() {
		TestSuite suite = new TestSuite("org.eclipse.ui.tests.navigator.WorkingSetTest");
		suite.addTest(new WorkingSetTest("testEmptyWindowWorkingSet"));
		suite.addTest(new WorkingSetTest("testMissingProjectsInWorkingSet"));
		suite.addTest(new WorkingSetTest("testTopLevelWorkingSet"));
		suite.addTest(new WorkingSetTest("testTopLevelChange"));
		suite.addTest(new WorkingSetTest("testMultipleWorkingSets"));
		suite.addTest(new WorkingSetTest("testWorkingSetFilter"));
		suite.addTest(new WorkingSetTest("testDeletedAndRecreated"));
		return suite;
	}

	public WorkingSetTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
	}

	public WorkingSetTest(String name) {
		super(name);
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
	}

	// Bug 157877 when using empty window working set, it should show all
	public void testEmptyWindowWorkingSet() throws Exception {

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);

		IWorkingSet workingSet = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getAggregateWorkingSet();

		// Set the filter to window working set (which should be empty)
		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, workingSet);
		l.propertyChange(event);

		TreeItem[] items = _viewer.getTree().getItems();
		assertTrue("There should be some items.", items.length > 0);
		assertEquals(null, _commonNavigator.getWorkingSetLabel());
	}

	// Bug 212389 projects are not shown when they are not in the working set,
	// but their children are
	public void testMissingProjectsInWorkingSet() throws Exception {

		IFile f1 = _p1.getFile("f1");

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1", new IAdaptable[] { f1 });

		AggregateWorkingSet agWorkingSet = new AggregateWorkingSet("AgWs", "Ag Working Set",
				new IWorkingSet[] { workingSet });

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, agWorkingSet);
		l.propertyChange(event);

		DisplayHelper.runEventLoop(Display.getCurrent(), 100);

		// DisplayHelper.sleep(Display.getCurrent(), 10000000);

		TreeItem[] items = _viewer.getTree().getItems();
		// The bug is here where the first item is a IFile, not the enclosing
		// project
		assertTrue("First item needs to be project", items[0].getData().equals(_p1));
		assertEquals("ws1", _commonNavigator.getWorkingSetLabel());
	}

	// bug 220090 test that working sets are shown when selected locally (not
	// using the window working set)
	public void testTopLevelWorkingSet() throws Exception {

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);

		IExtensionStateModel extensionStateModel = _contentService
				.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1", new IAdaptable[] { _p1 });

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, workingSet);
		l.propertyChange(event);

		// DisplayHelper.sleep(Display.getCurrent(), 10000000);

		_viewer.expandAll();

		TreeItem[] items = _viewer.getTree().getItems();
		// The bug is here where the first item is a IFile, not the enclosing
		// project
		assertTrue("First item needs to be working set", items[0].getData().equals(workingSet));
		assertEquals("ws1", _commonNavigator.getWorkingSetLabel());

		// bug 268250 [CommonNavigator] Project labels missing in Project
		// Explorer when working sets are top level elements
		TreeItem projectItem = items[0].getItem(0);
		assertEquals("p1", projectItem.getText());

	}

	// bug 244174 test property to switch back and forth between working sets
	// as top level and not
	public void testTopLevelChange() throws Exception {

		IExtensionStateModel extensionStateModel = _contentService
				.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		// Force the content provider to be loaded so that it responds to the
		// working set events
		INavigatorContentExtension ce = _contentService
				.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
		ce.getContentProvider();

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1", new IAdaptable[] { _p1 });

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);
		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, workingSet);
		l.propertyChange(event);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		IWorkingSet[] activeWorkingSets = activePage.getWorkingSets();
		activePage.setWorkingSets(new IWorkingSet[] { workingSet });

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);
		refreshViewer();

		TreeItem[] items = _viewer.getTree().getItems();

		assertTrue("First item needs to be working set", items[0].getData().equals(workingSet));

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, false);
		refreshViewer();

		items = _viewer.getTree().getItems();
		assertTrue("First item needs to be project", items[0].getData().equals(_p1));

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);
		refreshViewer();

		items = _viewer.getTree().getItems();
		assertTrue("First item needs to be working set", items[0].getData().equals(workingSet));

		// Restore active working sets
		activePage.setWorkingSets(activeWorkingSets);
	}

	public void testMultipleWorkingSets() throws Exception {

		// Force the content provider to be loaded so that it responds to the
		// working set events
		INavigatorContentExtension ce = _contentService
				.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
		ce.getContentProvider();

		IWorkingSet workingSet1 = new WorkingSet("ws1", "ws1", new IAdaptable[] { _p1 });
		IWorkingSet workingSet2 = new WorkingSet("ws2", "ws2", new IAdaptable[] { _p1 });

		AggregateWorkingSet agWorkingSet = new AggregateWorkingSet("AgWs", "Ag Working Set",
				new IWorkingSet[] { workingSet1, workingSet2 });

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, agWorkingSet);
		l.propertyChange(event);

		assertEquals(WorkbenchNavigatorMessages.WorkingSetActionProvider_multipleWorkingSets,
				_commonNavigator.getWorkingSetLabel());
	}

	// bug 262096 [CommonNavigator] Changing *any* filter breaks working sets
	// filter
	public void testWorkingSetFilter() throws Exception {

		_contentService.bindExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1", new IAdaptable[] { _p1 });

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);
		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, workingSet);
		l.propertyChange(event);

		ViewerFilter vf[];
		vf = _viewer.getFilters();
		boolean found = false;
		for (int i = 0; i < vf.length; i++) {
			if (vf[i] instanceof ResourceWorkingSetFilter)
				found = true;
		}
		assertTrue(found);

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(
				new String[] { TEST_FILTER_P1, TEST_FILTER_P2 });

		vf = _viewer.getFilters();
		found = false;
		for (int i = 0; i < vf.length; i++) {
			if (vf[i] instanceof ResourceWorkingSetFilter)
				found = true;
		}
		assertTrue("Working set filter is gone, oh my!", found);
	}

	// Bug 224703 - Project explorer doesn't show recreated working set
	// (this test does not show the problem since it was in the
	// WorkingSetSelectionDialog, but still it's nice to have)
	public void testDeletedAndRecreated() throws Exception {

		INavigatorContentExtension ce = _contentService
				.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
		ce.getContentProvider();

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

		IWorkingSet ws1 = workingSetManager.createWorkingSet("ws1", new IAdaptable[] { _p1 });
		workingSetManager.addWorkingSet(ws1);
		IWorkingSet ws2 = workingSetManager.createWorkingSet("ws2", new IAdaptable[] { _p2 });
		workingSetManager.addWorkingSet(ws2);

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(_contentService, _actionService, WorkingSetActionProvider.class);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		activePage.setWorkingSets(new IWorkingSet[] { ws1, ws2 });

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null, ws2);
		l.propertyChange(event);

		DisplayHelper.runEventLoop(Display.getCurrent(), 100);

		TreeItem[] items = _viewer.getTree().getItems();
		assertTrue(items[0].getData().equals(_p2));

		l = provider.getFilterChangeListener();
		event = new PropertyChangeEvent(this, WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				ws1);
		l.propertyChange(event);
		DisplayHelper.runEventLoop(Display.getCurrent(), 100);
		items = _viewer.getTree().getItems();
		assertTrue(items[0].getData().equals(_p1));

		workingSetManager.removeWorkingSet(ws2);

		ws2 = workingSetManager.createWorkingSet("ws2", new IAdaptable[] { _p2 });
		workingSetManager.addWorkingSet(ws2);

		l = provider.getFilterChangeListener();
		event = new PropertyChangeEvent(this, WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				ws2);
		l.propertyChange(event);
		DisplayHelper.runEventLoop(Display.getCurrent(), 100);
		items = _viewer.getTree().getItems();
		assertTrue(items[0].getData().equals(_p2));

		if (SLEEP_LONG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

	}

}
