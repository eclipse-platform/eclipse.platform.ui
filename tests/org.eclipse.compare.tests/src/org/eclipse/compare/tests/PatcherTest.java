package org.eclipse.compare.tests;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.compare.internal.patch.FileDiff;
import org.eclipse.compare.internal.patch.Hunk;
import org.eclipse.compare.internal.patch.Patcher;

public class PatcherTest extends TestCase {

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=187365
	public void testExcludePartOfNonWorkspacePatch() {
		Patcher patcher = new Patcher();
		MyFileDiff myFileDiff = new MyFileDiff();
		try {
			patcher.setEnabled(myFileDiff, false);
		} catch (NullPointerException e) {
			fail();
		}
	}

	/**
	 * A mock FileDiff class. It has set no paths nor project. It's perfect for
	 * our tests :)
	 */
	private class MyFileDiff extends FileDiff {
		protected MyFileDiff() {
			super(null, 0, null, 0);
			add(new Hunk(this, new int[] { 0, 0 }, new int[] { 0, 0 },
					new ArrayList(), false, false, false));
		}
	}
}
