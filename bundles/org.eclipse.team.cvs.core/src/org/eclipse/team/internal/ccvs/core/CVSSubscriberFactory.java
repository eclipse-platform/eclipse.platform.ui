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
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.subscribers.TeamSubscriberFactory;
import org.eclipse.team.internal.core.SaveContext;

/**
 * CVSSubscriberFactory
 */
public class CVSSubscriberFactory extends TeamSubscriberFactory {

	final static public String ID = "org.eclipse.team.cvs.subscribers";

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncTreeSubscriberFactory#getID()
	 */
	public String getID() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncTreeSubscriberFactory#createSubscriber(org.eclipse.core.runtime.QualifiedName, org.eclipse.team.internal.core.SaveContext)
	 */
	public TeamSubscriber restoreSubscriber(QualifiedName id, SaveContext saveContext) throws TeamException {
		if(isMergeSubscriber(id)) {
			return CVSMergeSubscriber.restore(id, saveContext);
		}
		// CVS workspace subscribers are automatically recreated when the platform restarts.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncTreeSubscriberFactory#saveSubscriber(org.eclipse.team.core.subscribers.TeamSubscriber)
	 */
	public SaveContext saveSubscriber(TeamSubscriber subscriber) throws TeamException {
		if(isMergeSubscriber(subscriber.getId())) {
			return ((CVSMergeSubscriber)subscriber).saveState();
		} else {
			return null;
		}
	}
	
	private boolean isMergeSubscriber(QualifiedName id) {
		String localName = id.getLocalName();
		if(localName.startsWith(CVSMergeSubscriber.UNIQUE_ID_PREFIX)) {
			return true;
		} else {
			return false;
		}
	}
}
