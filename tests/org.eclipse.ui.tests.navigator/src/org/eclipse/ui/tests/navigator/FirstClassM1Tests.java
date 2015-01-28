/*******************************************************************************
 * Copyright (c) 2009, 2015 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 ******************************************************************************/

package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.navigator.m12.model.M1Project;
import org.junit.Test;

/**
 * M1/M2 tests with M1 as a first class provider (i.e. override policy set to
 * InvokeAlways...)
 */
public class FirstClassM1Tests extends NavigatorTestBase {
	public FirstClassM1Tests() {
		_navigatorInstanceId = TEST_CONTENT_M12_VIEW;
	}

	/**
	 * Adaptable project children test: verifies that project children are
	 * visible in the project explorer even though the M1 provider has replaced
	 * IProjects with M1Projects, which adapt to IResource. This test fails in
	 * both Ganymede and Galileo because the resourceContent provider triggers
	 * for M1Project, then fails to provide any children. See Bug #285353
	 */
	@Test
	public void testM1ProjectHasChildren() throws Exception {
		String[] EXTENSIONS = new String[] { COMMON_NAVIGATOR_RESOURCE_EXT,
				// The issue only arises if the override policy is
				// InvokeAlways...
				TEST_CONTENT_M12_M1_CONTENT_FIRST_CLASS,
				TEST_CONTENT_M12_M2_CONTENT };
		_contentService.bindExtensions(EXTENSIONS, false);
		_contentService.getActivationService().activateExtensions(EXTENSIONS,
				true);

		TreeItem[] rootItems = _viewer.getTree().getItems();
		TreeItem p1Item = rootItems[_p1Ind];

		assertEquals("P1 tree item should be an M1Project", M1Project.class,
				p1Item.getData().getClass());

		_expand(rootItems);
		TreeItem[] p1Children = p1Item.getItems();

		assertEquals("Project should have 3 children", 3, p1Children.length);

	}

}