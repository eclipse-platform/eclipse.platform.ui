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

import org.eclipse.jface.text.*;
import java.util.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**
 * @version 22.10.2002
 */
public class PlantyTagScanner extends RuleBasedScanner {

    public PlantyTagScanner() {
        IToken string =
            new Token(
                new TextAttribute(
                    ExternalToolsPlugin.getPreferenceColor(PlantyColorConstants.P_STRING)));

        Vector rules = new Vector();

        // Add rule for single and double quotes
        rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new PlantyWhitespaceDetector()));

        IRule[] result = new IRule[rules.size()];
        rules.copyInto(result);
        setRules(result);
    }
    public IToken nextToken() {
        return super.nextToken();
    }
}
