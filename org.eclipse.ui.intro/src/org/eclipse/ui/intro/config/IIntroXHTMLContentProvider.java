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
package org.eclipse.ui.intro.config;

import org.w3c.dom.*;

/**
 * A content provider for dynamic XHTML intro content. When an XHTML intro page
 * is parsed and a contentProvider element is detected, it is used to create
 * dynamic XHTML content in the page.
 * 
 * @since 3.1
 */
public interface IIntroXHTMLContentProvider extends IIntroContentProvider {

    /**
     * Create XHTML content in the provided parent DOM Element. A typical usage
     * for this method would be: <br>
     * 
     * <br>
     * &ltdiv id=myContentProviderId> <br>
     * &lt;contentProvider id=&quot;contentProviderId&quot;
     * class=&quot;xx.yy.IntroContentProvider&quot;
     * pluginId=&quot;xx.yy.id&quot;&gt; <br>
     * &lt;contentProvider&gt; <br>
     * &lt/div&gt;
     * 
     * <br>
     * The parent Element will be passed to allow for the implementation to add
     * dynamic content by manipulating the Java XML DOM for the XHTML file.
     * 
     * 
     * @param id
     *                   the unique identifier of the content element. The same content
     *                   provider class can be reused for several elements and the id
     *                   can be used to tell them apart.
     * 
     * @param parent
     *                   the parent xml Element where dynamic content will be added as
     *                   children.
     *  
     */
    public void createContent(String id, Element parent);



}

