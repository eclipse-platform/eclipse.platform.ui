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

package org.eclipse.ui.activities;

/**
 * <p>
 * An instance of <code>ActivityServiceEvent</code> describes changes to an
 * instance of <code>IActivityService</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityService
 * @see IActivityServiceListener#activityServiceChanged
 */
public final class ActivityServiceEvent {

	private boolean activeActivityIdsChanged;
	private IActivityService activityService;

	/**
	 * TODO javadoc
	 * 
	 * @param activityService
	 * @param activeActivityIdsChanged
	 */
	public ActivityServiceEvent(IActivityService activityService, boolean activeActivityIdsChanged) {
		if (activityService == null)
			throw new NullPointerException();

		this.activeActivityIdsChanged = activeActivityIdsChanged;
		this.activityService = activityService;
	}

	/**
	 * Returns the instance of <code>IActivityService</code> that has
	 * changed.
	 * 
	 * @return the instance of <code>IActivityService</code> that has
	 *         changed. Guaranteed not to be <code>null</code>.
	 */
	public IActivityService getActivityService() {
		return activityService;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActiveActivityIdsChanged() {
		return activeActivityIdsChanged;
	}
}
