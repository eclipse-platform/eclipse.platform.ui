/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.IMarker;

public class MarkerSetTest extends ResourceTest {

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public MarkerSetTest() {
		super();
	}

	/**
	 * Creates a new markers test.
	 */
	public MarkerSetTest(String name) {
		super(name);
	}

	public void assertEquals(String message, IMarkerSetElement[] array1, IMarkerSetElement[] array2) {
		assertNotNull(message, array1);
		assertNotNull(message, array2);
		assertEquals(message, array1.length, array2.length);
		IMarkerSetElement[] m1 = new IMarkerSetElement[array1.length];
		System.arraycopy(array1, 0, m1, 0, array1.length);
		IMarkerSetElement[] m2 = new IMarkerSetElement[array2.length];
		System.arraycopy(array2, 0, m2, 0, array2.length);
		java.util.Comparator compare = new java.util.Comparator() {
			public int compare(Object o1, Object o2) {
				long id1 = ((IMarkerSetElement) o1).getId();
				long id2 = ((IMarkerSetElement) o2).getId();
				if (id1 == id2)
					return 0;
				return id1 < id2 ? -1 : 1;
			}
		};
		Arrays.sort(m1, compare);
		Arrays.sort(m2, compare);
		for (int i = 0; i < m1.length; i++)
			assertEquals(message, m1[i].getId(), m2[i].getId());
	}

	/**
	 * Configures the markers test suite.
	 */
	public static Test suite() {
		return new TestSuite(MarkerSetTest.class);

		//TestSuite suite = new TestSuite();
		//suite.addTest(new MarkerSetTest("_testPR"));
		//return suite;
	}

	public void testAdd() {

		// create the objects to insert into the set
		MarkerSet set = new MarkerSet();
		int max = 100;
		MarkerInfo info = null;
		MarkerInfo[] infos = new MarkerInfo[max];
		for (int i = 0; i < max; i++) {
			info = new MarkerInfo();
			info.setId(i);
			info.setType(IMarker.PROBLEM);
			info.setAttribute(IMarker.MESSAGE, getRandomString());
			infos[i] = info;
		}

		// add each info to the set
		for (int i = 0; i < infos.length; i++) {
			info = infos[i];
			set.add(info);
			assertTrue("2.0." + i, set.contains(info.getId()));
			assertEquals("2.1." + i, i + 1, set.size());
		}

		// make sure they are all still there
		assertEquals("3.0", max, set.size());
		for (int i = 0; i < infos.length; i++) {
			info = infos[i];
			assertTrue("3.1." + i, set.contains(info.getId()));
			assertNotNull("3.2." + i, set.get(info.getId()));
		}
	}

	public void testElements() {

		// populate the set
		MarkerSet set = new MarkerSet();
		int max = 100;
		MarkerInfo info = null;
		MarkerInfo[] infos = new MarkerInfo[max];
		for (int i = 0; i < max; i++) {
			info = new MarkerInfo();
			info.setId(i);
			info.setType(IMarker.PROBLEM);
			info.setAttribute(IMarker.MESSAGE, getRandomString());
			infos[i] = info;
		}
		set.addAll(infos);
		assertEquals("1.0", max, set.size());

		// remove each element
		assertEquals("2.0", set.elements(), infos);
	}

	public void testRemove() {

		// populate the set
		MarkerSet set = new MarkerSet();
		int max = 100;
		MarkerInfo info = null;
		MarkerInfo[] infos = new MarkerInfo[max];
		for (int i = 0; i < max; i++) {
			info = new MarkerInfo();
			info.setId(i);
			info.setType(IMarker.PROBLEM);
			info.setAttribute(IMarker.MESSAGE, getRandomString());
			infos[i] = info;
		}
		set.addAll(infos);
		assertEquals("1.0", max, set.size());

		// remove each element
		for (int i = max - 1; i >= 0; i--) {
			info = infos[i];
			set.remove(info);
			assertTrue("2.0." + i, !set.contains(info.getId()));
			assertEquals("2.1," + i, i, set.size());
			// check that the others still exist
			for (int j = 0; j < i; j++)
				assertTrue("2.2." + j, set.contains(infos[j].getId()));
		}

		// all gone?
		assertEquals("3.0", 0, set.size());
	}
}
