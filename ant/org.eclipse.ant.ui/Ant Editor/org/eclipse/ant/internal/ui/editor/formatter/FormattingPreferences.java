/*******************************************************************************
 * Copyright (c) 2004 John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 * 	   IBM Corporation - bug 52076
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.formatter;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class FormattingPreferences {
	
	IPreferenceStore fPrefs= AntUIPlugin.getDefault().getPreferenceStore();
   
    public String getCanonicalIndent() {
       String canonicalIndent;
       if (!useSpacesInsteadOfTabs()) {
            canonicalIndent = "\t"; //$NON-NLS-1$
        } else {
            String tab = ""; //$NON-NLS-1$
            for (int i = 0; i < getTabWidth(); i++) {
                tab = tab.concat(" "); //$NON-NLS-1$
            }
            canonicalIndent = tab;
        }
        
        return canonicalIndent;
    }
    
    public int getMaximumLineWidth() {
        return fPrefs.getInt(AntEditorPreferenceConstants.FORMATTER_MAX_LINE_LENGTH);
    }  
    
    public boolean wrapLongTags() {
        return fPrefs.getBoolean(AntEditorPreferenceConstants.FORMATTER_WRAP_LONG);
    }
    
    public boolean alignElementCloseChar() {
        return fPrefs.getBoolean(AntEditorPreferenceConstants.FORMATTER_ALIGN);        
    }

	public int getTabWidth() {
		return fPrefs.getInt(AntEditorPreferenceConstants.FORMATTER_TAB_SIZE);
	}
	
	public boolean useSpacesInsteadOfTabs() {
    	return ! fPrefs.getBoolean(AntEditorPreferenceConstants.FORMATTER_TAB_CHAR);
    }
}