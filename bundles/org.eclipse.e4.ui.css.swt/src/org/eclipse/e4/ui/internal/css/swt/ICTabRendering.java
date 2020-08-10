/*******************************************************************************
 *  Copyright (c) 2014, 2020 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Fabio Zadrozny - Bug 465711
 *      Simon Scholz <simon.scholz@vogella.com> - Bug 497586
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.eclipse.swt.graphics.Color;

public interface ICTabRendering {
	void setSelectedTabHighlightTop(boolean drawTabHiglightOnTop);

	void setSelectedTabHighlight(Color color);

	void setSelectedTabFill(Color color);

	void setSelectedTabFill(Color[] colors, int[] percents);

	void setUnselectedTabsColor(Color color);

	void setUnselectedTabsColor(Color[] colors, int[] percents);

	void setUnselectedHotTabsColorBackground(Color color);

	void setTabOutline(Color color);

	void setInnerKeyline(Color color);

	void setOuterKeyline(Color color);

	void setShadowColor(Color color);

	void setCornerRadius(int radius);

	void setShadowVisible(boolean visible);

	/**
	 * Sets whether to use a custom tab background (reusing tab colors and
	 * gradients), or default one from plain CTabFolder (using widget background
	 * color).
	 *
	 * @param drawCustomTabContentBackground
	 */
	void setDrawCustomTabContentBackground(boolean drawCustomTabContentBackground);
}
