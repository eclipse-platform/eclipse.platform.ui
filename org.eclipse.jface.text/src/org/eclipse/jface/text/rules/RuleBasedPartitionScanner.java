/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.rules;


import org.eclipse.jface.text.IDocument;


/**
 * Scanner that exclusively uses predicate rules.
 * <p>
 * If a partial range is set (see {@link #setPartialRange(IDocument, int, int, String, int)} with
 * content type that is not <code>null</code> then this scanner will first try the rules that match
 * the given content type.
 * </p>
 * 
 * @since 2.0
 */
public class RuleBasedPartitionScanner extends BufferedRuleBasedScanner implements IPartitionTokenScanner {

	/** The content type of the partition in which to resume scanning. */
	protected String fContentType;
	/** The offset of the partition inside which to resume. */
	protected int fPartitionOffset;


	/**
	 * Disallow setting the rules since this scanner
	 * exclusively uses predicate rules.
	 *
	 * @param rules the sequence of rules controlling this scanner
	 */
	public void setRules(IRule[] rules) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	public void setPredicateRules(IPredicateRule[] rules) {
		super.setRules(rules);
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		setPartialRange(document, offset, length, null, -1);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the given content type is not <code>null</code> then this scanner will first try the rules
	 * that match the given content type.
	 * </p>
	 */
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		fContentType= contentType;
		fPartitionOffset= partitionOffset;
		if (partitionOffset > -1) {
			int delta= offset - partitionOffset;
			if (delta > 0) {
				super.setRange(document, partitionOffset, length + delta);
				fOffset= offset;
				return;
			}
		}
		super.setRange(document, offset, length);
	}

	/*
	 * @see ITokenScanner#nextToken()
	 */
	public IToken nextToken() {


		if (fContentType == null || fRules == null) {
			//don't try to resume
			return super.nextToken();
		}

		// inside a partition

		fColumn= UNDEFINED;
		boolean resume= (fPartitionOffset > -1 && fPartitionOffset < fOffset);
		fTokenOffset= resume ? fPartitionOffset : fOffset;

		IPredicateRule rule;
		IToken token;

		for (int i= 0; i < fRules.length; i++) {
			rule= (IPredicateRule) fRules[i];
			token= rule.getSuccessToken();
			if (fContentType.equals(token.getData())) {
				token= rule.evaluate(this, resume);
				if (!token.isUndefined()) {
					fContentType= null;
					return token;
				}
			}
		}

		// haven't found any rule for this type of partition
		fContentType= null;
		if (resume)
			fOffset= fPartitionOffset;
		return super.nextToken();
	}
}
