/*******************************************************************************
 * Copyright (c) 2008, 2015 Oakland Software Incorporated and others.
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
package org.eclipse.ui.tests.navigator.cdt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.NavigatorTestBase;
import org.eclipse.ui.tests.navigator.util.ProjectUnzipUtil;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;
import org.junit.Test;

/**
 * This simulates the CDT usage of the CNF.
 *
 * @since 3.3
 *
 */
public class CdtTest extends NavigatorTestBase {

	private static final String CPROJECT_NAME = "Chello";
	private static final boolean SLEEP_LONG = false;
	private static final boolean IS_JDT_DISABLED = false;

	public CdtTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
	}

	@Test
	public void testCdt1() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_C_CONTENT }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_C_CONTENT }, false);

		TestWorkspace.initProject(
				new ProjectUnzipUtil(IPath.fromOSString("testdata/cproject.zip"), new String[] { CPROJECT_NAME }),
				CPROJECT_NAME);

		refreshViewer();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				CPROJECT_NAME);

		if (SLEEP_LONG)
			DisplayHelper.sleep(1000000000);

		_viewer.setExpandedState(project, true);

		TreeItem[] rootItems = _viewer.getTree().getItems();
		for (TreeItem rootItem : rootItems) {
			if (rootItem.getText().equals(CPROJECT_NAME)) {
				TreeItem[] items = rootItem.getItems();
				assertEquals("CL: CElement: Debug", items[0].getText());
				assertEquals("CL: CElement: src", items[1].getText());

				if (IS_JDT_DISABLED) {
					// This is how it appears if you disable the JDT NCE, it
					// uses the CDT label provider
					assertEquals("CL: chello/test.c", items[2].getText());
					assertEquals("CL: chello/test.txt", items[3].getText());
				} else {
					// This is what's happening now, it's probably good enough
					assertEquals("test.c", items[2].getText());
					assertEquals("test.txt", items[3].getText());
				}

				break;
			}
		}

	}

}
