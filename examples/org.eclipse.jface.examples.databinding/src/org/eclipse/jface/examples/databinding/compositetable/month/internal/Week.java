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
package org.eclipse.jface.examples.databinding.compositetable.month.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.2
 */
public class Week extends Composite {

    private final Day[] days;

    /**
     * @param parent
     * @param style
     */
    public Week(Composite parent, int style) {
		super(parent, style);
		initialize();
		this.days = new Day[] {createDay(), createDay(), createDay(), 
				createDay(), createDay(), createDay(), createDay()};
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		gridLayout.horizontalSpacing = 1;
		gridLayout.verticalSpacing = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.makeColumnsEqualWidth = true;
		this.setLayout(gridLayout);
//		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		this.setSize(new Point(870, 158));
	}

   /**
	 * This method initializes day	
	 */
	private Day createDay() {
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessVerticalSpace = true;
		Day day = new Day(this, SWT.NONE);
		day.setLayoutData(gd);
		return day;
	}
	
	/**
	 * Return a particular day in this week.
	 * 
	 * @param dayNumber 0-based day number
	 * @return The day corresponding to dayNumber
	 */
	public Day getDay(int dayNumber) {
		return days[dayNumber];
	}

}  //  @jve:decl-index=0:visual-constraint="8,13"
