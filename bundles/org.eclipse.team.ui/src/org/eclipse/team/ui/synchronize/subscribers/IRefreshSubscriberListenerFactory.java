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
package org.eclipse.team.ui.synchronize.subscribers;

import org.eclipse.team.core.synchronize.SyncInfoTree;

/**
 * A factory which provides standard listeners to subscriber refresh operations. These
 * can be used to add common behavior to refresh operations run via a
 * {@link SubscriberParticipant}.
 *
 * @since 3.0
 */
public interface IRefreshSubscriberListenerFactory {
	/**
	 * Returns a listener that will prompt with the resuts of the refresh in a dialog. You
	 * can configure the set of synchronization information that will be shown by specifying
	 * a {@link SyncInfoSet} that is different than the default one maintained by
	 * the given participant.
	 */
	public IRefreshSubscriberListener createModalDialogListener(String targetId, SubscriberParticipant participant, SyncInfoTree syncInfoSet);
	
	/**
	 * Returns a listener that will prompt at the end of the refresh indicating if changes are
	 * found and indicate 
	 */
	public IRefreshSubscriberListener createSynchronizeViewListener(SubscriberParticipant participant);
}
