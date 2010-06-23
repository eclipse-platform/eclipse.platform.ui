/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at, Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Kai Tödter - Adoption to contacts demo
 ******************************************************************************/
package org.eclipse.e4.demo.contacts.processors;

import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public abstract class AbstractThemeProcessor {

	@Execute
	public void process() {
		if (!check())
			return;

		// FIXME Remove once bug 314091 is resolved
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		BundleContext context = bundle.getBundleContext();

		ServiceReference reference = context
				.getServiceReference(IThemeManager.class.getName());
		IThemeManager mgr = (IThemeManager) context.getService(reference);
		IThemeEngine engine = mgr.getEngineForDisplay(Display.getCurrent());

		List<ITheme> themes = engine.getThemes();
		if (themes.size() > 0) {
			MApplication application = getApplication();
			
			MCommand switchThemeCommand = null;
			for (MCommand cmd : application.getCommands()) {
				if ("contacts.switchTheme".equals(cmd.getElementId())) { //$NON-NLS-1$
					switchThemeCommand = cmd;
					break;
				}
			}

			if (switchThemeCommand != null) {

				preprocess();

				for (ITheme theme : themes) {
					MParameter parameter = MCommandsFactory.INSTANCE
							.createParameter();
					parameter.setName("contacts.commands.switchtheme.themeid"); //$NON-NLS-1$
					parameter.setValue(theme.getId());
					String iconURI = getCSSUri(theme.getId());
					if (iconURI != null) {
						iconURI = iconURI.replace(".css", ".png");
					}
					processTheme(theme.getLabel(), switchThemeCommand, parameter,
							iconURI);
				}

				postprocess();
			}
		}
	}

	abstract protected boolean check();

	abstract protected void preprocess();

	abstract protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI);

	abstract protected void postprocess();
	
	abstract protected MApplication getApplication(); 

	private String getCSSUri(String themeId) {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry
				.getExtensionPoint("org.eclipse.e4.ui.css.swt.theme");

		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals("theme")
						&& ce.getAttribute("id").equals(themeId)) {
					return "platform:/plugin/" + ce.getContributor().getName()
							+ "/" + ce.getAttribute("basestylesheeturi");
				}
			}
		}
		return null;
	}
}
