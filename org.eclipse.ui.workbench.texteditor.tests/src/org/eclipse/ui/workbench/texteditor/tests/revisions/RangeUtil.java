/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

import org.eclipse.jface.internal.text.revisions.Range;

import org.eclipse.jface.text.source.ILineRange;


class RangeUtil extends Assert {
	private RangeUtil() {}

	static void assertEqualRange(ILineRange expected, ILineRange actual) {
		assertEquals(expected.getStartLine(), actual.getStartLine());
		assertEquals(expected.getNumberOfLines(), actual.getNumberOfLines());
	}

	static List<Range> deepClone(List<Range> ranges) {
		List<Range> list= new ArrayList<>(ranges.size());
		for (Iterator<Range> it= ranges.iterator(); it.hasNext();) {
			ILineRange range= it.next();
			list.add(Range.copy(range));
		}
		return list;
	}

	static void assertEqualRanges(ILineRange expected1, ILineRange expected2, List<Range> actual) {
		assertEquals(2, actual.size());
		RangeUtil.assertEqualRange(expected1, actual.get(0));
		RangeUtil.assertEqualRange(expected2, actual.get(1));
	}

	static void assertEqualSingleRange(ILineRange expected, List<Range> actual) {
		assertEquals(1, actual.size());
		RangeUtil.assertEqualRange(expected, actual.get(0));
	}

	static void assertEqualRanges(List<Range> expected, List<Range> actual) {
		assertEquals(expected.size(), actual.size());
		Iterator<Range> it1= expected.iterator();
		Iterator<Range> it2= actual.iterator();
		while (it1.hasNext()) {
			ILineRange r1= it1.next();
			ILineRange r2= it2.next();
			assertEqualRange(r1, r2);
		}
	}
}
