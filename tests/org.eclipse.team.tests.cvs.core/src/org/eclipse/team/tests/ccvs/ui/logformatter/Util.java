/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.logformatter;


import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Util {
	private static final NumberFormat percentageFormat = new DecimalFormat("####0.00%");
	
	public static String formatPercentageRatio(int numerator, int denominator) {
		return percentageFormat.format((double)numerator / denominator);
	}
}
