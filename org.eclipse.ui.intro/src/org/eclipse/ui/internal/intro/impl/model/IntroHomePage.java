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

package org.eclipse.ui.internal.intro.impl.model;

import java.util.*;

import org.osgi.framework.*;
import org.w3c.dom.*;

/**
 * An Intro Part home page. A home page is special because it is the page that
 * decides whether the OOBE pages are dynamic or static.
 */
public class IntroHomePage extends AbstractIntroPage {

    private static final String ATT_URL = "url"; //$NON-NLS-1$
    private static final String ATT_STANDBY_URL = "standby-url"; //$NON-NLS-1$
    private static final String ATT_STANDBY_STYLE = "standby-style"; //$NON-NLS-1$
    private static final String ATT_STANDBY_ALT_STYLE = "standby-alt-style"; //$NON-NLS-1$

    private String url;
    private String standby_url;
    private String standby_style;
    private String standby_alt_style;
    private boolean isDynamic = false;


    IntroHomePage(Element element, Bundle bundle) {
        super(element, bundle);
        url = getAttribute(element, ATT_URL);
        if (url == null) {
            // if we do not have a URL attribute, then we have dynamic content.
            isDynamic = true;
            standby_style = getAttribute(element, ATT_STANDBY_STYLE);
            standby_alt_style = getAttribute(element, ATT_STANDBY_ALT_STYLE);

            // Resolve standby styles. The ALT style need not be resolved.
            standby_style = IntroModelRoot.getPluginLocation(standby_style,
                    bundle);
        } else {
            // check the url/standby-url attributes and update accordingly.
            url = IntroModelRoot.resolveURL(url, bundle);
            standby_url = getAttribute(element, ATT_STANDBY_URL);
            standby_url = IntroModelRoot.resolveURL(standby_url, bundle);
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
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
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
        IntroDiv[] rootPageDivs = (IntroDiv[]) getChildrenOfType(AbstractIntroElement.DIV);
        Vector linkVector = new Vector();

        for (int i = 0; i < rootPageDivs.length; i++)
            addLinks(rootPageDivs[i], linkVector);

        links = new IntroLink[linkVector.size()];
        linkVector.copyInto(links);

        return links;
    }

    private void addLinks(IntroDiv div, Vector linksVector) {
        linksVector.addAll(Arrays.asList(getLinks(div)));
        IntroDiv[] divs = (IntroDiv[]) div
                .getChildrenOfType(AbstractIntroElement.DIV);
        for (int i = 0; i < divs.length; i++)
            addLinks(divs[i], linksVector);
    }


    public IntroLink[] getLinks(IntroDiv div) {
        return (IntroLink[]) div.getChildrenOfType(AbstractIntroElement.LINK);
    }


}

