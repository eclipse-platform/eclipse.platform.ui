/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

/**
 * Holds the information for text appearing in the about dialog
 */
public class AboutItem {
    private String text;

    private int[][] linkRanges;

    private String[] hrefs;

    /**
     * Creates a new about item
     */
    public AboutItem(String text, int[][] linkRanges, String[] hrefs) {

        this.text = text;
        this.linkRanges = linkRanges;
        this.hrefs = hrefs;
    }

    /**
     * Returns the link ranges (character locations)
     */
    public int[][] getLinkRanges() {
        return linkRanges;
    }

    /**
     * Returns the text to display
     */
    public String getText() {
        return text;
    }

    /**
     * Returns true if a link is present at the given character location
     */
    public boolean isLinkAt(int offset) {
        // Check if there is a link at the offset
        for (int i = 0; i < linkRanges.length; i++) {
            if (offset >= linkRanges[i][0]
                    && offset < linkRanges[i][0] + linkRanges[i][1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the link at the given offset (if there is one),
     * otherwise returns <code>null</code>.
     */
    public String getLinkAt(int offset) {
        // Check if there is a link at the offset
        for (int i = 0; i < linkRanges.length; i++) {
            if (offset >= linkRanges[i][0]
                    && offset < linkRanges[i][0] + linkRanges[i][1]) {
                return hrefs[i];
            }
        }
        return null;
    }
}
