/*******************************************************************************
 * Copyright (c) 2008, 2023 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     IBM Corporation - fixed dead code warning
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.sorters.CommonSorterDescriptor.WrappedViewerComparator;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.TestComparatorData;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;
import org.eclipse.ui.tests.navigator.extension.TestContentProviderResource;
import org.eclipse.ui.tests.navigator.extension.TestExtensionTreeData;
import org.eclipse.ui.tests.navigator.extension.TestSorterDataAndResource;
import org.eclipse.ui.tests.navigator.extension.TestSorterResource;
import org.junit.Test;

public class SorterTest extends NavigatorTestBase {

	public SorterTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	private int _statusCount;

	// bug 262707 CommonViewerSorter gets NPE when misconfigured
	@Test
	public void testSorterMissing() throws Exception {

		TestContentProviderResource._returnBadObject = true;

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL }, true);

		refreshViewer();

		ILogListener ll = (status, plugin) -> _statusCount++;

		NavigatorPlugin.getDefault().getLog().addLogListener(ll);

		// Gets an NPE because the sorter can't find the object
		_viewer.expandAll();

		NavigatorPlugin.getDefault().getLog().removeLogListener(ll);

		// We should not get any notification because of the way that
		// sorters are found
		assertEquals("Status Count: " + _statusCount, 0, _statusCount);
	}

	// bug 231855 [CommonNavigator] CommonViewerSorter does not support
	// isSorterProperty method of ViewerComparator
	@Test
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

		IFile file;

		file = _project.getFile("AddedFile1.txt");
		file.create(new ByteArrayInputStream(new byte[]{}), true, null);
		_viewer.add(newData, file);

		file = _project.getFile("AddedFile2.txt");
		file.create(new ByteArrayInputStream(new byte[]{}), true, null);
		_viewer.add(newData, file);
	}

	// The test is disabled until bug 309746 (intermittent test failures) can be fixed
	// Bug 141724 Allow sorting to be overridden
	public void DISABLED_testSorterContentOverride() throws Exception {
		waitForModelObjects();

		INavigatorContentDescriptor desc = _contentService
		.getContentDescriptorById(TEST_CONTENT_SORTER_RESOURCE_SORTONLY);

		// Make it sort backwards so we can tell
		TestSorterResource sorter = (TestSorterResource) _contentService
			.getSorterService().findSorter(desc, _project, null, null);
		sorter._forward = false;

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY }, false);

		_viewer.expandAll();
		TreeItem[] items = _viewer.getTree().getItems();
		TreeItem[] childItems;

		//DisplayHelper.sleep(100000000);

		// Backwards
		assertEquals("p2", items[0].getText());
		assertEquals("p1", items[1].getText());
		assertEquals("Test", items[2].getText());
		assertEquals("Bluefile6.txt", items[0].getItems()[0].getText());
		assertEquals("f2", items[1].getItems()[0].getText());

		_contentService.getActivationService().deactivateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY }, false);

		refreshViewer();
		_viewer.expandAll();

		final int WAIT_COUNT = 100;

		int count = WAIT_COUNT;
		boolean passed = false;

		while (!passed) {
			// Forwards
			items = _viewer.getTree().getItems();
			assertEquals("p2", items[1].getText());
			assertEquals("p1", items[0].getText());
			// Always at the end because it's not a resource
			assertEquals("Test", items[2].getText());
			childItems = items[1].getItems();
			if (!childItems[0].getText().equals("f1") && count-- >= 0) {
				System.out.println("Not equal: " + childItems[0].getText() + " waiting...");
				DisplayHelper.sleep(100);
				continue;
			}
			assertEquals("f1", childItems[0].getText());
			childItems = items[0].getItems();
			assertEquals("f1", childItems[0].getText());
			passed = true;
		}

		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY }, false);

		refreshViewer();
		_viewer.expandAll();

		count = WAIT_COUNT;
		passed = false;

		while (!passed) {
			// Backwards
			items = _viewer.getTree().getItems();
			assertEquals("p2", items[0].getText());
			assertEquals("p1", items[1].getText());
			assertEquals("Test", items[2].getText());
			if (!items[0].getItems()[0].getText().equals("Bluefile6.txt") && count-- >= 0) {
				System.out
						.println("Not equal: " + items[0].getItems()[0].getText() + " waiting...");
				DisplayHelper.sleep(100);
				continue;
			}
			assertEquals("Bluefile6.txt", items[0].getItems()[0].getText());
			assertEquals("f2", items[1].getItems()[0].getText());
			passed = true;
		}
		// And override again
		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY_OVERRIDE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_RESOURCE_SORTONLY_OVERRIDE }, false);

		refreshViewer();
		_viewer.expandAll();

		count = WAIT_COUNT;
		passed = false;

		while (!passed) {
			// Forwards - Test in front - since the override sorter sorts
			// differently
			// than the resource extension sorter
			items = _viewer.getTree().getItems();
			assertEquals("p2", items[2].getText());
			assertEquals("p1", items[1].getText());
			assertEquals("Test", items[0].getText());
			childItems = items[2].getItems();
			if (!childItems[0].getText().equals("f1") && count-- >= 0) {
				System.out.println("Not equal: " + childItems[0].getText() + " waiting...");
				DisplayHelper.sleep(100);
				continue;
			}
			assertEquals("f1", childItems[0].getText());
			childItems = items[1].getItems();
			assertEquals("f1", childItems[0].getText());
			passed = true;
		}
	}

	// Here we want to make sure the sorting is done by the
	// highest (in the override hierarchy) content extension that
	// has a sorter
	@Test
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

	@Test
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

	@Test
	public void testComparatorAsSorter() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_COMPARATOR_MODEL }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_COMPARATOR_MODEL }, false);

		dynamicAddModelObjects();

		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent = items[_projectInd].getItem(0);
		assertEquals("BlueAddedParent", addedParent.getText());
		// The sorter for TEST_CONTENT_COMPARATOR_MODEL sorts the model objects
		// before anything else
		assertEquals("BlueAddedChild1", addedParent.getItem(0).getText());
		assertEquals("BlueChild1", addedParent.getItem(1).getText());
		assertEquals("BlueAddedFile1.txt", addedParent.getItem(2).getText());
		assertEquals("BlueAddedFile2.txt", addedParent.getItem(3).getText());

		INavigatorContentDescriptor desc = _contentService.getContentDescriptorById(TEST_CONTENT_COMPARATOR_MODEL);

		ViewerComparator sorter = _contentService.getSorterService().findSorter(desc, _project, null, null);
		assertNotNull(sorter);
		WrappedViewerComparator wrapper = (WrappedViewerComparator) sorter;
		TestComparatorData original = (TestComparatorData) wrapper.getWrappedComparator();
		Object[] dataArray = new Object[items.length];

		for (int i = 0; i < items.length; i++) {
			TreeItem treeItem = items[i];
			Object data = treeItem.getData();
			dataArray[i] = data;
			assertEquals(original.category(data), wrapper.category(data));
			assertEquals(original.isSorterProperty(data, "true"), wrapper.isSorterProperty(data, "true"));
			assertEquals(original.isSorterProperty(data, "false"), wrapper.isSorterProperty(data, "false"));
			assertEquals(original.compare(_viewer, data, items[0].getData()),
					wrapper.compare(_viewer, data, items[0].getData()));
			assertEquals(false, wrapper.isSorterProperty(data, "false"));
			assertEquals(true, wrapper.isSorterProperty(data, "true"));
		}

		Object[] copy1 = Arrays.copyOf(dataArray, dataArray.length);
		Object[] copy2 = Arrays.copyOf(dataArray, dataArray.length);
		original._forward = !original._forward;
		original.sort(_viewer, copy1);
		wrapper.sort(_viewer, copy2);
		assertArrayEquals(copy1, copy2);

		assertNotEquals(copy1[0], dataArray[0]);
	}

	@Test
	public void testSorterContentAddOverride() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_MODEL_OVERRIDE }, false);

		dynamicAddModelObjects();

		TreeItem[] items = _viewer.getTree().getItems();

		TreeItem addedParent;

		addedParent = items[_projectInd].getItem(3);
		assertEquals("BlueParent", addedParent.getText());
		addedParent = items[_projectInd].getItem(2);
		assertEquals("BlueAddedParent", addedParent.getText());

		// The sorter for TEST_CONTENT_SORTER_MODEL_OVERRIDE sorts the model
		// using a sorter that is by name
		assertEquals("BlueAddedChild1", addedParent.getItem(0).getText());
		assertEquals("BlueAddedFile1.txt", addedParent.getItem(1).getText());
		assertEquals("BlueAddedFile2.txt", addedParent.getItem(2).getText());
		assertEquals("BlueChild1", addedParent.getItem(3).getText());

	}

	public void testSorterSortOnlyMultiContent() throws Exception {

		waitForModelObjects();

		_contentService.bindExtensions(new String[] { TEST_CONTENT_SORTER_BASIC_SORTONLY_SORTER }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER_BASIC_SORTONLY_SORTER }, false);
		_contentService.bindExtensions(new String[] { TEST_CONTENT_SORTER_BASIC_A }, false);
		_contentService.getActivationService().activateExtensions(new String[] { TEST_CONTENT_SORTER_BASIC_A }, false);
		_contentService.bindExtensions(new String[] { TEST_CONTENT_SORTER_BASIC_B }, false);
		_contentService.getActivationService().activateExtensions(new String[] { TEST_CONTENT_SORTER_BASIC_B }, false);


		TreeItem[] items = _viewer.getTree().getItems();

		// the test content enables on the input of the viewer thus it is on
		// top level
		assertEquals("child1", items[0].getText());
		assertEquals("child2", items[1].getText());
		assertEquals("child3", items[2].getText());
		assertEquals("child4", items[3].getText());

	}

	@Test
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

		TreeItem[] items = _viewer.getTree().getItems();

		// p2/file6.txt (don't use _p2Ind because of sorter)
		TreeItem file1 = items[2].getItem(0);
		assertEquals("file6.txt", file1.getText());
	}


}
