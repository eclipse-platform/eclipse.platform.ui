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
 * @author cmclaren
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractActivityService implements IActivityService {

	/**
	 * TODO javadoc
	 */
	private List activityServiceListeners;

	/**
	 * TODO javadoc
	 */
	protected AbstractActivityService() {
	}

	/**
	 * TODO javadoc ?
	 */
	public void addActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();

		if (activityServiceListeners == null)
			activityServiceListeners = new ArrayList();

		if (!activityServiceListeners.contains(activityServiceListener))
			activityServiceListeners.add(activityServiceListener);
	}

	/**
	 * TODO javadoc ?
	 */
	public void removeActivityServiceListener(IActivityServiceListener activityServiceListener) {
		if (activityServiceListener == null)
			throw new NullPointerException();

		if (activityServiceListeners != null)
			activityServiceListeners.remove(activityServiceListener);
	}

	protected void fireActivityServiceChanged(ActivityServiceEvent activityServiceEvent) {
		if (activityServiceEvent == null)
			throw new NullPointerException();

		if (activityServiceListeners != null)
			for (int i = 0; i < activityServiceListeners.size(); i++)
				 ((IActivityServiceListener) activityServiceListeners.get(i)).activityServiceChanged(activityServiceEvent);
	}
}
