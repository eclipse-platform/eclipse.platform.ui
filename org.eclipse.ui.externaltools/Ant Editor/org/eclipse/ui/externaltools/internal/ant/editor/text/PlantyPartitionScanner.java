package org.eclipse.ui.externaltools.internal.ant.editor.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.*;

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
	public final static String XML_DEFAULT = "__xml_default";
	public final static String XML_COMMENT = "__xml_comment";
	public final static String XML_TAG = "__xml_tag";

    /**
     * Creates an instance.
     */
	public PlantyPartitionScanner() {

		List rules = new ArrayList();

        IToken xmlComment = new Token(XML_COMMENT);
		rules.add(new MultiLineRule("<!--", "-->", xmlComment));

        IToken tag = new Token(XML_TAG);
		rules.add(new TagRule(tag));

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
