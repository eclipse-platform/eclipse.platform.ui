package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.IDocument;


/**
 * Scanner that exclusively uses predicate rules.
 */
public class RuleBasedPartitionScanner extends BufferedRuleBasedScanner implements IPartitionTokenScanner {
	
	protected String fContentType;
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
	 * @see ITokenScanner2#setPartialRange(IDocument, int, int, String, int)
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
		
		IPredicateRule rule;
		IToken token;
				
		int startIndex= -1;
		for (int i= 0; i < fRules.length; i++) {
			rule= (IPredicateRule) fRules[i];
			Object data= rule.getSuccessToken();
			if (fContentType.equals(data))
				startIndex= i;
		}
		fContentType= null;
		
		boolean resume= (startIndex > -1);
		fTokenOffset= resume ? fPartitionOffset : fOffset;
		fColumn= UNDEFINED;
					
		for (int i= Math.max(0, startIndex); i < fRules.length; i++) {
			rule= (IPredicateRule) fRules[i];
			token= rule.evaluate(this, resume);
			if (!token.isUndefined())
				return token;
		}
		
		if (read() == EOF)
			return Token.EOF;
		else
			return fDefaultReturnToken;
	}
}
