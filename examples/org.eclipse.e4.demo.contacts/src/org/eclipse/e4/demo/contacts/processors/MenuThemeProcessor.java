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

public class MenuThemeProcessor extends AbstractThemeProcessor {

	@Inject
	@Named("menu:org.eclipse.ui.main.menu")
	private MMenu menu;
	private MMenu themesMenu;

	@Override
	protected boolean check() {
		return menu != null;
	}

	@Override
	protected void preprocess() {
		themesMenu = MMenuFactory.INSTANCE.createMenu();
		themesMenu.setLabel("Theme"); //$NON-NLS-1$
	}

	@Override
	protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI) {
		MHandledMenuItem menuItem = MMenuFactory.INSTANCE
				.createHandledMenuItem();
		menuItem.setLabel(name);
		menuItem.setCommand(switchCommand);
		menuItem.getParameters().add(themeId);
		if (iconURI != null) {
			menuItem.setIconURI(iconURI);
		}
		themesMenu.getChildren().add(menuItem);

	}

	@Override
	protected void postprocess() {
		menu.getChildren().add(themesMenu);
	}

	@Override
	protected MApplication getApplication() {
		return (MApplication) (((EObject) menu).eContainer()).eContainer();
	}
}
