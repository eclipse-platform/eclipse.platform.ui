/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * This class is intended as internal and temporary
 */

package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

public class CTabETabHelper {
	public static Item[] getItems(Widget widget) {
		return ((CTabFolder) widget).getItems();
	}

	public static Item getSelection(Widget widget) {
		return ((CTabFolder) widget).getSelection();
	}

	public static int getSelectionIndex(Widget widget) {
		return ((CTabFolder) widget).getSelectionIndex();
	}

	public static void setShowClose(Item item, boolean bool) {
		if(item instanceof CTabItem) {
			((CTabItem) item).setShowClose(bool);
		}
	}

	public static Control getParent(Widget widget) {
		return ((CTabItem) widget).getParent();
	}
}
