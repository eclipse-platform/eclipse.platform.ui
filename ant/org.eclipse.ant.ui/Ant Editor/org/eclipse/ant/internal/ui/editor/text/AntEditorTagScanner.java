/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 31796, bug 24108, bug 47139
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

/**
 * The scanner to tokenize for strings and tags
 */
public class AntEditorTagScanner extends RuleBasedScanner {

	private Token fStringToken;
	
    public AntEditorTagScanner() {
    	fStringToken= new Token(
                			new TextAttribute(
                				AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.STRING_COLOR)));
                    
		IRule[] rules= new IRule[3];

        // Add rule for single and double quotes
        rules[0]= new MultiLineRule("\"", "\"", fStringToken, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
        rules[1]= new SingleLineRule("'", "'", fStringToken, '\\'); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules[2]= new WhitespaceRule(new AntEditorWhitespaceDetector());

        setRules(rules);
        
        setDefaultReturnToken(
        		new Token(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.TAG_COLOR))));
    }
    
    /**
     * Update the text attributes associated with the tokens of this scanner as a color preference has been changed. 
     */
    public void adaptToColorChange() {
    	((Token)fDefaultReturnToken).setData(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.TAG_COLOR)));
    	fStringToken.setData(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.STRING_COLOR)));
    }
}
