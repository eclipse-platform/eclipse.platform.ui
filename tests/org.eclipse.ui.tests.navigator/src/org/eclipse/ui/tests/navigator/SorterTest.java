/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *.....IBM Corporation - fixed dead code warning
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.tests.navigator.extension.TestDataSorter;
import org.eclipse.ui.tests.navigator.extension.TestContentProviderResource;

public class SorterTest extends NavigatorTestBase {

	public SorterTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	private int _statusCount;

	// bug 262707 CommonViewerSorter gets NPE when misconfigured
	public void testSorterMissing() throws Exception {

		TestContentProviderResource._returnBadObject = true;

		_contentService.bindExtensions(new String[] { TEST_CONTENT_SORTER },
				false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER }, true);

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

	// bug 231855 [CommonNavigator] CommonViewerSorter does not support isSorterProperty method of ViewerComparator 
	public void testSorterProperty() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_SORTER },
				false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_SORTER }, true);

		refreshViewer();

		_viewer.update(_p1, new String[]{"prop1"});
		_viewer.expandAll();
		
		assertEquals("prop1", TestDataSorter._sorterProperty);
		assertEquals(_p1, TestDataSorter._sorterElement);
	}


}
