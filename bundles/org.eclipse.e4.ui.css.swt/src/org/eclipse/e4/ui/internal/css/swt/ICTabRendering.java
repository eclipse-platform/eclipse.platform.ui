/*******************************************************************************
 *  Copyright (c) 2014-2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.eclipse.swt.graphics.Color;

public interface ICTabRendering {
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
}
