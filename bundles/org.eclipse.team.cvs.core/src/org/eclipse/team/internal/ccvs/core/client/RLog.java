/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;

import org.eclipse.team.internal.ccvs.core.CVSTag;

/**
 * The "cvs rlog..." command
 */
public class RLog extends RemoteCommand {
	
	/*** Local options: specific to rlog ***/
	public static final LocalOption NO_TAGS = new LocalOption("-N", null); //$NON-NLS-1$
	
	/**
	 * Makes a -r option for rlog.
	 * Valid for: rlog
	 */
	public static LocalOption makeTagOption(CVSTag tag1, CVSTag tag2) {
		int type = tag1.getType();
		switch (type) {
			case CVSTag.BRANCH:
			case CVSTag.VERSION:
			case CVSTag.HEAD:
				return new LocalOption("-r" + tag1.getName() + ":" + tag2.getName(), null); //$NON-NLS-1$ //$NON-NLS-2$
			case CVSTag.DATE:
				return new LocalOption("-d'" + tag1.getName() + "':'" + tag2.getName() + "'", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			default:
				// Unknow tag type!!!
				throw new IllegalArgumentException();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.client.Request#getRequestId()
	 */
	protected String getRequestId() {
		return "rlog"; //$NON-NLS-1$
	}
}
