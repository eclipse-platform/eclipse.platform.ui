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

import java.util.*;
import org.eclipse.jface.text.rules.*;

/** 
 * Scanner that scans the document for comments.
 * 
 * @version 22.10.2002
 */
public class PlantyCommentScanner extends RuleBasedScanner {

    /**
     * Creates an instance.
     */
    public PlantyCommentScanner(IColorManager manager) {
        IToken comment =
            new Token(
                new Token(
                    manager.getColor(PlantyColorConstants.P_XML_COMMENT)));

        List rules = new ArrayList();

        // Add rule for comments.
        rules.add(new MultiLineRule("<!--", "-->", comment));

        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }
}
