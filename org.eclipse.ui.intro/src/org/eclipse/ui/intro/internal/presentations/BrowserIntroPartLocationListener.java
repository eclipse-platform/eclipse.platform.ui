/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.internal.presentations;

import org.eclipse.swt.browser.*;
import org.eclipse.ui.intro.internal.model.*;

/**
 * A Location Listener that knows how to intercept OOBE action URLs. It also
 * knows how to update UI navigation hisutory.
 */
public class BrowserIntroPartLocationListener implements LocationListener {

    private AbstractIntroPartImplementation implementation;

    // flag used to filter out mutiple URL navigation events in one URL due to
    // frames.
    private int redundantNavigation = 0;

    /**
     * Takes the implementation as an input.
     */
    public BrowserIntroPartLocationListener(
            AbstractIntroPartImplementation implementation) {
        this.implementation = implementation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
     */
    public void changed(LocationEvent event) {
        // note: navigations fired due to a setText do not fire closing changed.
        redundantNavigation--;
    }

    /**
     * Intercept any LocationEvents on the browser. If the event location is a
     * valid IntroURL, cancel the event and execute the intro action that is
     * embedded in the URL
     */
    public void changing(LocationEvent event) {
        String url = event.location;
        if (url == null)
            return;
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasIntroUrl()) {
            // stop URL first.
            event.doit = false;
            // execute the action embedded in the IntroURL
            IntroURL introURL = parser.getIntroURL();
            introURL.execute();
            // if action is a show page, update UI history.
            if (introURL.getAction().equals(IntroURL.SHOW_PAGE))
                implementation.getModelRoot().getPresentation().updateHistory(
                        introURL.getParameter(IntroURL.KEY_ID));
            return;
        }

        if (parser.hasProtocol()) {
            // Update the history even with real URLs. If this listener gets
            // called due to a navigation, the navigation state controls the
            // update. Note that if we have multiple embedded URL navigations
            // due to frames, the redundantNavigation flag filters them out.
            if (redundantNavigation == 0)
                implementation.getModelRoot().getPresentation().updateHistory(
                        url);
        }
        redundantNavigation++;
        return;
    }

}