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

package org.eclipse.ui.internal.intro.impl.model;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;


/**
 * An Intro Config component that has an id attribute and a class attribute. It
 * is used as a base class for all config elements that can take id and class as
 * attribute.
 * 
 * Note: since some model classes do not have class-id attribute, the attribute
 * is loaded in this base class, but subclasses are responsible for exposing the
 * attribute.
 */
public abstract class AbstractBaseIntroElement extends AbstractIntroElement {

    public static final String ATT_ID = "id";
    private static final String ATT_CLASS_ID = "class-id";

    protected String id;
    protected String class_id;

    AbstractBaseIntroElement(IConfigurationElement element) {
        super(element);
        id = element.getAttribute(ATT_ID);
        class_id = element.getAttribute(ATT_CLASS_ID);
    }

    AbstractBaseIntroElement(Element element, IPluginDescriptor pd) {
        super(element, pd);
        id = getAttribute(element, ATT_ID);
        class_id = getAttribute(element, ATT_CLASS_ID);
    }



    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }


}