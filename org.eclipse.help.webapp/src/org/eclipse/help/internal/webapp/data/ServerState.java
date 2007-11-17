/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.base.BaseHelpSystem;

public class ServerState {
	
	/**
	 * Function called when index.jsp is loaded, the main purpose here is to detect the 
	 * situation where the Webapp is run without being launched on the built in server
	 * in which case we set the mode to infocenter.
	 */
    public static void webappStarted(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) { 
    	    BaseHelpSystem.checkMode();
    }
    
}
