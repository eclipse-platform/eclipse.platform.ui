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
package org.eclipse.team.ui.synchronize;

import org.eclipse.jface.viewers.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;

/**
 * A label decorator that adds itself to a synchronize page configuration
 * and provides subclasses with a means of refeshing the labels of the
 * elements in the page (for cases where the label configuration has changed, for
 * instance). Clients are not required to subclass this class in order to
 * add a decorator to a synchronize page configuration. This class is provided
 * as a convenience and for those cases that require the ability to refresh the 
 * labels.
 * @since 3.0
 */
public abstract class AbstractSynchronizeLabelDecorator extends LabelProvider implements ILabelDecorator {
	
	private ISynchronizePageConfiguration configuration;
	
	/**
	 * Create a decorator and add it to the configuration
	 * @param configuration a synchronize page configuration
	 */
	public AbstractSynchronizeLabelDecorator(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		configuration.addLabelDecorator(this);
	}
	
	/**
	 * Refresh all the labels of the elements being displayed by the
	 * page.
	 */
	protected void refreshLabels() {
		StructuredViewerAdvisor advisor 
			= (StructuredViewerAdvisor)configuration
				.getProperty(SynchronizePageConfiguration.P_ADVISOR);
		if (advisor == null) return;
		StructuredViewer viewer = advisor.getViewer();
		if (viewer == null) return;
		viewer.refresh(true /* update labels */);
	}
	
	/**
	 * Returns the configuration of this label decorator
	 * @return Returns the configuration.
	 */
	public ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}
}
