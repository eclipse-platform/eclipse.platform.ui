/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.jface.text.source.ILineRange;

import org.eclipse.jface.internal.text.revisions.Range;

class RangeUtil extends Assert {
	private RangeUtil() {}

	static void assertEqualRange(ILineRange expected, ILineRange actual) {
		assertEquals(expected.getStartLine(), actual.getStartLine());
		assertEquals(expected.getNumberOfLines(), actual.getNumberOfLines());
	}

	static List deepClone(List ranges) {
		List list= new ArrayList(ranges.size());
		for (Iterator it= ranges.iterator(); it.hasNext();) {
			ILineRange range= (ILineRange) it.next();
			list.add(Range.copy(range));
		}
		return list;
	}

	static void assertEqualRanges(ILineRange expected1, ILineRange expected2, List actual) {
		assertEquals(2, actual.size());
		RangeUtil.assertEqualRange(expected1, (ILineRange) actual.get(0));
		RangeUtil.assertEqualRange(expected2, (ILineRange) actual.get(1));
	}

	static void assertEqualSingleRange(ILineRange expected, List actual) {
		assertEquals(1, actual.size());
		RangeUtil.assertEqualRange(expected, (ILineRange) actual.get(0));
	}

	static void assertEqualRanges(List expected, List actual) {
		assertEquals(expected.size(), actual.size());
		Iterator it1= expected.iterator();
		Iterator it2= actual.iterator();
		while (it1.hasNext()) {
			ILineRange r1= (ILineRange) it1.next();
			ILineRange r2= (ILineRange) it2.next();
			assertEqualRange(r1, r2);
		}
	}
}
