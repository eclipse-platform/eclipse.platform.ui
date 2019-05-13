/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class ContentProviderFactory {

	@Inject
	Services services;

	@PostConstruct
	void init() {
		services.registerService(ContentProviderFactory.class, this);
	}

	public ProgressViewerContentProvider getProgressViewerContentProvider(
			AbstractProgressViewer structured, boolean debug,
			boolean showFinished) {

		//TODO E4 workaround for @Creatable problem
		return new ProgressViewerContentProvider(structured,
				services.getService(FinishedJobs.class),
				services.getService(ProgressViewUpdater.class),
				services.getService(ProgressManager.class), debug, showFinished);
	}

}
