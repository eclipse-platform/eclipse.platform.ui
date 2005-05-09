/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.model;

import org.osgi.framework.Bundle;
import org.w3c.dom.Element;


/**
 * An Intro IFrame element that has been injected at runtime to embed a url. It
 * is placed as the only child of a target div to display the content of a url.
 * The URL can be any valid URL.
 */
public class IntroInjectedIFrame extends AbstractIntroIdElement {

    private String url;

    IntroInjectedIFrame(Element element, Bundle bundle) {
        super(element, bundle);
    }

    public void setIFrameURL(String url) {
        this.url = url;
    }

    public String getIFrameURL() {
        return this.url;
    }


    public int getType() {
        return AbstractIntroElement.INJECTED_IFRAME;
    }


}
