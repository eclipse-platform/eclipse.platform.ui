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
import javax.annotation.PreDestroy;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.jface.menus.AbstractTrimWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.IWorkbenchWidget;

/**
 * @since 3.5
 *
 */
public class LegacyTrim {

	private IWorkbenchWidget widget;

	@PostConstruct
	void createWidget(IWorkbenchWindow iwbw, Composite parent, MToolControl toolControl) {
		IConfigurationElement ice = ((WorkbenchWindow) iwbw).getICEFor(toolControl);
		if (ice == null)
			return;

		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new RowLayout());

		widget = (IWorkbenchWidget) Util.safeLoadExecutableExtension(ice,
				IWorkbenchRegistryConstants.ATT_CLASS, IWorkbenchWidget.class);
		widget.init(iwbw);
		if (widget instanceof AbstractTrimWidget) {
			((AbstractTrimWidget) widget).fill(parent, SWT.DEFAULT, getSide(toolControl));
		} else {
			widget.fill(parent);
		}
	}

	@PreDestroy
	void destroy() {
		if (widget != null) {
			widget.dispose();
			widget = null;
		}
	}

	private int getSide(MUIElement element) {
		MUIElement parent = element.getParent();
		if (parent instanceof MTrimBar) {
			switch (((MTrimBar) parent).getSide()) {
			case BOTTOM:
				return SWT.BOTTOM;
			case LEFT:
				return SWT.LEFT;
			case RIGHT:
				return SWT.RIGHT;
			case TOP:
				return SWT.TOP;
			}
		}
		return parent == null ? SWT.DEFAULT : getSide(parent);
	}
}
