/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.ui;

import static org.eclipse.e4.demo.cssbridge.ui.ICommandIds.CSSThemeSwitchCommands.SWITCH_TO_BLUE_CSS_THEME;
import static org.eclipse.e4.demo.cssbridge.ui.ICommandIds.CSSThemeSwitchCommands.SWITCH_TO_EMPTY_CSS_THEME;
import static org.eclipse.e4.demo.cssbridge.ui.ICommandIds.CSSThemeSwitchCommands.SWITCH_TO_GREEN_CSS_THEME;
import static org.eclipse.e4.demo.cssbridge.ui.ICommandIds.CSSThemeSwitchCommands.SWITCH_TO_RED_CSS_THEME;

import org.eclipse.e4.demo.cssbridge.ui.actions.CssThemeSwitchAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private Action[] switchToThemeActions;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		switchToThemeActions = new Action[4];

		switchToThemeActions[0] = new CssThemeSwitchAction(
				SWITCH_TO_EMPTY_CSS_THEME,
				"Switch to the 'Empty' CSS theme where the CSS styling is disabled",
				"/icons/empty_theme.png", CSSTheme.EMPTY_THEME_ID, window);
		register(switchToThemeActions[0]);

		switchToThemeActions[1] = new CssThemeSwitchAction(
				SWITCH_TO_BLUE_CSS_THEME, "Switch to the 'Blue' CSS theme",
				"/icons/blue_theme.png", CSSTheme.BLUE_THEME_ID, window);
		register(switchToThemeActions[1]);

		switchToThemeActions[2] = new CssThemeSwitchAction(
				SWITCH_TO_GREEN_CSS_THEME, "Switch to the 'Green' CSS theme",
				"/icons/green_theme.png", CSSTheme.GREEN_THEME_ID, window);
		register(switchToThemeActions[2]);

		switchToThemeActions[3] = new CssThemeSwitchAction(
				SWITCH_TO_RED_CSS_THEME, "Switch to the 'Red' CSS theme",
				"/icons/red_theme.png", CSSTheme.RED_THEME_ID, window);
		register(switchToThemeActions[3]);

	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		for (Action action : switchToThemeActions) {
			toolbar.add(action);
		}
	}
}
