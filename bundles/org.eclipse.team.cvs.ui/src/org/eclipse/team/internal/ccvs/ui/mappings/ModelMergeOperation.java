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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

public class ModelMergeOperation extends AbstractModelMergeOperation {

	private final Subscriber subscriber;

	public ModelMergeOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, Subscriber subscriber) {
		super(part, selectedMappings, SubscriberResourceMappingContext.createContext(subscriber));
		this.subscriber = subscriber;
	}

	protected IMergeContext buildMergeContext(IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask(null, 100);
		IMergeContext context = MergeSubscriberContext.createContext(getScope(), subscriber, Policy.subMonitorFor(monitor, 50));
		monitor.done();
		return context;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return CVSUIMessages.MergeUpdateAction_jobName;
	}

}
