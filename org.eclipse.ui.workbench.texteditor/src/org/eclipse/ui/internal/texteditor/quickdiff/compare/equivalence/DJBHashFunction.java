/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;

/**
 *
 * @since 3.2
 */
public final class DJBHashFunction implements IHashFunction {

	public Hash computeHash(CharSequence string) {
		return new IntHash(hash(string));
	}

	private int hash(CharSequence seq){
        int hash = 5381;
        int len= seq.length();
        for (int i= 0; i < len; i++) {
        	char ch= seq.charAt(i);
            hash = ((hash << 5) + hash) + ch; /* hash * 33 + ch */
        }

        return hash;
    }

}
