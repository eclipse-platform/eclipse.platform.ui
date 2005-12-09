/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * An {@link IControlContentAdapter} for SWT Text controls. This is a
 * convenience class for easily creating a {@link ContentProposalAdapter} for
 * text fields.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public class TextContentAdapter implements IControlContentAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#getControlContents(org.eclipse.swt.widgets.Control)
	 */
	public String getControlContents(Control control) {
		return ((Text) control).getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#setControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String)
	 */
	public void setControlContents(Control control, String text) {
		((Text) control).setText(text);
		((Text) control).setSelection(text.length(), text.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#insertControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String)
	 */
	public void insertControlContents(Control control, String text) {
		((Text) control).insert(text);
	}
}
