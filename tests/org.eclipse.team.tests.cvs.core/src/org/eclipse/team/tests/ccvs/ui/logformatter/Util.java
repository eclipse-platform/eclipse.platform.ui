package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Util {
	private static final NumberFormat percentageFormat = new DecimalFormat("####0.00%");
	
	public static String formatPercentageRatio(int numerator, int denominator) {
		return percentageFormat.format((double)numerator / denominator);
	}
}
