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

package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Utilities for testing ICheckStateProviders.
 * @since 3.5
 */
public class CheckStateProviderTestsUtil {
	private static final int NUMBER_OF_STATES = 4;

	/**
	 * An ICheckStateProvider which records whether
	 * its isChecked and isGrayed methods are invoked.
	 * @since 3.5
	 */
	public static class TestMethodsInvokedCheckStateProvider implements ICheckStateProvider {
		public List isCheckedInvokedOn = new ArrayList();
		public List isGrayedInvokedOn = new ArrayList();
		
		public boolean isChecked(Object element) {
			isCheckedInvokedOn.add(element);
			return true;
		}

		public boolean isGrayed(Object element) {
			isGrayedInvokedOn.add(element);
			return true;
		}
		
		public void reset() {
			isCheckedInvokedOn = new ArrayList();
			isGrayedInvokedOn = new ArrayList();
		}
	}
	
	/**
	 * An ICheckStateProvider which provides a consistent
	 * variety of states for input elements based on the
	 * parameter provided in the constructor.
	 * @since 3.5
	 */
	public static final class TestCheckStateProvider extends TestMethodsInvokedCheckStateProvider {
		private int shift;
		
		/**
		 * A value from 0 to 2 which will change the 
		 * checkstate assignments.
		 * @param shift
		 */
		public TestCheckStateProvider(int shift) {
			this.shift = shift;
		}
		
		public boolean isChecked(Object element) {
			super.isChecked(element);
			return shouldBeChecked((TestElement)element, shift);
		}

		public boolean isGrayed(Object element) {
			super.isGrayed(element);
			return shouldBeGrayed((TestElement)element, shift);
		}
	}
	
	/**
	 * A sorter for TestElements.
	 * @since 3.5
	 */
	public static final class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return constructNumber((TestElement)e1) - constructNumber((TestElement)e2);
		}
	}
	
	/**
	 * A filter for TestElements.
	 * @since 3.5
	 */
	public static final class Filter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return (constructNumber((TestElement)element) % (NUMBER_OF_STATES * 2 - 1)) == (NUMBER_OF_STATES - 1);
		}
	}
    /**
     * @param te
     * @return	a number between 0 and 3 based on <code>te</code>. 
     * 	Given the same TestElement, this function always returns the
     * 	same value.
     */
	public static int constructNumber(TestElement te) {
		String id = te.getID();
		int number = Integer.parseInt(id.substring(id.lastIndexOf('-') + 1)) + id.length();
		return number % NUMBER_OF_STATES;
	}
	
	/**
	 * @param te
	 * @param shift	a parameter to change all check states
	 * 		to be different (use to simulate different
	 * 		providers over time)
	 * @return	true iff <code>te</code> should be checked
	 */
	public static boolean shouldBeChecked(TestElement te, int shift) {
		return ((constructNumber(te) + shift) % NUMBER_OF_STATES) > 1;
	}
	
	/**
	 * @param te
	 * @param shift	a parameter to change all check states
	 * 		to be different (use to simulate different
	 * 		providers over time)
	 * @return	true iff <code>te</code> should be grayed
	 */	
	public static boolean shouldBeGrayed(TestElement te, int shift) {
		return ((constructNumber(te) + shift) % NUMBER_OF_STATES) % 2 == 1;
	}
}