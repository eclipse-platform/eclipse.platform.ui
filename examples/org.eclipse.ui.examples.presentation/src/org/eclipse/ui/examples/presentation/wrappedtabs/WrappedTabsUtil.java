/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.presentation.wrappedtabs;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 3.0
 */
public class WrappedTabsUtil {
	private WrappedTabsUtil() {
		
	}
	
	/**
	 * Returns the width of the widest ToolItem in the given toolbar
	 * 
	 * @param toMeasure toolbar to measure
	 * @return the width (pixels) of the widest ToolItem in the given toolbar
	 */
	public static int getMaximumItemWidth(ToolBar toMeasure) {
		int maxWidth = 0;
		ToolItem items[] = toMeasure.getItems();
		
		for (int i = 0; i < items.length; i++) {
			ToolItem item = items[i];
			
			maxWidth = Math.max(maxWidth, item.getBounds().width);
		}
		
		return maxWidth;
	}
}
