/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;


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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

public class PlantyTagScanner extends RuleBasedScanner {

    public PlantyTagScanner() {
        IToken string = 
        	new Token(
                new TextAttribute(
                    ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_STRING)));
                    
		IRule[] rules =new IRule[3];

        // Add rule for single and double quotes
        rules[0]= new MultiLineRule("\"", "\"", string, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
        rules[1]= new SingleLineRule("'", "'", string, '\\'); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules[2]= new WhitespaceRule(new PlantyWhitespaceDetector());

        setRules(rules);
    }
}
