/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.*;

import org.apache.lucene.analysis.*;

/**
 * Tokenizer breaking words around letters or digits.
 */
public class LowerCaseAndDigitsTokenizer extends CharTokenizer {

    public LowerCaseAndDigitsTokenizer(Reader input) {
        super(input);
    }
    protected char normalize(char c) {
        return Character.toLowerCase(c);
    }

    protected boolean isTokenChar(char c) {
        return Character.isLetterOrDigit(c);
    }

}
