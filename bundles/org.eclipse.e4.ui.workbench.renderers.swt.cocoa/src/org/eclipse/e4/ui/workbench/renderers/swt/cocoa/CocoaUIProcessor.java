/*******************************************************************************
 * Copyright (c) 2010 Brian de Alwis, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;

/**
 * A hack to create an IStartup-like equivalent using ModelProcessor. The actual
 * handling is done by {@link CocoaUIHandler}. But as the context provided to a
 * {@code ModelProcessor} is destroyed after the processor has executed, we
 * create a new context.
 */
public class CocoaUIProcessor {
	@Inject
	protected MApplication app;

	/**
	 * Install the addon.
	 */
	@Execute
	public void execute() {
		String addonId = CocoaUIHandler.class.getName();
		for (MAddon addon : app.getAddons()) {
			if (addonId.equals(addon.getElementId())) {
				return;
			}
		}

		MAddon addon = MApplicationFactory.INSTANCE.createAddon();
		addon.setContributionURI(CocoaUIHandler.CLASS_URI);
		addon.setElementId(addonId);
		app.getAddons().add(addon);
	}

}
