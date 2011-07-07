/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IWorkbenchWidget;

/**
 * @since 3.5
 *
 */
public class LegacyTrim {
	@Inject
	EModelService modelService;

	@Inject
	IWorkbenchWindow iwbw;

	@PostConstruct
	void createWidget(Composite parent, MToolControl toolControl) {
		IConfigurationElement ice = ((WorkbenchWindow) iwbw).getICEFor(toolControl);
		if (ice == null)
			return;

		IWorkbenchWidget widget;
		try {
			widget = (IWorkbenchWidget) ice.createExecutableExtension("class"); //$NON-NLS-1$
			widget.fill(parent);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
