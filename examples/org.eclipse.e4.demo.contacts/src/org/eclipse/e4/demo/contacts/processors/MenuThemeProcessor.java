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
 *     Lars Vogel <lars.vogel@gmail.com> - Bug 413431, 416166
 ******************************************************************************/
package org.eclipse.e4.demo.contacts.processors;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.demo.contacts.util.ThemeHelper;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Display;

public class MenuThemeProcessor {
	private static final String BUNDLE_ID = "platform:/plugin/org.eclipse.e4.demo.contacts"; //$NON-NLS-1$

	@Inject
	@Named("menu:org.eclipse.ui.main.menu")
	private MMenu menu;
	private MMenu themesMenu;

	private final static String PROCESSOR_ID = "org.eclipse.e4.demo.contacts.processor.menu";

	@SuppressWarnings("restriction")
	@Execute
	public void execute(MApplication app, EModelService service,
			IExtensionRegistry registery, IThemeManager mgr) {

		// sanity check
		if (menu == null) {
			return;
		}

		List<String> tags = app.getTags();
		for (String tag : tags) {
			if (PROCESSOR_ID.equals(tag)) {
				return; // already processed
			}
		}

		tags.add(PROCESSOR_ID);

		IThemeEngine engine = mgr.getEngineForDisplay(Display.getCurrent());

		List<ITheme> themes = engine.getThemes();

		MCommand switchThemeCommand = ThemeHelper.findCommand(app);

		// no themes or command, stop processing
		if (themes.size() <= 0 || switchThemeCommand == null) {
			return;
		}

		themesMenu = service.createModelElement(MMenu.class);
		themesMenu.setLabel("%switchThemeMenu"); //$NON-NLS-1$
		themesMenu.setContributorURI(BUNDLE_ID);

		for (ITheme theme : themes) {
			if (!theme.getId().startsWith("org.eclipse.e4.demo.contacts.")) {
				return;
			}
			MParameter parameter = service.createModelElement(MParameter.class);
			parameter.setName("contacts.commands.switchtheme.themeid"); //$NON-NLS-1$
			parameter.setValue(theme.getId());
			String iconURI = ThemeHelper.getCSSUri(theme.getId(), registery);
			if (iconURI != null) {
				iconURI = iconURI.replace(".css", ".png");
			}
			processTheme(theme.getLabel(), switchThemeCommand, parameter,
					iconURI, service);
		}
		menu.getChildren().add(themesMenu);
	}

	protected void processTheme(String name, MCommand switchCommand,
			MParameter themeId, String iconURI, EModelService service) {
		MHandledMenuItem menuItem = service
				.createModelElement(MHandledMenuItem.class);
		menuItem.setLabel(name);
		menuItem.setCommand(switchCommand);
		menuItem.getParameters().add(themeId);
		menuItem.setContributorURI(BUNDLE_ID);
		if (iconURI != null) {
			menuItem.setIconURI(iconURI);
		}
		themesMenu.getChildren().add(menuItem);

	}

}
