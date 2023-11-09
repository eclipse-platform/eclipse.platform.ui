/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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

package org.eclipse.e4.ui.progress;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.progress.internal.PreferenceStore;
import org.eclipse.e4.ui.progress.internal.Preferences;
import org.eclipse.e4.ui.progress.internal.ProgressManager;
import org.eclipse.e4.ui.progress.internal.ProgressViewUpdater;
import org.eclipse.e4.ui.progress.internal.Services;

import jakarta.annotation.PostConstruct;

public class ProgressViewAddon {

	@PostConstruct
	public void init(MApplication application, IEclipseContext context) {
		IEclipseContext appContext = application.getContext();
		appContext.set(Preferences.class, ContextInjectionFactory.make(Preferences.class, appContext));
		appContext.set(PreferenceStore.class, ContextInjectionFactory.make(PreferenceStore.class, appContext));
		ContextInjectionFactory.make(Services.class, context);
		ProgressManager progressManager = ContextInjectionFactory.make(ProgressManager.class, context);
		appContext.set(ProgressManager.class, progressManager);
		// bug-fix: add a ProgressViewUpdater to the application context
		appContext.set(ProgressViewUpdater.class, ContextInjectionFactory.make(ProgressViewUpdater.class, appContext));
	}
}
