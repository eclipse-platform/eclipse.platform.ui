/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.internal.model;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;

/**
 * An Intro Part home page. A home page is special because it is the page that
 * decides whether the OOBE pages are dynamic or static.
 */
public class IntroHomePage extends AbstractIntroPage {

    private static final String ATT_URL = "url";
    private static final String ATT_STANDBY_URL = "standby-url";
    private static final String ATT_STANDBY_STYLE = "standby-style";
    private static final String ATT_STANDBY_ALT_STYLE = "standby-alt-style";

    private String url;
    private String standby_url;
    private String standby_style;
    private String standby_alt_style;
    private boolean isDynamic = false;


    IntroHomePage(Element element, IPluginDescriptor pd) {
        super(element, pd);
        url = getAttribute(element, ATT_URL);
        if (url == null) {
            // if we do not have a URL attribute, then we have dynamic content.
            isDynamic = true;
            standby_style = getAttribute(element, ATT_STANDBY_STYLE);
            standby_alt_style = getAttribute(element, ATT_STANDBY_ALT_STYLE);

            // Resolve standby styles. The ALT style need not be resolved.
            standby_style = IntroModelRoot.getPluginLocation(standby_style, pd);
        } else {
            // check the url/standby-url attributes and update accordingly.
            url = IntroModelRoot.resolveURL(url, pd);
            standby_url = getAttribute(element, ATT_STANDBY_URL);
            standby_url = IntroModelRoot.resolveURL(standby_url, pd);
        }
    }


    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return Returns the standby_url.
     */
    public String getStandbyUrl() {
        return standby_url;
    }

    /**
     * Returns true if this is a dynamic model or not. This is based on whether
     * this root page has a URL attribute or not.
     * 
     * @return Returns the isDynamic.
     */
    public boolean isDynamic() {
        return isDynamic;
    }

    /**
     * @return Returns the standby_style.
     */
    public String getStandbyStyle() {
        return standby_style;
    }

    /**
     * @return Returns the standby_alt_style.
     */
    public String getStandbyAltStyle() {
        return standby_alt_style;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.internal.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.HOME_PAGE;
    }

    // THESE METHODS WILL BE REMOVED. ADDED HERE FOR BACKWARD COMPATIBILITY.
    /**
     * This method is a customized method for root page to return the root page
     * links. Try to get the real links in the page. If there are non, return
     * the links in the first no-empty div.
     */
    public IntroLink[] getLinks() {
        // DONOW:
        IntroLink[] links = (IntroLink[]) getChildrenOfType(AbstractIntroElement.LINK);
        if (links.length != 0)
            return links;

        // root page does not have any links, append all links off non-filtered
        // divs.
        IntroDiv[] rootPageDivs = getDivs();
        Vector linkVector = new Vector();
        for (int i = 0; i < rootPageDivs.length; i++)
            linkVector.addAll(Arrays.asList(rootPageDivs[i].getLinks()));
        links = new IntroLink[linkVector.size()];
        linkVector.copyInto(links);

        return links;
    }

    /**
     * Returns the divs in this root page. HTML presentation divs are filtered
     * out, for now.
     */
    public IntroDiv[] getDivs() {
        // get real page divs.
        IntroDiv[] divs = (IntroDiv[]) getChildrenOfType(AbstractIntroElement.DIV);

        // filter bad stuff for now.
        Vector vectorDivs = new Vector(Arrays.asList(divs));
        for (int i = 0; i < vectorDivs.size(); i++) {
            IntroDiv aDiv = (IntroDiv) vectorDivs.elementAt(i);
            if (aDiv.getId().equals("background-image")
                    || aDiv.getId().equals("root-background")) {
                vectorDivs.remove(aDiv);
                i--;
            }

        }

        // return proper object type.
        IntroDiv[] filteredDivs = new IntroDiv[vectorDivs.size()];
        vectorDivs.copyInto(filteredDivs);
        return filteredDivs;
    }

}

