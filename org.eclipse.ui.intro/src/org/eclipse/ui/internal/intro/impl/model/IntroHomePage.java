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
 * An Intro Home page. A home page is special because it is the page that
 * decides whether the OOBE pages are dynamic or static. This model class models
 * the home and the standby page (since there is no difference between the two).
 */
public class IntroHomePage extends AbstractIntroPage {

    private static final String ATT_URL = "url"; //$NON-NLS-1$

    private String url;
    private boolean isDynamic = false;
    private boolean isStandbyPage;


    IntroHomePage(Element element, Bundle bundle) {
        super(element, bundle);
        url = getAttribute(element, ATT_URL);
        if (url == null)
            // if we do not have a URL attribute, then we have dynamic content.
            isDynamic = true;
        else
            // check the url/standby-url attributes and update accordingly.
            url = IntroModelRoot.resolveURL(url, bundle);
    }


    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
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


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.intro.impl.model.IntroElement#getType()
     */
    public int getType() {
        return AbstractIntroElement.HOME_PAGE;
    }


    /**
     * @return Returns the isStandbyPage.
     */
    public boolean isStandbyPage() {
        return isStandbyPage;
    }

    /**
     * @param isStandbyPage
     *            The isStandbyPage to set.
     */
    public void setStandbyPage(boolean isStandbyPage) {
        this.isStandbyPage = isStandbyPage;
    }


    // THESE METHODS WILL BE REMOVED!
    /**
     * This method is a customized method for root page to return the root page
     * links. Try to get the real links in the page, and all links in all divs.
     */
    public IntroLink[] getLinks() {
        Vector linkVector = new Vector();

        AbstractIntroElement[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            AbstractIntroElement child = children[i];
            if (child.isOfType(AbstractIntroElement.LINK))
                linkVector.add(child);
            else if (child.isOfType(AbstractIntroElement.GROUP)) {
                addLinks((IntroGroup) child, linkVector);
            }
        }

        IntroLink[] links = new IntroLink[linkVector.size()];
        linkVector.copyInto(links);
        return links;
    }

    private void addLinks(IntroGroup group, Vector linkVector) {
        AbstractIntroElement[] children = group.getChildren();
        for (int i = 0; i < children.length; i++) {
            AbstractIntroElement child = children[i];
            if (child.isOfType(AbstractIntroElement.LINK))
                linkVector.add(child);
            else if (child.isOfType(AbstractIntroElement.GROUP)) {
                addLinks((IntroGroup) child, linkVector);
            }
        }
    }


}

