/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * Can be used to automatically assign non-conflicting mnemonics to SWT widgets.
 * Note: the current implementation is pretty bare-bones, and is intended to fix
 * a couple dialogs where the widgets are generated programmatically. If this were
 * to go into more general use, it would have to take into account preferred hotkeys
 * for the locale and have preferences for words coming after whitespace, etc. 
 * 
 * @since 3.1
 */
public class MnemonicAssigner {
    private Set assigned = new HashSet();
    
    public boolean isReserved(char toCheck) {
        return assigned.contains(new Character(Character.toLowerCase(toCheck)));
    }
    
    /**
     * Returns the index of the mnemonic in the given string. Returns 
     * inputString.length() if the string does not contain a mnemonic.
     * 
     * @param inputString
     * @return
     * @since 3.1
     */
    private static int getAmpersandIndex(String inputString) {
        for(int idx = 0; idx < inputString.length(); idx++) {
            char next = inputString.charAt(idx);
            
            if (next == '&') {
                return idx;
            }
        }
        
        return inputString.length();
    }
    
    /**
     * Returns the current mnemonic in the given input string. Returns
     * 0 if none.
     * 
     * @param inputString
     * @return
     * @since 3.1
     */
    public static char getMnemonic(String inputString) {
        int idx = getAmpersandIndex(inputString);
        
        if (idx < inputString.length() - 1) {
            return inputString.charAt(idx + 1);
        }
        
        return 0;
    }
    
    public void reserve(char toReserve) {
        assigned.add(new Character(Character.toLowerCase(toReserve)));
    }
    
    public static String withoutMnemonic(String toRemove) {
        int idx = getAmpersandIndex(toRemove);
        String result = toRemove.substring(0, idx);
        
        if (idx < toRemove.length()) {
            result += toRemove.substring(idx + 1, toRemove.length());
        }
        
        return result;
    }
    
    /**
     * Suggests a unique mnemonic for the given input string and reserves it, preventing
     * future conflicts
     * 
     * @param inputString
     * @return
     * @since 3.1
     */
    public String assign(String inputString) {
        String result = suggest(inputString);
        reserve(result);
        return result;
    }
    
    /**
     * Reserves the mnemonic that is currently being used by the given input string
     * 
     * @param inputString
     * @since 3.1
     */
    public void reserve(String inputString) {
       reserve(getMnemonic(inputString)); 
    }
    
    /**
     * Suggests a unique mnemonic for the given input string that does not conflict with 
     * any existing mnemonics.
     * 
     * @param inputString
     * @return the string with a unique mnemonic
     * @since 3.1
     */
    public String suggest(String inputString) {
        char mnemonic = getMnemonic(inputString); 
        
        if (mnemonic != 0 && !isReserved(mnemonic)) {
            return inputString;
        }
        
        // Try to find a suitable mnemonic from the input string. Search left-to-right.
        String stripped = withoutMnemonic(inputString);
        
        for (int idx = 0; idx < stripped.length(); idx++) {
            char next = stripped.charAt(idx);
            
            if (!isReserved(next)) {
                return stripped.substring(0, idx) + '&' + stripped.substring(idx, stripped.length());
            }
        }
        
        // Currently, if we can't find a unique mnemonic, we just leave the string unmodified.
        // This may leave the string with a duplicate or unassigned mnemonic... We should really
        // append a suitable mnemonic from a set of possible mnemonics in the user's locale.
        return inputString;
    }
}
