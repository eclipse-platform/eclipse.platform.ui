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
package org.eclipse.ui.internal.intro.impl.model;

import java.net.*;
import java.util.*;

/**
 * A parser that knows how to parser OOBE action URLs. If URL is a valid intro
 * url, it will create an instance of the IntroURL class.
 */
public class IntroURLParser {

    private String url_string = null;
    private boolean hasProtocol = false;
    private boolean isIntroUrl = false;
    private String action = null;
    private Properties parameters = null;

    private IntroURL introURL = null;

    /**
     * Constructor that gets the URL to parse.
     */
    public IntroURLParser(String url) {
        url_string = url;
        // create a URL instance, and parser it for parameters.
        parseUrl(url);
        if (isIntroUrl) {
            // class instance vars are already populated by now.
            introURL = new IntroURL(action, parameters);
        }
    }

    private void parseUrl(String url) {
        if (url == null)
            return;
        URL url_inst = null;
        try {
            url_inst = new URL(url);
        } catch (MalformedURLException e) {
            // not a valid URL. set state.
            return;
        }

        if (url_inst.getProtocol() != null) {
            // URL has some valid protocol. Check to see if it is an intro
            // url.
            hasProtocol = true;
            isIntroUrl = isIntoUrl(url_inst);
            if (isIntroUrl) {
                // valid intro URL. Extract the action and parameters.
                action = getPathAsAction(url_inst);
                parameters = getQueryParameters(url_inst);
            }
            return;
        }

        // not an Intro URL. do nothing.
        return;
    }

    /**
     * Checks to see if tha passed URL is an Intro URL. An intro URL is an http
     * URL that has the intro plugin id as a host. eg:
     * "http://org.eclipse.ui.intro/test".
     * 
     * @param url
     * @return true if url is an intro URL.
     */
    private boolean isIntoUrl(URL url) {
        if (!url.getProtocol().equalsIgnoreCase(IntroURL.INTRO_PROTOCOL))
            // quick exit. If it is not http, url is not an Intro url.
            return false;

        if (url.getHost().equalsIgnoreCase(IntroURL.INTRO_HOST_ID))
            return true;
        return false;
    }

    /**
     * Retruns the path attribute of the passed URL, stripped out of the leading
     * "/". Returns null if the url does not have a path.
     * 
     * @param url
     * @return
     */
    private String getPathAsAction(URL url) {
        // get possible action.
        String action = url.getPath();
        // remove leading "/" from path.
        if (action != null)
            action = action.substring(1);
        return action;
    }

    /**
     * Retruns the Query part of the URL as an instance of a Properties class.
     * 
     * @param url
     * @return
     */
    public Properties getQueryParameters(URL url) {
        // parser all query parameters.
        Properties properties = new Properties();
        String query = url.getQuery();
        if (query == null)
            // we do not have any parameters in this URL, return an empty
            // Properties instance.
            return properties;

        // now extract the key/value pairs from the query.
        String[] params = query.split("&"); //$NON-NLS-1$
        for (int i = 0; i < params.length; i++) {
            // for every parameter, ie: key=value pair, create a property
            // entry. we know we have the key as the first string in the array,
            // and the value as the second array.
            String[] keyValuePair = params[i].split("="); //$NON-NLS-1$
            properties.setProperty(keyValuePair[0], keyValuePair[1]);
        }
        return properties;
    }

    /**
     * @return Returns the hasProtocol.
     */
    public boolean hasProtocol() {
        return hasProtocol;
    }

    /**
     * @return Returns the isIntroUrl.
     */
    public boolean hasIntroUrl() {
        return isIntroUrl;
    }

    /**
     * @return Returns the introURL. Will be null if the parsed URL is not an
     *         Intro URL.
     */
    public IntroURL getIntroURL() {
        return introURL;
    }

}
