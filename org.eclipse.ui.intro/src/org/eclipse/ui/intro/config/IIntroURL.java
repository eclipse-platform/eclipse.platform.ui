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

/**
 * An intro url. An intro URL is a valid http url, with org.eclipse.ui.intro as
 * a host. This class holds all logic to execute Intro URL commands, ie: an
 * Intro URL knows how to execute itself.
 */
public interface IIntroURL {
    /**
     * Executes whatever valid Intro action is embedded in this Intro URL.
     *  
     */
	public void execute();

    /**
     * @return Returns the action imbedded in this URL.
     */
	public String getAction();

    /**
     * Return a parameter defined in the Intro URL. Returns null if the
     * parameter is not defined.
     * 
     * @param parameterId
     * @return
     */
	public String getParameter(String parameterId);
}
