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

import org.eclipse.jface.text.rules.*;

/**
 * @version 22.10.2002
 */
public class TagRule extends MultiLineRule {

    public TagRule(IToken token) {
        super("<", ">", token); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected boolean sequenceDetected(
        ICharacterScanner scanner,
        char[] sequence,
        boolean eofAllowed) {
        int c = scanner.read();
        if (sequence[0] == '<') {
            if (c == '?') {
                // processing instruction - abort
                scanner.unread();
                return false;
            }
            if (c == '!') {
                scanner.unread();
                // comment - abort
                return false;
            }
        } else if (sequence[0] == '>') {
            scanner.unread();
        }

        return super.sequenceDetected(scanner, sequence, eofAllowed);
    }

}
