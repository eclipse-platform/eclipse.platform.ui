/*******************************************************************************
 * Copyright (c) 2016, 2023 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Peter Palaga (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.navigator.resources.nested.PathComparator;
import org.junit.Test;

public class PathComparatorTest {

	private static final Comparator<IPath> COMPARATOR = new PathComparator();

	private static void assertConsistentWithEquals(IPath p1, IPath p2) {

		boolean equals = p1.equals(p2);
		int compare = COMPARATOR.compare(p1, p2);
		if (equals != (compare == 0)) {
			fail("Path.equals() == " + equals + " inconsistent with " + PathComparator.class.getName()
					+ ".compare() == " + compare + " for paths '" + p1 + "' and '" + p2 + "'");
		}
	}

	private static void assertLessThan(IPath p1, IPath p2) {
		int compare = COMPARATOR.compare(p1, p2);
		assertTrue(PathComparator.class.getName() + ".compare() returned " + compare
				+ " expected less than zero for paths '" + p1 + "' and '"
				+ p2 + "'", compare < 0);
	}

	@Test
	public void checkInvariant() {
		IPath ab = IPath.fromOSString("a/b");
		IPath abc = IPath.fromOSString("a/b/c");
		IPath ac = IPath.fromOSString("a/c");
		IPath acb = IPath.fromOSString("a/c/b");
		assertTrue(COMPARATOR.compare(ab, abc) < 0);
		assertTrue(COMPARATOR.compare(abc, ac) < 0);
		assertTrue(COMPARATOR.compare(ac, acb) < 0);
	}

	@Test
	public void consistentWithEqualsDistLength() {
		assertConsistentWithEquals(IPath.forWindows("a:/f1/f2"), IPath.forWindows("a:/f1/f2/f3"));
	}

	@Test
	public void consistentWithEqualsDist() {
		assertConsistentWithEquals(IPath.forWindows("a:/f1/f2"), IPath.forWindows("a:/f1/f3"));
	}

	@Test
	public void consistentWithEqualsDistDevice() {
		assertConsistentWithEquals(IPath.forWindows("a:/f1/f2"), IPath.forWindows("b:/f1/f2"));
	}

	@Test
	public void consistentWithEqualsDistLeadingSlash() {
		assertConsistentWithEquals(IPath.forWindows("/f1/f2"), IPath.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsDistTrailingSlash() {
		assertConsistentWithEquals(IPath.forWindows("f1/f2/"), IPath.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsSame() {
		assertConsistentWithEquals(IPath.forWindows("a:/f1/f2"), IPath.forWindows("a:/f1/f2"));
	}

	@Test
	public void consistentWithEqualsUncAbsolute() {
		assertConsistentWithEquals(IPath.forWindows("//f1/f2"), IPath.forWindows("/f1/f2"));
	}

	@Test
	public void consistentWithEqualsUncRelative() {
		assertConsistentWithEquals(IPath.forWindows("//f1/f2"), IPath.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsWinPosix() {
		assertConsistentWithEquals(IPath.forWindows("f1/f2"), IPath.forPosix("f1/f2"));
	}

	@Test
	public void lessThanRelativeDashSlash() {
		assertLessThan(IPath.forWindows("f1/f1"), IPath.forWindows("f1-f2"));
	}

	@Test
	public void lessThanRelativeDepth1() {
		assertLessThan(IPath.forWindows("f1"), IPath.forWindows("f2"));
	}

	@Test
	public void lessThanRelativeDepth2() {
		assertLessThan(IPath.forWindows("f1/f1"), IPath.forWindows("f1/f2"));
	}

	@Test
	public void deviceALessThanDeviceB() {
		assertLessThan(IPath.forWindows("a:/f1/f2"), IPath.forWindows("b:/f1/f2"));
	}

	@Test
	public void relativeLessThanAbsolute() {
		assertLessThan(IPath.forWindows("f1/f2"), IPath.forWindows("/f1/f2"));
	}

	@Test
	public void absoluteLessThanUnc() {
		assertLessThan(IPath.forWindows("/f1/f2"), IPath.forWindows("//f1/f2"));
	}

	@Test
	public void uncLessThanDevice() {
		assertLessThan(IPath.forWindows("//f1/f2"), IPath.forWindows("a:/f1/f2"));
	}

}
