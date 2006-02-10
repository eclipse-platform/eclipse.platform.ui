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
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.IWorkbenchPart;

public class ModelCompareOperation extends AbstractModelMergeOperation {

	private final CVSCompareSubscriber subscriber;

	public ModelCompareOperation(IWorkbenchPart part, ResourceMapping[] mappings, final CVSCompareSubscriber subscriber) {
		super(part, new SubscriberScopeManager(mappings, subscriber, true){
			public void dispose() {
				subscriber.dispose();
				super.dispose();
			}
		}, true);
		this.subscriber = subscriber;
	}

	protected SynchronizationContext createMergeContext() {
		return CompareSubscriberContext.createContext(getScopeManager(), subscriber);
	}

	public boolean isPreviewRequested() {
		return true;
	}
	
	protected boolean isPreviewInDialog() {
		return false;
	}
	
	protected ModelSynchronizeParticipant createParticipant() {
		return new ModelCompareParticipant(createMergeContext());
	}
	
	protected String getJobName() {
		return "CVS Compare";
	}
}
