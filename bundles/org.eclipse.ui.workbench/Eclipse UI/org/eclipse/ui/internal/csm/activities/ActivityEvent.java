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

package org.eclipse.ui.internal.csm.activities;

import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityEvent;

final class ActivityEvent implements IActivityEvent {

	private IActivity activity;

	ActivityEvent(IActivity activity) {
		if (activity == null)
			throw new NullPointerException();
		
		this.activity = activity;
	}

	public IActivity getActivity() {
		return activity;
	}
}
