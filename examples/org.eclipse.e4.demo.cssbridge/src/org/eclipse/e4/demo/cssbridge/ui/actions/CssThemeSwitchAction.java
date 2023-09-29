/*******************************************************************************
 * Copyright (c) 2015, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.ui.actions;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.framework.FrameworkUtil;

@SuppressWarnings("restriction")
public class CssThemeSwitchAction extends Action {
	private final String themeId;

	private final IThemeEngine themeEngine;

	private final IWorkbenchWindow window;

	public CssThemeSwitchAction(String actionId, String text, String iconPath,
			String themeId, IWorkbenchWindow window) {
		super(text);
		setId(actionId);
		setActionDefinitionId(actionId);
		String bundleSymbolicName = FrameworkUtil.getBundle(CssThemeSwitchAction.class).getSymbolicName();
		setImageDescriptor(ResourceLocator.imageDescriptorFromBundle(bundleSymbolicName, iconPath).orElse(null));

		this.window = window;
		this.themeId = themeId;
		themeEngine = window.getService(IThemeEngine.class);
	}

	@Override
	public void run() {
		if (themeEngine == null) {
			MessageDialog.openWarning(window.getShell(), "Warnings",
					"The CSS ThemeEngine is not registered");
			return;
		}

		ITheme theme = themeEngine.getActiveTheme();
		if (theme != null && !theme.getId().equals(themeId)) {
			themeEngine.setTheme(themeId, false);
		}
	}
}
