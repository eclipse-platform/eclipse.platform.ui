/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.BasicWorkingSetElementAdapter;
import org.eclipse.ui.tests.menus.ObjectContributionClasses;

/**
 * Tests BasicWorkingSetElementAdapter.
 * 
 * @since 3.5
 *
 */
public class IWorkingSetElementAdapterTests extends TestCase {
	
	String data = "org.eclipse.ui.tests.menus.ObjectContributionClasses$ICommon;adapt=true,org.eclipse.ui.tests.menus.ObjectContributionClasses$IF;adapt=true";
	BasicWorkingSetElementAdapter adapter;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		adapter = new BasicWorkingSetElementAdapter();
		adapter.setInitializationData(null, "class", data);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		adapter.dispose();
	}
	
	public void testBasicWorkingSetElementAdapter_Direct() {
		IAdaptable [] result = adapter.adaptElements(null, new IAdaptable[] {new ObjectContributionClasses.Common()});
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.Common.class, result[0].getClass());
	}
	
	public void testBasicWorkingSetElementAdapter_Inheritance() {
		IAdaptable [] result = adapter.adaptElements(null, new IAdaptable[] {new ObjectContributionClasses.D()});
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.D.class, result[0].getClass());
	}
	
	public void testBasicWorkingSetElementAdapter_IAdaptable() {
		IAdaptable[] result = adapter.adaptElements(null,
				new IAdaptable[] { new ObjectContributionClasses.E()  });
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.F.class, result[0].getClass());
	}
	
	public void testBasicWorkingSetElementAdapter_AdapterManager() {
		IAdaptable[] result = adapter.adaptElements(null,
				new IAdaptable[] { new ObjectContributionClasses.E1()  });
		assertEquals(1, result.length);
		assertEquals(ObjectContributionClasses.Common.class, result[0].getClass());
	}
}
