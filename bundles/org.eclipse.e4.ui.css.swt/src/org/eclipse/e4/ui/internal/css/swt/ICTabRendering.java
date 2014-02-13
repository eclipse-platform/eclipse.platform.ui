/*******************************************************************************
 *  Copyright (c) 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.eclipse.swt.graphics.Color;

public interface ICTabRendering {
	void setSelectedTabFill(Color color);

	void setSelectedTabFill(Color[] colors, int[] percents);

	void setUnselectedTabsColor(Color color);

	void setUnselectedTabsColor(Color[] colors, int[] percents);

	void setTabOutline(Color color);

	void setInnerKeyline(Color color);

	void setOuterKeyline(Color color);

	void setShadowColor(Color color);

	void setCornerRadius(int radius);

	void setShadowVisible(boolean visible);
}
