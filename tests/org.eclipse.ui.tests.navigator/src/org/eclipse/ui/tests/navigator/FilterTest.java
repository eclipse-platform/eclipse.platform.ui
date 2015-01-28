/*******************************************************************************
 * Copyright (c) 2010, 2015 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.junit.Test;

public class FilterTest extends NavigatorTestBase {

	public FilterTest() {
		_navigatorInstanceId = TEST_VIEWER_FILTER;
	}

	// bug 292813 Add API for high level activation of filters
	@Test
	public void testFilterActivation() throws Exception {

		_contentService.bindExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		_viewer.expandAll();

		TreeItem[] items;
		items = _viewer.getTree().getItems();
		assertEquals(3, items.length);

		NavigatorContentService ncs = (NavigatorContentService) _contentService;

		// Bug 305703 Make sure that contribution memory does not leak on filters
		assertEquals(0, ncs.getContributionMemorySize());

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(
				new String[] { TEST_FILTER_P1, TEST_FILTER_P2 });

		items = _viewer.getTree().getItems();
		assertEquals(1, items.length);
		assertEquals("Test", items[0].getText());
		assertEquals(0, ncs.getContributionMemorySize());

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(
				new String[] { TEST_FILTER_P1 });

		items = _viewer.getTree().getItems();
		assertEquals(2, items.length);
		assertEquals("p2", items[0].getText());
		assertEquals(0, ncs.getContributionMemorySize());

		_contentService.getFilterService().activateFilterIdsAndUpdateViewer(new String[] {});

		items = _viewer.getTree().getItems();
		assertEquals(3, items.length);
		assertEquals("p1", items[0].getText());
		assertEquals("p2", items[1].getText());
		assertEquals(0, ncs.getContributionMemorySize());

	}

}
