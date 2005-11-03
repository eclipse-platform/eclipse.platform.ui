/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class Row extends Composite {

	public Text name = null;
	public Text address = null;
	public Text city = null;
	public Text state = null;

	public Row(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		this.setSize(new org.eclipse.swt.graphics.Point(131,52));
		name = new Text(this, SWT.NONE);
		name.setBounds(new org.eclipse.swt.graphics.Rectangle(12,12,10,25));
		address = new Text(this, SWT.NONE);
		address.setBounds(new org.eclipse.swt.graphics.Rectangle(43,12,10,25));
		city = new Text(this, SWT.NONE);
		city.setBounds(new org.eclipse.swt.graphics.Rectangle(75,14,10,25));
		state = new Text(this, SWT.NONE);
		state.setBounds(new org.eclipse.swt.graphics.Rectangle(109,16,10,25));
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
