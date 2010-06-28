/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public static ApplicationWorkbenchAdvisor INSTANCE;

	public ApplicationWorkbenchAdvisor() {
		super();
		INSTANCE = this;
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return Perspective.ID;
	}

	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		Bundle b = FrameworkUtil.getBundle(getClass());
		BundleContext context = b.getBundleContext();
		ServiceReference serviceRef = context
				.getServiceReference(IThemeManager.class.getName());
		IThemeManager themeManager = (IThemeManager) context
				.getService(serviceRef);

		final IThemeEngine engine = themeManager.getEngineForDisplay(Display
				.getCurrent());
		engine.setTheme("org.eclipse.e4.ui.examples.css.rcp", true);
		if (serviceRef != null) {
			serviceRef = null;
		}
		if (themeManager != null) {
			themeManager = null;
		}
	}

}
