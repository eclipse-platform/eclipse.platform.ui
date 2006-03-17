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
package org.eclipse.jface.examples.databinding.compositetable.month;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class WeekHeader extends Composite {

	private Label label = null;
	private Label label1 = null;
	private Label label2 = null;
	private Label label3 = null;
   private Label label4 = null;
   private Label label5 = null;
   private Label label6 = null;

	public WeekHeader(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new org.eclipse.swt.graphics.Point(536,54));
        label = new Label(this, SWT.CENTER);
        label.setBounds(new org.eclipse.swt.graphics.Rectangle(23,18,53,18));
        label.setText("Sunday");
        label1 = new Label(this, SWT.CENTER);
        label1.setBounds(new org.eclipse.swt.graphics.Rectangle(98,18,79,17));
        label1.setText("Monday");
        label2 = new Label(this, SWT.CENTER);
        label2.setBounds(new org.eclipse.swt.graphics.Rectangle(187,18,47,17));
        label2.setText("Tuesday");
        label3 = new Label(this, SWT.CENTER);
        label3.setBounds(new org.eclipse.swt.graphics.Rectangle(256,17,67,17));
        label3.setText("Wednesday");
        label4 = new Label(this, SWT.CENTER);
        label4.setBounds(new org.eclipse.swt.graphics.Rectangle(338,17,62,20));
        label4.setText("Thursday");
        label5 = new Label(this, SWT.CENTER);
        label5.setBounds(new org.eclipse.swt.graphics.Rectangle(415,16,43,21));
        label5.setText("Friday");
        label6 = new Label(this, SWT.CENTER);
        label6.setBounds(new org.eclipse.swt.graphics.Rectangle(469,16,61,23));
        label6.setText("Saturday");
			
	}

}  //  @jve:decl-index=0:visual-constraint="11,16"
