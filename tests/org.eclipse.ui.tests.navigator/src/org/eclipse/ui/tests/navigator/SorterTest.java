/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     IBM Corporation - fixed dead code warning
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestExtensionTreeData;
import org.eclipse.ui.tests.navigator.extension.TestContentProviderResource;
import org.eclipse.ui.tests.navigator.extension.TestSorterDataAndResource;
import org.eclipse.ui.tests.navigator.extension.TestSorterResource;

public class SorterTest extends NavigatorTestBase {

	public SorterTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	private int _statusCount;

	// bug 262707 CommonViewerSorter gets NPE when misconfigured
	public void testSorterMissing() throws Exception {

		TestContentProviderResource._returnBadObject = true;

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, true);

		refreshViewer();

		ILogListener ll = new ILogListener() {
			public void logging(IStatus status, String plugin) {
				_statusCount++;
			}
		};

		NavigatorPlugin.getDefault().getLog().addLogListener(ll);

		// Gets an NPE because the sorter can't find the object
		_viewer.expandAll();

		NavigatorPlugin.getDefault().getLog().removeLogListener(ll);

		// We should not get any notification because of the way that
		// sorters are found
		assertTrue("Status Count: " + _statusCount, _statusCount == 0);
	}

	// bug 231855 [CommonNavigator] CommonViewerSorter does not support
	// isSorterProperty method of ViewerComparator
	public void testSorterProperty() throws Exception {

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE }, true);

		refreshViewer();

		_viewer.update(_p1, new String[] { "prop1" });
		_viewer.expandAll();
 
		assertEquals("prop1", TestSorterResource._sorterProperty);
		assertEquals(_p1, TestSorterResource._sorterElement);
	}

	protected void dynamicAddModelObjects() throws Exception {
		_viewer.setExpandedState(_project, true);

		Properties props = new Properties();
		props.put("AddedParent", "Child1,AddedChild1");

		TestExtensionTreeData newData = new TestExtensionTreeData(
				TestContentProvider._modelRoot, "AddedParent", props, null);

		_viewer.add(_project, newData);
		_viewer.setExpandedState(newData, true);

		_viewer.add(newData, _project.getFile("AddedFile1.txt"));
		_viewer.add(newData, _project.getFile("AddedFile2.txt"));
	}

	/* 
	 * This test will not work properly until real sort overriding is implemented.  It worked 
	 * for a while in 3.5 as a side effect of bug 273660, however with the correct fix
	 * in bug 287103 the tests needs to be disabled.
	 */
	public void XXtestSorterContentOverride() throws Exception {
		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);

		INavigatorContentDescriptor desc = _contentService
				.getContentDescriptorById(TEST_CONTENT_SORTER_MODEL_OVERRIDE);

		// Make it sort backwards so we can tell
		TestSorterDataAndResource sorter = (TestSorterDataAndResource) _contentService
				.getSorterService().findSorter(desc, _project, null, null);
		sorter._forward = false;

		_viewer.setExpandedState(_project,	true);
		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent = items[_projectInd].getItem(3);
		_viewer.setExpandedState(addedParent.getData(), true);
		assertEquals("BlueParent", addedParent.getText());
		
		// Backwards sort because of override
		assertEquals("BlueChild2", addedParent.getItem(0).getText());
		assertEquals("BlueChild1", addedParent.getItem(1).getText());

	}

	// Here we want to make sure the sorting is done by the 
	// highest (in the override hierarchy) content extension that
	// has a sorter
	public void testSorterContentOverrideNoSort() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE_NOSORT }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE_NOSORT }, false);

		INavigatorContentDescriptor desc = _contentService
				.getContentDescriptorById(TEST_CONTENT_SORTER_MODEL_OVERRIDE);

		// Make it sort backwards so we can tell
		TestSorterDataAndResource sorter = (TestSorterDataAndResource) _contentService
				.getSorterService().findSorter(desc, _project, null, null);
		sorter._forward = false;

		_viewer.setExpandedState(_project,	true);

		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent = items[_projectInd].getItem(0);
		_viewer.setExpandedState(addedParent.getData(), true);
		assertEquals("BlueParent", addedParent.getText());
		// Forward sort
		assertEquals("BlueChild1", addedParent.getItem(0).getText());
		assertEquals("BlueChild2", addedParent.getItem(1).getText());

	}

	public void testSorterContentAdd() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, false);

		dynamicAddModelObjects();

		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent = items[_projectInd].getItem(0);
		assertEquals("BlueAddedParent", addedParent.getText());
		// The sorter for TEST_CONTENT_SORTER_MODEL sorts the model objects
		// before anything else
		assertEquals("BlueAddedChild1", addedParent.getItem(0).getText());
		assertEquals("BlueChild1", addedParent.getItem(1).getText());
		assertEquals("BlueAddedFile1.txt", addedParent.getItem(2).getText());
		assertEquals("BlueAddedFile2.txt", addedParent.getItem(3).getText());

	}

	public void testSorterContentAddOverride() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);

		dynamicAddModelObjects();

		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent;
		
		addedParent = items[_projectInd].getItem(1);
		assertEquals("BlueParent", addedParent.getText());
		addedParent = items[_projectInd].getItem(0);
		assertEquals("BlueAddedParent", addedParent.getText());
		
		// The sorter for TEST_CONTENT_SORTER_MODEL_OVERRIDE sorts the model
		// using a sorter that is by name
		assertEquals("BlueAddedChild1", addedParent.getItem(0).getText());
		assertEquals("BlueAddedFile1.txt", addedParent.getItem(1).getText());
		assertEquals("BlueAddedFile2.txt", addedParent.getItem(2).getText());
		assertEquals("BlueChild1", addedParent.getItem(3).getText());

	}

	public void testSorterResource() throws Exception {

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE }, true);

		refreshViewer();

		INavigatorContentDescriptor desc = _contentService
				.getContentDescriptorById(TEST_CONTENT_SORTER_RESOURCE);

		TestSorterResource sorter = (TestSorterResource) _contentService
				.getSorterService().findSorter(desc, _p2, null, null);
		sorter._forward = false;

		IStructuredSelection sel;
		// p2/f1
		IContainer cont = (IContainer) _p2.members()[1];
		sel = new StructuredSelection(cont.members()[0]);
		_viewer.setSelection(sel);
		_viewer.setExpandedState(cont, true);

		if (false)
			DisplayHelper.sleep(1000000000);

		TreeItem[] items = _viewer.getTree().getItems();

		// p2/file6.txt (don't use _p2Ind because of sorter)
		TreeItem file1 = items[2].getItem(0);
		assertEquals("file6.txt", file1.getText());
	}

	public void NOtestSorterResourceOverride() throws Exception {

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_OVERRIDE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_OVERRIDE }, false);
		
		refreshViewer();

		INavigatorContentDescriptor desc = _contentService
				.getContentDescriptorById(TEST_CONTENT_SORTER_RESOURCE_OVERRIDE);

		TestSorterResource sorter = (TestSorterResource) _contentService
				.getSorterService().findSorter(desc, _p2, null, null);
		sorter._forward = false;

		IStructuredSelection sel;
		// p2/f1
		IContainer cont = (IContainer) _p2.members()[1];
		sel = new StructuredSelection(cont.members()[0]);
		_viewer.setSelection(sel);
		// _viewer.setExpandedState(cont, true);

		if (false)
			DisplayHelper.sleep(1000000000);

		TreeItem[] items = _viewer.getTree().getItems();

		// p2/file6.txt (don't use _p2Ind because of the reverse sort)
		TreeItem file1 = items[2].getItem(0);
		assertEquals("file6.txt", file1.getText());
	}

	public void NOtestSorterResourceOverrideSorter() throws Exception {

		_contentService.bindExtensions(new String[] {
				TEST_CONTENT_SORTER_RESOURCE_OVERRIDE,
				TEST_CONTENT_SORTER_RESOURCE_OVERRIDE_SORTER }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_OVERRIDE,
						TEST_CONTENT_SORTER_RESOURCE_OVERRIDE_SORTER }, false);

		refreshViewer();

		INavigatorContentDescriptor desc = _contentService
				.getContentDescriptorById(TEST_CONTENT_SORTER_RESOURCE_OVERRIDE_SORTER);

		TestSorterResource sorter = (TestSorterResource) _contentService
				.getSorterService().findSorter(desc, _p2, null, null);
		sorter._forward = false;

		IStructuredSelection sel;
		// p2/f1
		IContainer cont = (IContainer) _p2.members()[1];
		sel = new StructuredSelection(cont.members()[0]);
		_viewer.setSelection(sel);
		//_viewer.setExpandedState(cont, true);

		if (false)
			DisplayHelper.sleep(1000000000);

		TreeItem[] items = _viewer.getTree().getItems();

		// p2/file6.txt (don't use _p2Ind because of the reverse sort)
		TreeItem file1 = items[2].getItem(0);
		assertEquals("file6.txt", file1.getText());
	}

}
