/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;


/**
 * @since 3.0
 */
public class FastPartitionerTest {

	protected static final String COMMENT= "comment";
	protected static final String DEFAULT= IDocument.DEFAULT_CONTENT_TYPE;

	private IDocument fDoc;
	private IDocumentPartitioner fPartitioner;

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
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);
	}

	protected IDocumentPartitioner createPartitioner(IPartitionTokenScanner scanner) {
		return new FastPartitioner(scanner, new String[] { DEFAULT, COMMENT });
	}

	@Test
	public void testGetPartition() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetPartitionEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetPartition_InterleavingPartitions(offsets);
	}
	
	@Test
	public void testGetPartitionEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetPartitionEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetContentType() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetContentTypeEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetContentTypeEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testGetContentTypeEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioning() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioningEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioningEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioningEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testComputePartitioningSubrangeBeforeBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26 };
		assertComputePartitioning_InterleavingPartitions(12, 38, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeOnBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertComputePartitioning_InterleavingPartitions(13, 39, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeOnBoundaries2() {
		fDoc.set("/* comment *//* comment *//* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertComputePartitioning_InterleavingPartitions(13, 26, offsets, DEFAULT);
	}

	@Test
	public void testComputePartitioningSubrangeAfterBoundaries() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 26, 26, 39 };
		assertComputePartitioning_InterleavingPartitions(14, 40, offsets, COMMENT);
	}

	@Test
	public void testComputePartitioningSubrangeInBoundaries1() {
		fDoc.set("/* comment */");

		int[] offsets= new int[] { };
		assertComputePartitioning_InterleavingPartitions(1, 12, offsets, COMMENT);
	}

	@Test
	public void testComputePartitioningSubrangeInBoundaries2() {
		fDoc.set("docu     ment");

		int[] offsets= new int[] { };
		assertComputePartitioning_InterleavingPartitions(1, 12, offsets, DEFAULT);
	}

	@Test
	public void testPR101014() throws BadLocationException {
		fDoc.set(
				"package pr101014;\n" +
				"\n" +
				"class X {\n" +
				"String s= \n" +
				"	/*foo*/;\n" +
				"}\n");

		int[] offsets= {41, 48};
		assertComputePartitioning_InterleavingPartitions(offsets);

		fDoc.replace(40, 8, "	/*foo*/");
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	@Test
	public void testPR130900() throws Exception {
		fPartitioner.disconnect();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new SingleLineRule("#", null, comment, (char) 0, true, false) };
				setPredicateRules(rules);
			}
		};
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);

		fDoc.set("#");
		int[] offsets= new int[] { 0, 1 };
		assertComputePartitioning_InterleavingPartitions(offsets);

    }

	@Test
	public void testBug368219_1() throws Exception {
		fPartitioner.disconnect();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("/*", "*/", comment) };
				setPredicateRules(rules);
			}
		};
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);

		fDoc.set("/**");
		assertEqualPartition(0, 3, DEFAULT);

	}

	@Test
	public void testBug368219_2() throws Exception {
		fPartitioner.disconnect();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("/*", "*/", comment, (char)0, true) };
				setPredicateRules(rules);
			}
		};
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);

		fDoc.set("/**");
		assertEqualPartition(0, 3, COMMENT);

	}

	@Test
	public void testBug409538_1() throws Exception {
		fPartitioner.disconnect();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("<!--", "-->", comment, (char)0, true) };
				setPredicateRules(rules);
			}
		};
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);

		fDoc.set("<");
		assertEqualPartition(0, 1, DEFAULT);

	}

	@Test
	public void testBug409538_2() throws Exception {
		fPartitioner.disconnect();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("<!--", "-->", comment, (char)0, true) };
				setPredicateRules(rules);
			}
		};
		fPartitioner= createPartitioner(scanner);
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);

		fDoc.set("<!-- blah");
		assertEqualPartition(0, 9, COMMENT);

	}

	private void assertComputePartitioning_InterleavingPartitions(int[] offsets) {
		assertComputePartitioning_InterleavingPartitions(0, fDoc.getLength(), offsets, DEFAULT);
	}

	private void assertComputePartitioning_InterleavingPartitions(int startOffset, int endOffset, int[] offsets, String startType) {
		ITypedRegion[] regions= fPartitioner.computePartitioning(startOffset, endOffset - startOffset);

		String type= startType;
		int previousOffset= startOffset;

		int j= 0;
		for (int i= 0; i <= offsets.length; i++) {
			int currentOffset= (i == offsets.length) ? endOffset : offsets[i];
			if (currentOffset - previousOffset != 0) { // don't do empty partitions
				ITypedRegion region= regions[j++];

				assertTypedRegion(region, previousOffset, currentOffset, type);
			}

			// advance
			if (type == DEFAULT)
				type= COMMENT;
			else
				type= DEFAULT;
			previousOffset= currentOffset;
		}
	}

	private void assertGetContentType_InterleavingPartitions(int[] offsets) {
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

	private void assertGetPartition_InterleavingPartitions(int[] offsets) {
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

	private void assertEqualPartition(int offset, int end, String type) {
		int from= offset;
		int to= end - 1;
		for (int i= from; i <= to; i++) {
			ITypedRegion region= fPartitioner.getPartition(i);
			assertTypedRegion(region, offset, end, type);
		}
	}

	private void assertTypedRegion(ITypedRegion region, int offset, int end, String type) {
		assertEquals(offset, region.getOffset());
		assertEquals(end - offset, region.getLength());
		assertEquals(type, region.getType());
	}

	private void assertEqualPartitionType(int offset, int end, String type) {
		int from= offset;
		int to= end - 1;
		for (int i= from; i <= to; i++) {
			assertEquals(type, fPartitioner.getContentType(i));
		}
	}

}
