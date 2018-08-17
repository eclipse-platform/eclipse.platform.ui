/*******************************************************************************
 * Copyright (c) 2010, 2015 Oakland Software Incorporated and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *     Bachmann electronic GmbH - Bug 447530 - adding a test for active non visible filters
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.junit.Test;

public class FilterTest extends NavigatorTestBase {

	private static final String TEST_FILTER_ACTIVE_NOT_VISIBLE = "org.eclipse.ui.tests.navigator.filters.nonvisibleactive";

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

	// bug 447530, when a filter is active by default but not visible in the ui,
	// it must still be active after
	// restoring the active filters from the preferences.
	@Test
	public void testNonVisibleFilters() {
		_contentService.getFilterService().persistFilterActivationState();
		// "restore" by creating a new instance of the serivce
		NavigatorFilterService filterService = new NavigatorFilterService((NavigatorContentService) _contentService);
		assertTrue(filterService.isActive(TEST_FILTER_ACTIVE_NOT_VISIBLE));
	}

}
