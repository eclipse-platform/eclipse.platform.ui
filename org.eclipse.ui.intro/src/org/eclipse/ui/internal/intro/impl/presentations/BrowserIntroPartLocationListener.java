/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.presentations;

import org.eclipse.swt.browser.*;
import org.eclipse.ui.internal.intro.impl.model.*;

/**
 * A Location Listener that knows how to intercept OOBE action URLs. It also
 * knows how to update UI navigation hisutory.
 */
public class BrowserIntroPartLocationListener implements LocationListener {

    private BrowserIntroPartImplementation implementation;

    /**
     * Takes the implementation as an input.
     */
    public BrowserIntroPartLocationListener(
            BrowserIntroPartImplementation implementation) {
        this.implementation = implementation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
     */
    public void changed(LocationEvent event) {
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
            return;
        }

        if (!parser.hasProtocol())
            // this will filter out two navigation events fired by the browser
            // on a setText. (about:blank and
            // res://C:\WINDOWS\System32\shdoclc.dll/navcancl.htm)
            return;

        if (implementation.getModel().isDynamic()) {
            // Update the history even with real URLs. Note that if we have
            // multiple embedded URL navigations due to frames, the
            // frameNavigation flag filters them. This flag is set here, and is
            // cleared by a progress listener, when all the frame navigation is
            // completed. We need to update history only in dynamic case. In
            // static case, the browser keeps the history.
            Browser browser = (Browser) event.getSource();
            if (browser.getData("frameNavigation") == null) { //$NON-NLS-1$
                browser.setData("frameNavigation", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                implementation.updateHistory(url);
            }
        }
        return;
    }

}