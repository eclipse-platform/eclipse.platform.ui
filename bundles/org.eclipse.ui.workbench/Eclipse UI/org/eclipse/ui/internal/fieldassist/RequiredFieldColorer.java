/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.fieldassist;

import org.eclipse.jface.fieldassist.FieldAssistColors;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * DecoratedFieldColorer watches for focus changes on a control and colors the
 * field when it loses focus and is empty.
 * 
 * @since 3.2
 */
public class RequiredFieldColorer implements FocusListener {

	private Control control;

	private IControlContentAdapter contentAdapter;

	private Color originalBackgroundColor;

	/**
	 * Create a RequiredFieldColorer on the specified control.
	 * 
	 * @param control
	 *            the control to watch
	 * @param contentAdapter
	 *            the content adapter used to retrieve the control's content
	 */
	public RequiredFieldColorer(Control control,
			IControlContentAdapter contentAdapter) {
		super();
		this.contentAdapter = contentAdapter;
		this.control = control;
		originalBackgroundColor = control.getBackground();
	}

	public void focusGained(FocusEvent event) {
		control.setBackground(originalBackgroundColor);
	}

	public void focusLost(FocusEvent event) {
		if (contentAdapter.getControlContents(control).length() == 0) {
			control.setBackground(FieldAssistColors
					.getRequiredFieldBackgroundColor(control));
		}
	}
}
