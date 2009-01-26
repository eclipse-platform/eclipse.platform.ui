/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.navigator.resources.actions.WorkingSetActionProvider;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

public class WorkingSetTest extends NavigatorTestBase {

	public WorkingSetTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
		_initTestData = false;
	}

	// Bug 157877 when using empty window working set, it should show all
	public void testEmptyWindowWorkingSet() throws Exception {
		TestWorkspace.init();

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(contentService, _actionService,
						WorkingSetActionProvider.class);

		IWorkingSet workingSet = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getAggregateWorkingSet();

		// Set the filter to window working set (which should be empty)
		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				workingSet);
		l.propertyChange(event);

		TreeItem[] items = viewer.getTree().getItems();
		assertTrue("There should be some items.", items.length > 0);
		assertEquals(null, ((ProjectExplorer) _commonNavigator)
				.getWorkingSetLabel());
	}

	// Bug 212389 projects are not shown when they are not in the working set,
	// but their children are
	public void testMissingProjectsInWorkingSet() throws Exception {

		IProject p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
		p1.create(null);
		p1.open(null);
		IFile f1 = p1.getFile("f1");
		f1.create(new ByteArrayInputStream(new byte[] {}), true, null);

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(contentService, _actionService,
						WorkingSetActionProvider.class);

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1",
				new IAdaptable[] { f1 });

		AggregateWorkingSet agWorkingSet = new AggregateWorkingSet("AgWs",
				"Ag Working Set", new IWorkingSet[] { workingSet });

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				agWorkingSet);
		l.propertyChange(event);

		DisplayHelper.runEventLoop(Display.getCurrent(), 100);

		// DisplayHelper.sleep(Display.getCurrent(), 10000000);

		TreeItem[] items = viewer.getTree().getItems();
		// The bug is here where the first item is a IFile, not the enclosing
		// project
		assertTrue("First item needs to be project", items[0].getData().equals(
				p1));
		assertEquals("ws1", ((ProjectExplorer) _commonNavigator)
				.getWorkingSetLabel());
	}

	// bug 220090 test that working sets are shown when selected locally (not
	// using the window working set)
	public void testTopLevelWorkingSet() throws Exception {

		IProject p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
		p1.create(null);
		p1.open(null);
		IFile f1 = p1.getFile("f1");
		f1.create(new ByteArrayInputStream(new byte[] {}), true, null);

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(contentService, _actionService,
						WorkingSetActionProvider.class);

		IExtensionStateModel extensionStateModel = contentService
				.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1",
				new IAdaptable[] { p1 });

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				workingSet);
		l.propertyChange(event);

		// DisplayHelper.sleep(Display.getCurrent(), 10000000);

		TreeItem[] items = viewer.getTree().getItems();
		// The bug is here where the first item is a IFile, not the enclosing
		// project
		assertTrue("First item needs to be working set", items[0].getData()
				.equals(workingSet));
		assertEquals("ws1", ((ProjectExplorer) _commonNavigator)
				.getWorkingSetLabel());
	}

	// bug 244174 test property to switch back and forth between working sets
	// as top level and not
	public void testTopLevelChange() throws Exception {

		IProject p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
		p1.create(null);
		p1.open(null);
		IFile f1 = p1.getFile("f1");
		f1.create(new ByteArrayInputStream(new byte[] {}), true, null);

		IExtensionStateModel extensionStateModel = contentService
				.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		// Force the content provider to be loaded so that it responds to the
		// working set events
		INavigatorContentExtension ce = contentService
				.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
		ce.getContentProvider();

		IWorkingSet workingSet = new WorkingSet("ws1", "ws1",
				new IAdaptable[] { p1 });

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(contentService, _actionService,
						WorkingSetActionProvider.class);
		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				workingSet);
		l.propertyChange(event);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		activePage.setWorkingSets(new IWorkingSet[] { workingSet });

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);
		refreshViewer();

		TreeItem[] items = viewer.getTree().getItems();

		assertTrue("First item needs to be working set", items[0].getData()
				.equals(workingSet));

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, false);
		refreshViewer();

		items = viewer.getTree().getItems();
		assertTrue("First item needs to be project", items[0].getData().equals(
				p1));

		extensionStateModel.setBooleanProperty(
				WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, true);
		refreshViewer();

		items = viewer.getTree().getItems();
		assertTrue("First item needs to be working set", items[0].getData()
				.equals(workingSet));
	}

	public void testMultipleWorkingSets() throws Exception {

		IProject p1 = ResourcesPlugin.getWorkspace().getRoot().getProject("p1");
		p1.create(null);
		p1.open(null);
		IProject p2 = ResourcesPlugin.getWorkspace().getRoot().getProject("p2");
		p2.create(null);
		p2.open(null);

		// Force the content provider to be loaded so that it responds to the
		// working set events
		INavigatorContentExtension ce = contentService
				.getContentExtensionById(WorkingSetsContentProvider.EXTENSION_ID);
		ce.getContentProvider();

		IWorkingSet workingSet1 = new WorkingSet("ws1", "ws1",
				new IAdaptable[] { p1 });
		IWorkingSet workingSet2 = new WorkingSet("ws2", "ws2",
				new IAdaptable[] { p1 });

		AggregateWorkingSet agWorkingSet = new AggregateWorkingSet("AgWs",
				"Ag Working Set",
				new IWorkingSet[] { workingSet1, workingSet2 });

		WorkingSetActionProvider provider = (WorkingSetActionProvider) TestAccessHelper
				.getActionProvider(contentService, _actionService,
						WorkingSetActionProvider.class);

		IPropertyChangeListener l = provider.getFilterChangeListener();
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				WorkingSetFilterActionGroup.CHANGE_WORKING_SET, null,
				agWorkingSet);
		l.propertyChange(event);

		assertEquals(
				WorkbenchNavigatorMessages.WorkingSetActionProvider_multipleWorkingSets,
				((ProjectExplorer) _commonNavigator).getWorkingSetLabel());
	}

}
