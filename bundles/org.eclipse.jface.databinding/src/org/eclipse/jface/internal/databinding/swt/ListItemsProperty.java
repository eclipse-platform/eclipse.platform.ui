/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.3
 * 
 */
public class ListItemsProperty extends ControlStringListProperty {
	protected void doSetStringList(Control control, String[] list) {
		((List) control).setItems(list);
	}

	public String[] doGetStringList(Control control) {
		return ((List) control).getItems();
	}

	public String toString() {
		return "List.items[] <String>"; //$NON-NLS-1$
	}
}
