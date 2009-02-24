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

import java.util.Properties;

import org.eclipse.ui.intro.IIntroSite;

/**
 * An Intro action. Classes that implement this interface can be used as valid
 * value for the "class" parameter for the following intro url:
 * <p>
 * http://org.eclipse.ui.intro/runAction?pluginId=x.y.z&class=x.y.z.someClass
 * </p>
 * 
 * @since 3.0
 */
public interface IIntroAction {

    /**
     * Called to run this intro command. The properties represents the key=value
     * pairs extracted from the intro URL query.
     * 
     * @param site
     * @param params
     */
    public void run(IIntroSite site, Properties params);

}
