/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom.scrollbar;

import org.eclipse.swt.graphics.Color;

public interface IScrollBarSettings {

	public void setScrollBarThemed(boolean themed);

	public boolean getScrollBarThemed();

	public Color getForegroundColor();

	public Color getBackgroundColor();

	public void setForegroundColor(Color color);

	public void setBackgroundColor(Color color);

	public void setScrollBarWidth(int width);

	public int getScrollBarWidth();

	public void setMouseNearScrollScrollBarWidth(int width);

	public int getMouseNearScrollScrollBarWidth();

	public void setScrollBarBorderRadius(int radius);

	public int getScrollBarBorderRadius();
}
