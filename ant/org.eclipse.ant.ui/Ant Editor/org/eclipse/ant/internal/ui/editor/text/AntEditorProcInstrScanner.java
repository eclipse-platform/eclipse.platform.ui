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
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

/**
 * The scanner to tokenize for XML processing instructions and text
 */
public class AntEditorProcInstrScanner extends RuleBasedScanner {

	Token fProcInstructionToken= null;
	
    public AntEditorProcInstrScanner() {
		IRule[] rules =new IRule[2];
        fProcInstructionToken =
            new Token(
                new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.P_PROC_INSTR)));

        //Add rule for processing instructions
        rules[0]= new SingleLineRule("<?", "?>", fProcInstructionToken); //$NON-NLS-1$ //$NON-NLS-2$

        // Add generic whitespace rule.
        rules[1]= new WhitespaceRule(new AntEditorWhitespaceDetector());

        setRules(rules);
        
        setDefaultReturnToken(new Token(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.P_DEFAULT))));
    }

	/**
	 * Update the text attributes associated with the tokens of this scanner as a color preference has been changed. 
	 */
	public void adaptToColorChange() {
		((Token)fDefaultReturnToken).setData(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.P_DEFAULT)));
		fProcInstructionToken.setData(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.P_PROC_INSTR)));
	}
}