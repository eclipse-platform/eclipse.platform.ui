/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Kai Tödter - Adoption to contacts demo
 ******************************************************************************/
package org.eclipse.e4.demo.contacts.handlers;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;

public class SwitchThemeHandler {
	@Execute
	public void switchTheme(
			@Named("contacts.commands.switchtheme.themeid") String themeId,
			IThemeEngine engine) {
		engine.setTheme(themeId, true);
	}
}