package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.IDocument;


/**
 * A token scanner scans a range of a document and reports about the token it finds.
 * A scanner has state. When asked the scanner returns the offset and the length of the
 * last found token.
 * 
 * @see org.eclipse.jface.text.rules.IToken
 */
public interface IPartitionTokenScanner  extends ITokenScanner {
	
	/**
	 * Configures the scanner by providing access to the document range over which to scan.
	 * The range is not a full range but starts at the beginning of a line in the middle of a partition
	 * of the given content type. Therefore, it is assumed that a partition delimiter can not contain
	 * a line delimiter.
	 *
	 * @param document the document to scan
	 * @param offset the offset of the document range to scan
	 * @param length the length of the document range to scan
	 * @param contentType the content type at the given offset
	 * @param partitionOffset the offset at which the partition of the given offset starts
	 */
	void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset);
}
