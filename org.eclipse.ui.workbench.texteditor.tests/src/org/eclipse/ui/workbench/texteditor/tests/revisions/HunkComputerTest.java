/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.jface.internal.text.revisions.Hunk;
import org.eclipse.jface.internal.text.revisions.HunkComputer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

/**
 * Tests {@link HunkComputer}.
 *
 * @since 3.3
 */
public class HunkComputerTest {

	private static final int A= ILineDiffInfo.ADDED;
	private static final int C= ILineDiffInfo.CHANGED;
	private static final int U= ILineDiffInfo.UNCHANGED;

	private int[] fDiffInformation;
	private ILineDiffer fDiffer= new ILineDiffer() {

		@Override
		public ILineDiffInfo getLineInfo(final int line) {
			return new ILineDiffInfo() {

				@Override
				public int getChangeType() {
					return fDiffInformation[line * 2];
				}

				@Override
				public String[] getOriginalText() {
					throw new UnsupportedOperationException();
				}

				@Override
				public int getRemovedLinesAbove() {
					return fDiffInformation[line * 2 + 1];
				}

				@Override
				public int getRemovedLinesBelow() {
					if (fRemovedBelow == null)
						return 0;
					return fRemovedBelow[line];
				}

				@Override
				public boolean hasChanges() {
					throw new UnsupportedOperationException();
				}

			};
		}
		@Override
		public int restoreAfterLine(int line) throws BadLocationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void revertBlock(int line) throws BadLocationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void revertLine(int line) throws BadLocationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void revertSelection(int line, int nLines) throws BadLocationException {
			throw new UnsupportedOperationException();
		}
	};
	private int[] fRemovedBelow;


	@Test
	public void testNoDiff() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testShiftOne() throws Exception {
		int[] diffInfo= new int[] {C, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {0, 0, 1};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveFirstLine() throws Exception {
		int[] diffInfo= new int[] {U, 1, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {0, -1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveSecondLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 1, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {1, -1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testAddFirstLine() throws Exception {
		int[] diffInfo= new int[] {A, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {0, 1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testAddSecondLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, A, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {1, 1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testAddThirdLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, A, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {2, 1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveFirstRegion() throws Exception {
		int[] diffInfo= new int[] {U, 2, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {0, -2, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testReplaceFirstRegion() throws Exception {
		int[] diffInfo= new int[] {C, 0, C, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {0, 0, 2};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveOverlappingRegion() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 2, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {1, -2, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testReplaceOverlappingRegion() throws Exception {
		int[] diffInfo= new int[] {U, 0, C, 0, C, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, };
		int[] expected= {1, 0, 2};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveInnerLines() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 2, };
		int[] expected= {8, -2, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testReplaceInnerLines() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, C, 0, C, 0, U, 0, };
		int[] expected= {8, 0, 2};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testAddInnerLines() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, A, 0, A, 0, U, 0, U, 0, U, 0, };
		int[] expected= {8, +2, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testRemoveLastLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0};
		fRemovedBelow= new int[10];
		fRemovedBelow[9]= 1;
		int[] expected= {10, -1, 0};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testReplaceLastLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, C, 0, };
		int[] expected= {10, 0, 1};

		assertHunks(diffInfo, expected);
	}

	@Test
	public void testAddLastLine() throws Exception {
		int[] diffInfo= new int[] {U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, U, 0, A, 0, };
		int[] expected= {12, 1, 0};

		assertHunks(diffInfo, expected);
	}

	private void assertHunks(int[] diffInfo, int[] expected) {
		fDiffInformation= diffInfo;
		assertEquals(0, diffInfo.length % 2);
		Hunk[] hunks= HunkComputer.computeHunks(fDiffer, fDiffInformation.length / 2);
		assertEquals(0, expected.length % 3);
		int n= expected.length / 3;
		assertEquals(n, hunks.length);
		for (int i= 0; i < n; i++) {
			Hunk h= hunks[i];
			assertEquals(new Hunk(expected[3*i], expected[3*i+1], expected[3*i+2]), h);
		}
	}


}
