/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429421
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Model processors which adds the Splitter add-on to the application model
 */

public class SplitterProcessor {
	@Execute
	void addSplitterAddon(MApplication app, EModelService modelService) {
		List<MAddon> addons = app.getAddons();

		// prevent multiple copies
		for (MAddon addon : addons) {
			if (addon.getContributionURI().contains(
					"ui.workbench.addons.splitteraddon.SplitterAddon")) {
				return;
			}
		}

		// adds the add-on to the application model
		MAddon splitterAddon = modelService.createModelElement(MAddon.class);
		splitterAddon.setElementId("SplitterAddon"); //$NON-NLS-1$
		splitterAddon
				.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.splitteraddon.SplitterAddon"); //$NON-NLS-1$
		app.getAddons().add(splitterAddon);
	}
}
