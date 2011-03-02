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

import org.eclipse.help.internal.webapp.servlet.ControlServlet;

/**
 * Controls Eclipse help application from standalone application. This
 * service do not allow remote clients to control Eclipse help application.
 * 
 * <p>Extends the {@link org.eclipse.help.internal.webapp.servlet.ControlServlet}
 * servlet.
 * 
 * @param command		- specifies the control command. Accepts the following parameters: 
 * 						displayHelp | displayHelpWindow | shutdown
 * 						| install | update | enable | disable | uninstall
 * 						| search | listFeatures | addSite | removeSite
 * 						| apply
 * 
 * @version	$Version$
 * 
 **/
public class ControlService extends ControlServlet {

	private static final long serialVersionUID = 1L;

}
