/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.tests.rules;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


/**
 * @since 3.0
 */
public class DefaultPartitionerTest extends TestCase {

	private static final String COMMENT= "comment";
	private static final String DEFAULT= IDocument.DEFAULT_CONTENT_TYPE;
	
	private IDocument fDoc;
	private DefaultPartitioner fPartitioner;

	public void setUp() {
		fDoc= new Document();
		IPartitionTokenScanner scanner= new RuleBasedPartitionScanner() {
			{
				IToken comment= new Token(COMMENT);
				IPredicateRule[] rules= new IPredicateRule[] { new MultiLineRule("/*", "*/", comment) };
				setPredicateRules(rules);

			}
		};
		fPartitioner= new DefaultPartitioner(scanner, new String[] { DEFAULT, COMMENT });
		fDoc.setDocumentPartitioner(fPartitioner);
		fPartitioner.connect(fDoc);
	}

	public void testGetPartition() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	public void testGetPartitionEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	public void testGetPartitionEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	public void testGetPartitionEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetPartition_InterleavingPartitions(offsets);
	}

	public void testGetContentType() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	public void testGetContentTypeEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	public void testGetContentTypeEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertGetContentType_InterleavingPartitions(offsets);
	}

	public void testGetContentTypeEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertGetContentType_InterleavingPartitions(offsets);
	}
	
	public void testComputePartitioning() {
		fDoc.set("docu     ment/* comment */docu     ment");

		int[] offsets= new int[] { 13, 26 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	public void testComputePartitioningEmptyMiddle() {
		fDoc.set("docu     ment/* comment *//* comment */docu     ment");

		int[] offsets= new int[] { 13, 26, 26, 39 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	public void testComputePartitioningEmptyStart() {
		fDoc.set("/* comment */docu     ment");

		int[] offsets= new int[] { 0, 13 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	public void testComputePartitioningEmptyEnd() {
		fDoc.set("docu     ment/* comment */");

		int[] offsets= new int[] { 13, 26 };
		assertComputePartitioning_InterleavingPartitions(offsets);
	}

	private void assertComputePartitioning_InterleavingPartitions(int[] offsets) {
		ITypedRegion[] regions= fPartitioner.computePartitioning(0, fDoc.getLength());
		
		String type= DEFAULT;
		int previousOffset= 0;
		
		int j= 0;
		for (int i= 0; i <= offsets.length; i++) {
			int offset= (i == offsets.length) ? fDoc.getLength() : offsets[i];
			if (offset - previousOffset != 0) { // don't do empty partitions
				ITypedRegion region= regions[j++];
				
				assertTypedRegion(region, previousOffset, offset, type);
			}
			
			// advance
			if (type == DEFAULT)
				type= COMMENT;
			else
				type= DEFAULT;
			previousOffset= offset;
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
