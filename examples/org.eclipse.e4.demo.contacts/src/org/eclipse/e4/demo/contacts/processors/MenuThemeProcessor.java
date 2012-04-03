/*******************************************************************************
 * Copyright (c) 2010, 2012 BestSolution.at, Siemens AG and others.
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
import org.eclipse.e4.core.di.annotations.Execute;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.emf.ecore.EObject;

public class MenuThemeProcessor extends AbstractThemeProcessor {
	// should be bundleclass, see bug 374961
	// private static final String BUNDLE_ID =
	// "bundleclass://org.eclipse.e4.demo.contacts/org.eclipse.e4.demo.contacts.processors.MenuThemeProcessor
	private static final String BUNDLE_ID = "platform:/plugin/org.eclipse.e4.demo.contacts"; //$NON-NLS-1$
	
	@Inject
	@Named("menu:org.eclipse.ui.main.menu")
	private MMenu menu;
	private MMenu themesMenu;
	
	private final static String PROCESSOR_ID = "org.eclipse.e4.demo.contacts.processor.menu"; 

	@Execute
	public void process() {
		if (menu == null)
			return;
		
		MApplication theApp = getApplication(); 
		List<String> tags = theApp.getTags();
		for(String tag : tags) {
			if (PROCESSOR_ID.equals(tag))
				return; // already processed
		}
		if (!check())
			return;
		tags.add(PROCESSOR_ID);
		super.process();
	}

	@Override
	protected boolean check() {
		return menu != null;
	}

	@Override
	protected void preprocess() {
		themesMenu = MMenuFactory.INSTANCE.createMenu();
		themesMenu.setLabel("%switchThemeMenu"); //$NON-NLS-1$
		themesMenu.setContributorURI(BUNDLE_ID);
	}

	@Override
	protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI) {
		MHandledMenuItem menuItem = MMenuFactory.INSTANCE
				.createHandledMenuItem();
		menuItem.setLabel(name);
		menuItem.setCommand(switchCommand);
		menuItem.getParameters().add(themeId);
		menuItem.setContributorURI(BUNDLE_ID);
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
