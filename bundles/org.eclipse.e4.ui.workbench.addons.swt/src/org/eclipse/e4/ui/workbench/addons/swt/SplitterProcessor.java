/*******************************************************************************
 * Copyright (c) 2013, 2024 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429421
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.IModelProcessorContribution;
import org.osgi.service.component.annotations.Component;

/**
 * Model processors which adds the Splitter add-on to the application model
 */
@Component
public class SplitterProcessor implements IModelProcessorContribution {
	@Execute
	void addSplitterAddon(MApplication app, EModelService modelService) {
		List<MAddon> addons = app.getAddons();

		// prevent multiple copies
		for (MAddon addon : addons) {
			if (addon.getContributionURI().contains(
					"ui.workbench.addons.splitteraddon.SplitterAddon")) { //$NON-NLS-1$
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
