package org.eclipse.ui.externaltools.internal.ant.editor.text;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
// 

/*
 * This file originates from an internal package of Eclipse's 
 * Manifest Editor. It has been copied by GEBIT to here in order to
 * permanently use those features. It has been renamed and edited by GEBIT 
 * after copying.
 */

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Scanner that scans the document and partitions the document into the three 
 * supported content types:
 * <ul>
 * <li>XML_COMMENT</li>
 * <li>XML_TAG</li>
 * <li>XML_DEFAULT</li>
 * </ul>
 * 
 * @version 22.10.2002
 */
public class PlantyPartitionScanner extends RuleBasedPartitionScanner {
	public class MultiLineRuleWithEOF extends MultiLineRule {
		public MultiLineRuleWithEOF(String startSequence, String endSequence, IToken token) {
			super(startSequence, endSequence, token);
		}
		protected boolean endSequenceDetected(ICharacterScanner scanner) {
			boolean result= super.endSequenceDetected(scanner);
			if (!result) {
				if (scanner.read() == ICharacterScanner.EOF) {
					result= true;
				} else {
					scanner.unread();
				}
			}
			return result;
		}
	}

	public final static String XML_DEFAULT = "__xml_default"; //$NON-NLS-1$
	public final static String XML_COMMENT = "__xml_comment"; //$NON-NLS-1$
	public final static String XML_TAG = "__xml_tag"; //$NON-NLS-1$

    /**
     * Creates an instance.
     */
	public PlantyPartitionScanner() {

		IPredicateRule[] rules =new IPredicateRule[2];

        IToken xmlComment = new Token(XML_COMMENT);
		rules[0]= new MultiLineRuleWithEOF("<!--", "-->", xmlComment); //$NON-NLS-1$ //$NON-NLS-2$

        IToken tag = new Token(XML_TAG);
		rules[1]= new TagRule(tag);
	
		setPredicateRules(rules);
	}
}
