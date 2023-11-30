/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.BasicWorkingSetElementAdapter;
import org.eclipse.ui.tests.menus.ObjectContributionClasses;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests BasicWorkingSetElementAdapter.
 *
 * @since 3.5
 */
public class IWorkingSetElementAdapterTests {

	String data = "org.eclipse.ui.tests.menus.ObjectContributionClasses$ICommon;adapt=true,org.eclipse.ui.tests.menus.ObjectContributionClasses$IF;adapt=true";
	BasicWorkingSetElementAdapter adapter;

	@Before
	public void setUp() throws Exception {
		adapter = new BasicWorkingSetElementAdapter();
		adapter.setInitializationData(null, "class", data);
	}

	@After
	public void tearDown() throws Exception {
		adapter.dispose();
	}

	@Test
	public void testBasicWorkingSetElementAdapter_Direct() {
		IAdaptable [] result = adapter.adaptElements(null, new IAdaptable[] {new ObjectContributionClasses.Common()});
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.Common.class, result[0].getClass());
	}

	@Test
	public void testBasicWorkingSetElementAdapter_Inheritance() {
		IAdaptable [] result = adapter.adaptElements(null, new IAdaptable[] {new ObjectContributionClasses.D()});
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.D.class, result[0].getClass());
	}

	@Test
	public void testBasicWorkingSetElementAdapter_IAdaptable() {
		IAdaptable[] result = adapter.adaptElements(null,
				new IAdaptable[] { new ObjectContributionClasses.E()  });
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.F.class, result[0].getClass());
	}

	@Test
	public void testBasicWorkingSetElementAdapter_AdapterManager() {
		IAdaptable[] result = adapter.adaptElements(null,
				new IAdaptable[] { new ObjectContributionClasses.E1()  });
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.Common.class, result[0].getClass());
	}
}
