package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * @see org.eclipse.jface.text.rules.DefaultPartitioner
 */
public class RuleBasedPartitioner extends DefaultPartitioner {
	
	/**
	 * Creates a new partitioner that uses the given scanner and may return 
	 * partitions of the given legal content types.
	 *
	 * @param scanner the scanner this partitioner is supposed to use
	 * @param legalContentTypes the legal content types of this partitioner
	 */
	public RuleBasedPartitioner(RuleBasedPartitionScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}
}