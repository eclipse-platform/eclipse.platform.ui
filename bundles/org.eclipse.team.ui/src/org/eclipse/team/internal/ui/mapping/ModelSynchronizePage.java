/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * A synchronize page for displaying a {@link ResourceMappingSynchronizeParticipant}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 **/
public class ModelSynchronizePage extends AbstractSynchronizePage {

	private ResourceMappingSynchronizeParticipant participant;

	/**
	 * Create a page from the given configuration
	 * @param configuration a page configuration
	 */
	public ModelSynchronizePage(ISynchronizePageConfiguration configuration) {
		super(configuration);
		this.participant = (ResourceMappingSynchronizeParticipant)configuration.getParticipant();
		configuration.setComparisonType(isThreeWay() 
						? ISynchronizePageConfiguration.THREE_WAY 
						: ISynchronizePageConfiguration.TWO_WAY);
		// TODO: This is a hack to get something working
		//configuration.setProperty(ISynchronizePageConfiguration.P_SYNC_INFO_SET, new SyncInfoTree());
		//configuration.setProperty(SynchronizePageConfiguration.P_WORKING_SET_SYNC_INFO_SET, new SyncInfoTree());
	}

	private boolean isThreeWay() {
		return getParticipant().getContext().getType() == ISynchronizationContext.THREE_WAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#updateMode(int)
	 */
	protected void updateMode(int mode) {
		if (isThreeWay()) {
			CommonViewerAdvisor advisor = (CommonViewerAdvisor)getConfiguration().getProperty(SynchronizePageConfiguration.P_ADVISOR);
			if (advisor != null)
				advisor.setExtentionProperty(ISynchronizePageConfiguration.P_MODE, mode);
		}
	}

	/**
	 * Return the participant of this page.
	 * @return the participant of this page
	 */
	protected ResourceMappingSynchronizeParticipant getParticipant() {
		return participant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#createViewerAdvisor(org.eclipse.swt.widgets.Composite)
	 */
	protected AbstractViewerAdvisor createViewerAdvisor(Composite parent) {
		CommonViewerAdvisor commonViewerAdvisor = new CommonViewerAdvisor(parent, getConfiguration());
		updateMode(getConfiguration().getMode());
		return commonViewerAdvisor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#createChangesSection()
	 */
	protected ChangesSection createChangesSection(Composite parent) {
		return new ChangesSection(parent, this, getConfiguration());
	}

}
