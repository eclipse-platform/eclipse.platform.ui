/*******************************************************************************
 * Copyright (c) John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     John-Mason P. Shackelford - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.formatter;

public class FormattingPreferences {
    private String canonicalIndent;

    private boolean useTabs;

    private int tabWitdth;

    private int printMargin;

    private boolean useElementWrapping;

    /**
     * @return
     */
    public String getCanonicalIndent() {
        if (this.canonicalIndent == null) {
            if (this.useTabs) {
                this.canonicalIndent = "\t"; //$NON-NLS-1$
            } else {
                String tab = ""; //$NON-NLS-1$
                for (int i = 0; i < this.tabWitdth; i++) {
                    tab = tab.concat(" "); //$NON-NLS-1$
                }
                this.canonicalIndent = tab;
            }
        }
        return this.canonicalIndent;
    }

    // TODO connect to ant preferences and remove this method (?)
    public void useHorizontalTabs() {
        this.useTabs = true;
    }

    // TODO connect to ant preferences and remove this method (?)
    public void useSpacesForTab(int tabWidth) {
        this.useTabs = false;
        this.tabWitdth = tabWidth;
    }

    // TODO connect to ant preferences and remove this method (?)
    public void setPrintMargin(int column) {
        this.printMargin = column;
    }
    /**
     * @return Returns the printMargin.
     */
    public int getPrintMargin() {
        return this.printMargin;
    }  
    
    /**
     * @return
     */
    public boolean useElementWrapping() {
        return this.useElementWrapping;
    }

    // TODO connect to ant preferences and remove this method (?)
    public void setUseElementWrapping(boolean useElementWrapping) {
        this.useElementWrapping = useElementWrapping;
    }
}
