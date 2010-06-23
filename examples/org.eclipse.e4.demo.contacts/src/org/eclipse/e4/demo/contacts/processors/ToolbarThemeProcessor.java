/*******************************************************************************
 * Copyright (c) 2010 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.processors;

import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;

import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;

import javax.inject.Inject;

import java.util.List;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ToolbarThemeProcessor extends AbstractThemeProcessor {

	@Inject
	@Named("toolbar:org.eclipse.ui.main.toolbar")
	private MToolBar toolbar;

	@Override
	protected boolean check() {
		return toolbar != null;
	}

	@Override
	protected void preprocess() {
		toolbar.getChildren().add(
				MMenuFactory.INSTANCE.createToolBarSeparator());
	}

	@Override
	protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI) {
		MHandledToolItem toolItem = MMenuFactory.INSTANCE
				.createHandledToolItem();
		toolItem.setTooltip(name);
		toolItem.setCommand(switchCommand);
		toolItem.getParameters().add(themeId);
		if (iconURI != null) {
			toolItem.setIconURI(iconURI);
		}
		toolbar.getChildren().add(toolItem);
	}

	@Override
	protected void postprocess() {
	}

	@Override
	protected MApplication getApplication() {
		return (MApplication) (((EObject) toolbar).eContainer()).eContainer().eContainer();
	}
}
