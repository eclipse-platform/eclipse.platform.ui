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
package org.eclipse.team.ui.mapping;

import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.TeamOperation;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This operation class can be used by model providers when performing
 * merge operations triggered from a synchronize participant page
 * associated with a synchronization or merge context.
 * <p>
 * This class may be subclasses by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see ISynchronizationContext
 * @see IMergeContext
 * 
 * @since 3.2
 */
public abstract class SynchronizationOperation extends TeamOperation {

	private final ISynchronizePageConfiguration configuration;

	/*
	 * Helper method for extracting the part safely from a configuration
	 */
	private static IWorkbenchPart getPart(ISynchronizePageConfiguration configuration) {
		if (configuration != null) {
			ISynchronizePageSite site = configuration.getSite();
			if (site != null) {
				return site.getPart();
			}
		}
		return null;
	}
	
	protected SynchronizationOperation(ISynchronizePageConfiguration configuration) {
		super(getPart(configuration));
		this.configuration = configuration;
	}

	public ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Return the synchronization context associated with this action.
	 * @return the synchronization context associated with this action
	 */
	protected ISynchronizationContext getContext() {
		return ((ResourceMappingSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
	}
	
	/**
	 * Make <code>shouldRun</code> public so the result
	 * can be used to provide handler enablement
	 */
	public boolean shouldRun() {
		return super.shouldRun();
	}


}
