/*******************************************************************************
 * Copyright (c) 2003 CSC SoftwareConsult GmbH & Co. OHG, Germany and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * 	CSC - Intial implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;


/**
 * 
 * The editors command
 *
 *  @author <a href="mailto:kohlwes@gmx.net">Gregor Kohlwes</a>
 */
public class Editors extends AbstractMessageCommand {

	/**
	 * @see org.eclipse.team.internal.ccvs.core.client.Request#getRequestId()
	 */
	protected String getRequestId() {
		return "editors"; //$NON-NLS-1$
	}

}
