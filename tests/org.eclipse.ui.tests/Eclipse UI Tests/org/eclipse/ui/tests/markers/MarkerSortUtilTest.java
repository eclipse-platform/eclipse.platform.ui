/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.markers;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ui.internal.views.markers.MarkerSortUtil;
import org.eclipse.ui.internal.views.markers.MockMarkerEntry;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.markers.MarkerItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.5
 */
@RunWith(JUnit4.class)
public class MarkerSortUtilTest extends UITestCase {

	private static final int ARRAYSIZE = 100000;

	public MarkerSortUtilTest() {
		super("MarkerSortUtilTest");
	}

	@Override
	protected void doSetUp() throws Exception {
		// TODO Auto-generated method stub
		super.doSetUp();

	}

	@Test
	public void testPartialSort() {
		sortToLimit(ARRAYSIZE,ARRAYSIZE/2);
	}

	@Test
	public void testCompleteSort() {
		sortToLimit(ARRAYSIZE,ARRAYSIZE);
	}
	private void sortToLimit(int arraySize,int limit) {
		MockMarkerEntry[] fArray1=generateArray(arraySize);
		MockMarkerEntry[] fArray2=fArray1.clone();
		Comparator<MarkerItem> comparator = (o1, o2) -> ((MockMarkerEntry) o1).name
				.compareTo(((MockMarkerEntry) o2).name);
		MarkerSortUtil.sortStartingKElement(fArray1, comparator, 0,fArray1.length-1, limit);
		Arrays.sort(fArray2,comparator);

		for (int i = 0; i < limit; i++) {
			if(!fArray1[i].equals(fArray2[i])){
				fail("Incorrect sorting by MarkerSortUtil.sortStartingKElement(...)");
			}
		}
	}


	/**
	 * Generate a large sized array for sorting
	 */
	static MockMarkerEntry[] generateArray(int arraySize) {
		MockMarkerEntry[] fArray = new MockMarkerEntry[arraySize];
		int count = arraySize - 1;
		outer: while (count >= 0) {
			for (int i = 0; i < 26; i++) {
				for (int j = 0; j < 26; j++) {
					for (int k = 0; k < 26; k++) {
						if (count < 0) {
							break outer;
						}
						if (k % 2 == 0) {
							fArray[count] = new MockMarkerEntry(
									new String(
											new char[] { (char) (i + 'a'),
													(char) (j + 'A'),
													(char) (k + 'a') }));
						} else if (k % 3 == 0) {
							fArray[count] = new MockMarkerEntry(
									new String(
											new char[] { (char) (i + 'a'),
													(char) (j + 'a'),
													(char) (k + 'A') }));
						} else {
							fArray[count] = new MockMarkerEntry(
									new String(
											new char[] { (char) (i + 'A'),
													(char) (j + 'a'),
													(char) (k + 'a') }));
						}
						--count;
					}

				}
			}

		}
		return fArray;
	}
}
