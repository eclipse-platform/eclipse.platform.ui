/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.rules;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;


/**
 * Scanner that exclusively uses predicate rules.
 * @since 2.0
 */
public class RuleBasedPartitionScanner extends BufferedRuleBasedScanner implements IPartitionTokenScanner {
	
	/** The content type of the partion in which to resume scanning. */
	protected String fContentType;
	/** The offset of the partition inside which to resume. */
	protected int fPartitionOffset;
	
	
	/*
	 * @see RuleBasedScanner#setRules(IRule[])
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
	
	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		fContentType= contentType;
		fPartitionOffset= partitionOffset;
		super.setRange(document, offset, length);
	}
	
	/*
	 * @see ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		
		if (fContentType == null || fRules == null)
			return super.nextToken();
		
		fTokenOffset= fOffset;
		fColumn= UNDEFINED;
		boolean resume= (fPartitionOffset < fOffset);
				
		IPredicateRule rule;
		IToken token;
		
		for (int i= 0; i < fRules.length; i++) {
			rule= (IPredicateRule) fRules[i];
			token= rule.getSuccessToken();
			if (fContentType.equals(token.getData())) {
				if (resume)
					fTokenOffset= fPartitionOffset;
				token= rule.evaluate(this, resume);
				if (!token.isUndefined()) {
					fContentType= null;
					return token;
				}
			}
		}
		
		fContentType= null;
		return super.nextToken();
	}
}
