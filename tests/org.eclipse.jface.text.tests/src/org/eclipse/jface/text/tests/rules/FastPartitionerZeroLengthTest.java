/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

package org.eclipse.jface.text.tests.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


/**
 * @since 3.0
 */
public class FastPartitionerZeroLengthTest {

	private static final String COMMENT= "comment";
	private static final String DEFAULT= IDocument.DEFAULT_CONTENT_TYPE;

	private IDocument fDoc;
	private FastPartitioner fPartitioner;

	@Before
	public void setUp() {
		fDoc= new Document();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("/*", "*/", comment) };
				setPredicateRules(rules);

			}
		};
		fPartitioner= new FastPartitioner(scanner, new String[] { DEFAULT, COMMENT });
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);
	}

	@Test
	public void testGetZeroLengthPartition() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetZeroLengthPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthPartitionEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetZeroLengthPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthPartitionEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetZeroLengthPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthPartitionEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetZeroLengthPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthContentType() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetZeroLengthContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthContentTypeEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetZeroLengthContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthContentTypeEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetZeroLengthContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetZeroLengthContentTypeEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetZeroLengthContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputeZeroLengthPartitioning() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputeZeroLengthPartitioningEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputeZeroLengthPartitioningEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputeZeroLengthPartitioningEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioningSubrangeBeforeBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(12, 38, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeOnBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(13, 39, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeOnBoundaries2() {
		fDoc.set("/* comment *//* comment *//* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(13, 26, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeAfterBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 26, 26, 39 };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(14, 40, offsets, COMMENT);
	}

	@Test
	public void testComputePartitioningSubrangeInBoundaries1() {
		fDoc.set("/* comment */");

		int[] offsets= new int[] { };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(1, 12, offsets, COMMENT);
	}

	@Test
	public void testComputePartitioningSubrangeInBoundaries2() {
		fDoc.set("docu     ment");

		int[] offsets= new int[] { };
		assertComputeZeroLengthPartitioning_InterleavingPartitions(1, 12, offsets, DEFAULT);
	}

	private void assertComputeZeroLengthPartitioning_InterleavingPartitions(int[] offsets) {
		assertComputeZeroLengthPartitioning_InterleavingPartitions(0, fDoc.getLength(), offsets, DEFAULT);
	}

	private void assertComputeZeroLengthPartitioning_InterleavingPartitions(int startOffset, int endOffset, int[] offsets, String startType) {
		ITypedRegion[] regions= fPartitioner.computePartitioning(startOffset, endOffset - startOffset, true);

		String type= startType;
		int previousOffset= startOffset;

		assertEquals(offsets.length + 1, regions.length);
		for (int i= 0; i <= offsets.length; i++) {
			int currentOffset= (i == offsets.length) ? endOffset : offsets[i];
			ITypedRegion region= regions[i];

			assertTypedRegion(region, previousOffset, currentOffset, type);

			// advance
			if (type == DEFAULT)
				type= COMMENT;
			else
				type= DEFAULT;
			previousOffset= currentOffset;
		}
	}

	private void assertGetZeroLengthContentType_InterleavingPartitions(int[] offsets) {
		String type= DEFAULT;
		int previousOffset= 0;
		for (int i= 0; i <= offsets.length; i++) {
			int offset= (i == offsets.length) ? fDoc.getLength() : offsets[i];
			assertEqualPartitionType(previousOffset, offset, type);

			// advance
			if (type == DEFAULT)
				type= COMMENT;
			else
				type= DEFAULT;
			previousOffset= offset;
		}
	}

	private void assertGetZeroLengthPartition_InterleavingPartitions(int[] offsets) {
		String type= DEFAULT;
		int previousOffset= 0;
		for (int i= 0; i <= offsets.length; i++) {
			int offset= (i == offsets.length) ? fDoc.getLength() : offsets[i];
			assertEqualPartition(previousOffset, offset, type);

			// advance
			if (type == DEFAULT)
				type= COMMENT;
			else
				type= DEFAULT;
			previousOffset= offset;
		}
	}

	private boolean isOpenType(String type) {
		return type.equals(DEFAULT);
	}

	private void assertEqualPartition(int offset, int inclusiveEnd, String type) {
		int from= isOpenType(type) ? offset : offset + 1;
		int to= isOpenType(type) ? inclusiveEnd : inclusiveEnd - 1;
		for (int i= from; i <= to; i++) {
			ITypedRegion region= fPartitioner.getPartition(i, true);
			assertTypedRegion(region, offset, inclusiveEnd, type);
		}
	}

	private void assertTypedRegion(ITypedRegion region, int offset, int inclusiveEnd, String type) {
		assertEquals(offset, region.getOffset());
		assertEquals(inclusiveEnd - offset, region.getLength());
		assertEquals(type, region.getType());
	}

	private void assertEqualPartitionType(int offset, int inclusiveEnd, String type) {
		int from= isOpenType(type) ? offset : offset + 1;
		int to= isOpenType(type) ? inclusiveEnd : inclusiveEnd - 1;
		for (int i= from; i <= to; i++) {
			assertEquals(type, fPartitioner.getContentType(i, true));
		}
	}

}
