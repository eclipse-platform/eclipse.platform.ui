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

import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.intro.*;

/**
 * Intro Action.
 */
public interface IIntroAction extends IAction {

    /**
     * Called right before the run method of the action. The properties
     * represents the key=value pairs extracted from the intro URL query.
     * 
     * @param site
     * @param params
     */
    void initialize(IIntroSite site, Properties params);

}
