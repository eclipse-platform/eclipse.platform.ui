/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.IColorFactory;

/**
 * Color factory for viewer selection colors that adapts to the OS/desktop
 * theme. Provides default colors based on system colors for focused and
 * unfocused selections.
 * <p>
 * The default colors are based on system title bar colors which automatically
 * adapt to light/dark themes and high contrast modes. Themes can override these
 * defaults to provide custom styling.
 * </p>
 *
 * @since 3.39
 */
public class ColumnViewerSelectionColorFactory implements IColorFactory, IExecutableExtension {

	private String color = null;

	@Override
	public RGB createColor() {
		return Display.getDefault().getSystemColor(idToColor(color)).getRGB();
	}

	private int idToColor(String id) {
		return switch (id) {
		case "SELECTED_CELL_BACKGROUND" -> SWT.COLOR_TITLE_BACKGROUND; //$NON-NLS-1$
		case "SELECTED_CELL_FOREGROUND" -> SWT.COLOR_WHITE; //$NON-NLS-1$
		case "SELECTED_CELL_BACKGROUND_NO_FOCUS" -> SWT.COLOR_TITLE_INACTIVE_BACKGROUND; //$NON-NLS-1$
		case "SELECTED_CELL_FOREGROUND_NO_FOCUS" -> SWT.COLOR_TITLE_INACTIVE_FOREGROUND; //$NON-NLS-1$
		default -> SWT.COLOR_BLACK;
		};
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable table) {
			this.color = (String) table.get("color"); //$NON-NLS-1$
		}
	}
}
