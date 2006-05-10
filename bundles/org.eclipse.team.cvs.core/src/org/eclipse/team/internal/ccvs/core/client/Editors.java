/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CSC - Intial implementation
 *     IBM Corporation - ongoing maintenance
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.client.Command#isWorkspaceModification()
	 */
	protected boolean isWorkspaceModification() {
		return false;
	}

}
