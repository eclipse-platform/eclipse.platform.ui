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

import java.util.ArrayList;
import java.util.List;

/**
 * TODO javadoc
 */
public abstract class AbstractActivityService implements IActivityService {

	private List activityServiceListeners;

	/**
	 * Constructs an instance of <code>AbstractActivityService</code>.
	 */
	protected AbstractActivityService() {
	}

	public void addActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();

		if (activityServiceListeners == null)
			activityServiceListeners = new ArrayList();

		if (!activityServiceListeners.contains(activityServiceListener))
			activityServiceListeners.add(activityServiceListener);
	}

	/**
	 * TODO javadoc
	 * 
	 * @param activityServiceEvent
	 */
	protected void fireActivityServiceChanged(ActivityServiceEvent activityServiceEvent) {
		if (activityServiceEvent == null)
			throw new NullPointerException();

		if (activityServiceListeners != null)
			for (int i = 0; i < activityServiceListeners.size(); i++)
				(
					(IActivityServiceListener) activityServiceListeners.get(
						i)).activityServiceChanged(
					activityServiceEvent);
	}

	public void removeActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();

		if (activityServiceListeners != null)
			activityServiceListeners.remove(activityServiceListener);
	}
}
