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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * Superclass for all CVS particpants (workspace, merge and compare)
 */
public class CVSParticipant extends SubscriberParticipant {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		// The decorator adds itself to the configuration
		ILabelDecorator labelDecorator = new CVSParticipantLabelDecorator(configuration);
		configuration.addLabelDecorator(labelDecorator);
	}
}
