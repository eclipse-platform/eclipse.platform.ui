/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 ******************************************************************************/

package org.eclipse.jface.tests.performance;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.util.Policy;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * @since 3.5
 *
 */
public class CollatorPerformanceTest extends BasicPerformanceTest {

	private static final int ARRAYSIZE=100000;
	private static String[] fArray;

	/**
	 * @param testName
	 */
	public CollatorPerformanceTest (String testName) {
		super(testName);
		generateArray();
	}

	/**
	 *  test Collator by sorting the array
	 */
	public void testCollator(){
		Comparator<Object> comparator=Policy.getComparator();
		for (int i = 0; i < 15; i++) {
			String[] array=fArray.clone();
			startMeasuring();
			Arrays.sort(array, comparator);
			stopMeasuring();
		}
        commitMeasurements();
        assertPerformance();
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