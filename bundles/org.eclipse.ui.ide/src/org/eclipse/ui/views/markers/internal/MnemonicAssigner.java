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
    // Stores keys that have already been assigned
    private Set assigned = new HashSet();
    
    // List of all possible valid mnemonics
    private String validHotkeys = new String();
    
    // Set of all possible valid mnemonics
    private Set validKeys = new HashSet();
    
    public MnemonicAssigner() {
        // Initialize the set of valid mnemonic keys
        addKeys(Messages.getString("MnemonicAssigner.valid_mnemonics")); //$NON-NLS-1$
    }
    
    /**
     * Adds a set of keys to be considered as potentially valid mnemonics
     * 
     * @param keys
     * @return
     * @since 3.1
     */
    private void addKeys(String keys) {
        validHotkeys = validHotkeys + keys;
        
        for(int idx = 0; idx < validHotkeys.length(); idx++) {
            validKeys.add(new Character(Character.toLowerCase(keys.charAt(idx))));
        }
    }
    
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
        for(int idx = 0; idx < inputString.length() - 1; idx++) {
            char next = inputString.charAt(idx);
            
            if (next == '&') {
                if (inputString.charAt(idx + 1) == '&') {
                    // If dual-ampersand, skip it
                    idx++;
                } else {
                    return idx;
                }
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
    
    /**
     * Returns the given string without its associated mnemonic
     * 
     * @param toRemove
     * @return
     * @since 3.1
     */
    public static String withoutMnemonic(String toRemove) {
        String working = toRemove;
        
        int idx = getAmpersandIndex(working);
        while(idx < working.length()) {
            working = working.substring(0, idx) + working.substring(idx + 1, working.length());
            idx = getAmpersandIndex(working);
        }
        
        return working;
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
     * Returns true iff the given character could possibly be used as a mnemonic
     * 
     * @param next
     * @return
     * @since 3.1
     */
    public boolean isValidMnemonic(char next) {
        return validKeys.contains(new Character(Character.toLowerCase(next)));
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
        
        // Try to find a suitable mnemonic from the input string.
        String stripped = withoutMnemonic(inputString);
        
        // Index of the best mnemonic found so far
        int bestMnemonic = -1;
        
        // Rank of the best mnemonic found so far (whenever a potential mnemonic is
        // discovered in the string, we heuristically assign it a rank indicating how 
        // much we'd like this to be the mnemonic. Bigger ranks are preferred over smaller
        // ones.
        int mnemonicRank = -1;
        
        boolean lastWasWhitespace = true;
        
        for (int idx = 0; idx < stripped.length(); idx++) {
            char next = stripped.charAt(idx);
            
            if (isValidMnemonic(next) && !isReserved(next)) {
                int thisRank = 0;
                
                // Prefer upper-case characters to lower-case ones
                if (Character.isUpperCase(next)) {
                    thisRank += 1;
                }
                
                // Give characters following whitespace the highest priority
                if (lastWasWhitespace) {
                    thisRank += 2;
                }
                
                if (thisRank > mnemonicRank) {
                    bestMnemonic = idx;
                    mnemonicRank = thisRank;
                }
                
                break;
            }
            
            lastWasWhitespace = (Character.isWhitespace(next));
        }
        
        // If there was a valid mnemonic within the string, return it
        if (bestMnemonic >= 0) {
            return stripped.substring(0, bestMnemonic) + '&' 
            	+ stripped.substring(bestMnemonic, stripped.length()); 
        }
        
        // No valid mnemonics within the string. Try to append one.
        for (int idx = 0; idx < validHotkeys.length(); idx++) {
            char next = validHotkeys.charAt(idx);
            
            if (!isReserved(next)) {
                return Messages.format(Messages.getString("MnemonicAssigner.missing_mnemonic_format"), new String[] {stripped, "&" + next}); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        // No unique mnemonics remain. Leave the string unmodified
        return inputString;
    }
}
