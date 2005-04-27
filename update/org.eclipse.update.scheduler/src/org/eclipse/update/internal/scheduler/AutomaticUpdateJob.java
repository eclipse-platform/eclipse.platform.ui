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
package org.eclipse.update.internal.scheduler;

import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.ui.UpdateJob;

class AutomaticUpdateJob extends UpdateJob {

	public AutomaticUpdateJob(String name, boolean isAutomatic, boolean download) {
		super(name, isAutomatic, download);
	}

	public AutomaticUpdateJob(String name, UpdateSearchRequest searchRequest) {
		super(name, searchRequest);
	}

	public boolean belongsTo(Object family) {
		return SchedulerStartup.automaticJobFamily == family;
	}
}