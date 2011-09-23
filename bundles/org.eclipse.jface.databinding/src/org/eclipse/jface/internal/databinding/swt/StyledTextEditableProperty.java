/*******************************************************************************
 * Copyright (c) 2011 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 306203)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.custom.StyledText;

/**
 * @since 3.3
 * 
 */
public class StyledTextEditableProperty extends WidgetBooleanValueProperty {
	boolean doGetBooleanValue(Object source) {
		return ((StyledText) source).getEditable();
	}

	void doSetBooleanValue(Object source, boolean value) {
		((StyledText) source).setEditable(value);
	}

	public String toString() {
		return "StyledText.editable <boolean>"; //$NON-NLS-1$
	}
}
