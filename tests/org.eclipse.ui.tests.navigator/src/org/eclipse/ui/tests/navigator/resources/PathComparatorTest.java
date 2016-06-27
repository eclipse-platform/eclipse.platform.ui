/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Peter Palaga (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.navigator.resources.nested.PathComparator;
import org.junit.Assert;
import org.junit.Test;

public class PathComparatorTest {

	private static final Comparator<IPath> COMPARATOR = new PathComparator();

	private static void assertConsistentWithEquals(IPath p1, IPath p2) {

		boolean equals = p1.equals(p2);
		int compare = COMPARATOR.compare(p1, p2);

		if (equals != (compare == 0)) {
			Assert.fail("Path.equals() == " + equals + " inconsistent with " + PathComparator.class.getName()
					+ ".compare() == " + compare + " for paths '" + p1 + "' and '" + p2 + "'");
		}
	}

	private static void assertLessThan(IPath p1, IPath p2) {
		int compare = COMPARATOR.compare(p1, p2);
		Assert.assertTrue(PathComparator.class.getName() + ".compare() returned " + compare
				+ " expected less than zero for paths '" + p1 + "' and '"
				+ p2 + "'", compare < 0);
	}

	@Test
	public void consistentWithEqualsDistLength() {
		assertConsistentWithEquals(Path.forWindows("a:/f1/f2"), Path.forWindows("a:/f1/f2/f3"));
	}

	@Test
	public void consistentWithEqualsDist() {
		assertConsistentWithEquals(Path.forWindows("a:/f1/f2"), Path.forWindows("a:/f1/f3"));
	}

	@Test
	public void consistentWithEqualsDistDevice() {
		assertConsistentWithEquals(Path.forWindows("a:/f1/f2"), Path.forWindows("b:/f1/f2"));
	}

	@Test
	public void consistentWithEqualsDistLeadingSlash() {
		assertConsistentWithEquals(Path.forWindows("/f1/f2"), Path.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsDistTrailingSlash() {
		assertConsistentWithEquals(Path.forWindows("f1/f2/"), Path.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsSame() {
		assertConsistentWithEquals(Path.forWindows("a:/f1/f2"), Path.forWindows("a:/f1/f2"));
	}

	@Test
	public void consistentWithEqualsUncAbsolute() {
		assertConsistentWithEquals(Path.forWindows("//f1/f2"), Path.forWindows("/f1/f2"));
	}

	@Test
	public void consistentWithEqualsUncRelative() {
		assertConsistentWithEquals(Path.forWindows("//f1/f2"), Path.forWindows("f1/f2"));
	}

	@Test
	public void consistentWithEqualsWinPosix() {
		assertConsistentWithEquals(Path.forWindows("f1/f2"), Path.forPosix("f1/f2"));
	}

	@Test
	public void lessThanRelativeDashSlash() {
		assertLessThan(Path.forWindows("f1/f1"), Path.forWindows("f1-f2"));
	}

	@Test
	public void lessThanRelativeDepth1() {
		assertLessThan(Path.forWindows("f1"), Path.forWindows("f2"));
	}

	@Test
	public void lessThanRelativeDepth2() {
		assertLessThan(Path.forWindows("f1/f1"), Path.forWindows("f1/f2"));
	}

	@Test
	public void deviceALessThanDeviceB() {
		assertLessThan(Path.forWindows("a:/f1/f2"), Path.forWindows("b:/f1/f2"));
	}

	@Test
	public void relativeLessThanAbsolute() {
		assertLessThan(Path.forWindows("f1/f2"), Path.forWindows("/f1/f2"));
	}

	@Test
	public void absoluteLessThanUnc() {
		assertLessThan(Path.forWindows("/f1/f2"), Path.forWindows("//f1/f2"));
	}

	@Test
	public void uncLessThanDevice() {
		assertLessThan(Path.forWindows("//f1/f2"), Path.forWindows("a:/f1/f2"));
	}

}