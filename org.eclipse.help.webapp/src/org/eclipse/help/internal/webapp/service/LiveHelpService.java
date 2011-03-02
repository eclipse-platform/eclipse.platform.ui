/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.service;

import org.eclipse.help.internal.webapp.servlet.LiveHelpServlet;

/**
 * Handles the live help action requests with the specified 
 * <code>pluginID</code>, <code>class</code> and respective <code>args</code>.
 * 
 * <p>Extends the {@link org.eclipse.help.internal.webapp.servlet.LiveHelpServlet}
 * servlet.
 * 
 * @param pluginID		- specifying the plugin id
 * @param class			- specifying the class
 * @param arg			- (Optional) specifying the arguments for the
 * 						  live help extension
 * 
 * @version	$Version$
 * 
 **/
public class LiveHelpService extends LiveHelpServlet {

	private static final long serialVersionUID = 1L;

}
