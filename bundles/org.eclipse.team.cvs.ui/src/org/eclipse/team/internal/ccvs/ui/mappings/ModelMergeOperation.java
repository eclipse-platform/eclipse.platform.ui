/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.ui.IWorkbenchPart;

public class ModelMergeOperation extends AbstractModelMergeOperation {

	private final Subscriber subscriber;
	
	/**
	 * Create a merge operation for the given subscriber. The merge operation will cancel the subscriber
	 * when it is no longer needed.
	 * @param part the part
	 * @param mappings the mappings
	 * @param subscriber the subscriber
	 */
	public ModelMergeOperation(IWorkbenchPart part, ResourceMapping[] mappings, final CVSMergeSubscriber subscriber) {
		super(part, new SubscriberScopeManager(mappings, subscriber, true){
			public void dispose() {
				subscriber.cancel();
				super.dispose();
			}
		}, true);
		this.subscriber = subscriber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelParticipantMergeOperation#createMergeContext()
	 */
	protected SynchronizationContext createMergeContext() {
		return MergeSubscriberContext.createContext(getScopeManager(), subscriber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return CVSUIMessages.MergeUpdateAction_jobName;
	}

}
