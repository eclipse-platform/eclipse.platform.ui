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
		Display display = Display.getDefault();

		if ("SELECTED_CELL_BACKGROUND".equals(color)) { //$NON-NLS-1$
			return display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB();

		} else if ("SELECTED_CELL_FOREGROUND".equals(color)) { //$NON-NLS-1$
			return display.getSystemColor(SWT.COLOR_WHITE).getRGB();

		} else if ("SELECTED_CELL_BACKGROUND_NO_FOCUS".equals(color)) { //$NON-NLS-1$
			return display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND).getRGB();

		} else if ("SELECTED_CELL_FOREGROUND_NO_FOCUS".equals(color)) { //$NON-NLS-1$
			return display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND).getRGB();

		} else {
			return new RGB(0, 0, 0);
		}
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable table) {
			this.color = (String) table.get("color"); //$NON-NLS-1$
		}
	}
}
