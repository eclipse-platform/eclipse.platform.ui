/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 24108
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor.text;

/*
 * This file originates from an internal package of Eclipse's 
 * Manifest Editor. It has been copied by GEBIT to here in order to
 * permanently use those features. It has been renamed and edited by GEBIT 
 * after copying.
 */

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

public class AntEditorProcInstrScanner extends RuleBasedScanner {

    public AntEditorProcInstrScanner() {
		IRule[] rules =new IRule[2];
        IToken procInstr =
            new Token(
                new TextAttribute(
                    ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_PROC_INSTR)));

        //Add rule for processing instructions
        rules[0]= new SingleLineRule("<?", "?>", procInstr); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules[1]= new WhitespaceRule(new AntEditorWhitespaceDetector());

        setRules(rules);
    }
}
