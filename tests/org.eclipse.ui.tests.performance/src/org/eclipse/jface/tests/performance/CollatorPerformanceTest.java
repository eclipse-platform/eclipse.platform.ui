/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 ******************************************************************************/

package org.eclipse.jface.tests.performance;

import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.util.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @since 3.5
 */
public class CollatorPerformanceTest {

	private static final int ARRAYSIZE=100000;
	private static String[] fArray;

	public CollatorPerformanceTest() {
		generateArray();
	}

	/**
	 *  test Collator by sorting the array
	 */
	@Test
	public void testCollator(TestInfo testInfo) {
		Comparator<Object> comparator = Policy.getComparator();
		List<Long> timings = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			String[] array = fArray.clone();
			long before = System.nanoTime();
			Arrays.sort(array, comparator);
			timings.add(System.nanoTime() - before);
			assertEquals(ARRAYSIZE, array.length);
		}

		reportTimings(getClass().getSimpleName() + "." + testInfo.getDisplayName(), timings);
	}

	/**
	 * Generate a large sized array for sorting
	 */
	private void generateArray() {
		if(fArray==null){
			fArray=new String[ARRAYSIZE];
			int count=ARRAYSIZE-1;
			while(count>=0){
				for (int i = 0; i < 26; i++) {
					for (int j = 0; j < 26; j++) {
						for (int k = 0; k < 26; k++) {
							if(count<0)return;
							if(k%2==0)
							fArray[count]=new String(new char[]{
									(char) (i+'a'),(char) (j+'A'),(char) (k+'a')
							});
							else if(k%3==0)
								fArray[count]=new String(new char[]{
										(char) (i+'a'),(char) (j+'a'),(char) (k+'A')
							});
							else
								fArray[count]=new String(new char[]{
										(char) (i+'A'),(char) (j+'a'),(char) (k+'a')
								});
							--count;
						}

					}
				}

			}
		}
	}
}