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

import org.eclipse.help.internal.webapp.servlet.AboutServlet;

/**
 * Generates an html page having informations about either <code>User-Agent</code>,
 * Help system <code>preferences</code> or the available plug-ins in Help system
 * like Provider, Plugin name, Version and PluginId.
 * 
 * <p>Extends the {@link org.eclipse.help.internal.webapp.servlet.AboutServlet}
 * servlet.
 * 
 * @param show			- (Optional) specifying either <code>agent</code>
 * 						to view the request's <code>User-Agent</code> info, else
 * 						<code>preferences</code> to view the Help system preferences.
 * 						Do not specify any value to show the available plugins in
 * 						Help web application.
 * @param sortColumn	- (Optional) specifying the column number over
 * 						which displayed output needs to be sorted. Applicable only if
 * 						<code>show</code> parameter is <code>null</code>. 
 * 
 * @return		Html page having informations about either <code>User-Agent</code>,
 * <code>preferences</code> or the available plug-ins.
 * 
 * @version	$Version$
 * 
 **/
public class AboutService extends AboutServlet {

	private static final long serialVersionUID = 1L;

}
