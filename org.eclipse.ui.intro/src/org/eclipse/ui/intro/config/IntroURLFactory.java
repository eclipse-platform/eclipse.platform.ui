/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.config;

import org.eclipse.ui.internal.intro.impl.model.url.IntroURL;
import org.eclipse.ui.internal.intro.impl.model.url.IntroURLParser;

/**
 * Factory class used to create instances of an Intro URL. Instances of intro
 * URLs need to be created if you need to programatically construct and execute
 * a valid Intro URL.
 * <p>
 * This class provides all its functionality via static members. It is not
 * intended to be instantiated.
 * </p>
 * 
 * @see IIntroURL
 * @since 3.0
 */
public final class IntroURLFactory {

    /**
     * Non-instantiable.
     */
    private IntroURLFactory() {
        // do nothing
    }


    /**
     * Parses the given string, and returns an IntroURL if the string is a valid
     * Intro URL. Returns null in all other cases. Example usage:
     * 
     * <pre>
     * StringBuffer url = new StringBuffer();
     * url.append(&quot;http://org.eclipse.ui.intro/showStandby?&quot;);
     * url.append(&quot;pluginId=org.eclipse.pde.ui&quot;);
     * url.append(&quot;&amp;&quot;);
     * url.append(&quot;partId=org.eclipse.pde.ui.sampleStandbyPart&quot;);
     * url.append(&quot;&amp;&quot;);
     * url.append(&quot;input=&quot;);
     * url.append(sampleId);
     * IIntroURL introURL = IntroURLFactory.createIntroURL(url.toString());
     * if (introURL != null) {
     *     introURL.execute();
     * }
     * </pre>
     * 
     * @param url
     *            the url to construct an IntroURL from
     * @return an IntroURL, or <code>null</code> if the url is invalid
     */
    public static IIntroURL createIntroURL(String url) {
        IntroURLParser parser = new IntroURLParser(url);
        if (parser.hasIntroUrl()) {
            IntroURL introURL = parser.getIntroURL();
            return introURL;
        }
        return null;
    }

}
