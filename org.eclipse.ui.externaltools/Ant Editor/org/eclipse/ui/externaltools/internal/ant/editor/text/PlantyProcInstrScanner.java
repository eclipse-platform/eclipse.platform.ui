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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**
 * @version 22.10.2002
 */
public class PlantyProcInstrScanner extends RuleBasedScanner {

    public PlantyProcInstrScanner() {
        List rules = new ArrayList();
        IToken procInstr =
            new Token(
                new TextAttribute(
                    ExternalToolsPlugin.getPreferenceColor(PlantyColorConstants.P_PROC_INSTR)));

        //Add rule for processing instructions
        rules.add(new SingleLineRule("<?", "?>", procInstr)); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new PlantyWhitespaceDetector()));

        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }
}
