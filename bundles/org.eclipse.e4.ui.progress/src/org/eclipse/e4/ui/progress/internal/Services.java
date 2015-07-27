/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

public class Services {

	// TODO E4 synchronization needed ?

	@Inject
	private Display display;

	@Inject
	@Optional
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private MApplication mApplication;

	@Inject
	@Optional
	@Active
	private MWindow window;

	@Inject
	private EHandlerService eHandlerService;

	@Inject
	private IProgressService progressService;

	@Inject
	private EModelService modelService;

	@Inject
	private EPartService partService;

	@Inject
	private LogService logService;

	@Inject
	private StatusReporter statusReporter;

	@Inject
	private UISynchronize uiSynchronize;

	@Inject
	IEclipseContext localContext;

	IEclipseContext appContext;

	protected static Services instance;

	Services() {
		instance = this;
	}

	@PostConstruct
	void init() {
		appContext = mApplication.getContext();
		appContext.set(Services.class, this);
	}

	public <T> T getService(Class<T> clazz) {
		return localContext.get(clazz);
	}

	public <T> void registerService(Class<T> clazz, T value) {
		appContext.set(clazz, value);
	}

	public static Services getInstance() {
	    return instance;
    }

	public Display getDisplay() {
		return display != null ? display : getDefaultDisplay();
	}

	private Display getDefaultDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public Shell getShell() {
		return shell;
	}

	public UISynchronize getUISynchronize() {
		return uiSynchronize;
	}

	public EHandlerService getEHandlerService() {
		return eHandlerService;
	}

	public LogService getLogService() {
	    return logService;
    }

	public StatusReporter getStatusReporter() {
	    return statusReporter;
    }

	public IProgressService getProgressService() {
	    return progressService;
    }

	public EModelService getModelService() {
		return modelService;
	}

	public EPartService getPartService() {
		return partService;
	}

	public MWindow getMWindow() {
	    return window;
    }

	public MApplication getMApplication() {
		return mApplication;
	}

}
