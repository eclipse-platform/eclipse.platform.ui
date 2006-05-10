/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * Action in compare editor that reverts the local contents to match the contents on the server.
 */
public class CompareRevertAction extends CVSParticipantAction {

	public CompareRevertAction(ISynchronizePageConfiguration configuration) {
		super(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.SubscriberAction#getSubscriberOperation(org.eclipse.compare.structuremergeviewer.IDiffElement[])
	 */
	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return new CompareRevertOperation(configuration, elements);
	}

}
