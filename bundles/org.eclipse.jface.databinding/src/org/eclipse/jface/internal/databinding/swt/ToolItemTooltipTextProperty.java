/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 262946
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 3.3
 * 
 */
public class ToolItemTooltipTextProperty extends WidgetStringValueProperty {
	String doGetStringValue(Object source) {
		return ((ToolItem) source).getToolTipText();
	}

	void doSetStringValue(Object source, String value) {
		((ToolItem) source).setToolTipText(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "ToolItem.toolTipText <String>"; //$NON-NLS-1$
	}
}
