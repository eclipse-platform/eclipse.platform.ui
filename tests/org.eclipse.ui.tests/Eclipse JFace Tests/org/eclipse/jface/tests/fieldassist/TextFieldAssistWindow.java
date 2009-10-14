/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM - ongoing development
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class TextFieldAssistWindow extends AbstractFieldAssistWindow {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.jface.tests.fieldassist.AbstractFieldAssistWindow#
	 * createFieldAssistControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createFieldAssistControl(Composite parent) {
		return new Text(parent, SWT.SINGLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.fieldassist.AbstractFieldAssistWindow#getControlContentAdapter()
	 */
	protected IControlContentAdapter getControlContentAdapter() {
		return new TextContentAdapter();
	}

}
