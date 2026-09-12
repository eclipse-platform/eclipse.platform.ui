/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.themes.DefaultThemePreference;

/**
 * Lists the installed themes and applies the one the user picks, remembering it
 * for the next start. A last entry records the active theme as the default for
 * new workspaces.
 */
public class ThemeContributionItem extends CompoundContributionItem {

	public ThemeContributionItem() {
	}

	public ThemeContributionItem(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		IThemeEngine engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
		// High contrast mode pins the engine to the high contrast theme, so offer no choice.
		if (engine == null || PlatformUI.getWorkbench().getDisplay().getHighContrast()) {
			return new IContributionItem[0];
		}
		List<ITheme> themes = new ArrayList<>(engine.getThemes());
		themes.sort(Comparator.comparing(ITheme::getLabel, String.CASE_INSENSITIVE_ORDER));

		List<IContributionItem> items = new ArrayList<>();
		themes.forEach(theme -> items.add(createItem(engine, theme)));
		if (engine.getActiveTheme() != null) {
			items.add(new Separator());
			items.add(createSetAsDefaultItem(engine));
		}
		return items.toArray(IContributionItem[]::new);
	}

	/** Records the active theme as the default for new workspaces. */
	private static IContributionItem createSetAsDefaultItem(IThemeEngine engine) {
		return new ContributionItem() {
			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.PUSH, index);
				item.setText(WorkbenchMessages.ThemeDefault_setDefault);
				item.addListener(SWT.Selection, event -> {
					ITheme activeTheme = engine.getActiveTheme();
					if (activeTheme != null) {
						DefaultThemePreference.set(activeTheme);
					}
				});
			}
		};
	}

	private static IContributionItem createItem(IThemeEngine engine, ITheme theme) {
		return new ContributionItem() {
			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.RADIO, index);
				item.setText(theme.getLabel());
				ITheme activeTheme = engine.getActiveTheme();
				item.setSelection(activeTheme != null && activeTheme.getId().equals(theme.getId()));
				item.addListener(SWT.Selection, event -> {
					if (!((MenuItem) event.widget).getSelection()) {
						return;
					}
					ITheme previousTheme = engine.getActiveTheme();
					// Clicking the checked entry fires a selection too, reapplying it would
					// restyle the whole workbench for nothing.
					if (previousTheme != null && previousTheme.getId().equals(theme.getId())) {
						return;
					}
					engine.setTheme(theme, true);
					// Only a light/dark switch leaves parts styled for the previous appearance.
					if (previousTheme != null && previousTheme.isDark() != theme.isDark()) {
						offerRestart();
					}
				});
			}
		};
	}

	private static void offerRestart() {
		MessageDialog dialog = new MessageDialog(null, WorkbenchMessages.ThemeChangeWarningTitle, null,
				WorkbenchMessages.ThemeChangeWarningText, MessageDialog.NONE, 1,
				WorkbenchMessages.Workbench_RestartButton, WorkbenchMessages.Workbench_DontRestartButton);
		if (dialog.open() == 0) {
			Display.getDefault().asyncExec(() -> PlatformUI.getWorkbench().restart());
		}
	}
}
