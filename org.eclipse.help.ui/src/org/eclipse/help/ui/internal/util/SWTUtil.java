/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.util;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
/**
 * Utility class to simplify access to some SWT resources. 
 */
public class SWTUtil {
	private static double fgHorizontalDialogUnitSize = 0.0;
	private static double fgVerticalDialogUnitSize = 0.0;
	private static void initializeDialogUnits(Control control) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		int averageWidth = gc.getFontMetrics().getAverageCharWidth();
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();
		fgHorizontalDialogUnitSize = averageWidth * 0.25;
		fgVerticalDialogUnitSize = height * 0.125;
	}
	/**
	 * @see DialogPage#convertHorizontalDLUsToPixels
	 */
	private static int convertHorizontalDLUsToPixels(int dlus) {
		return (int) Math.round(dlus * fgHorizontalDialogUnitSize);
	}
	/**
	 * @see DialogPage#convertVerticalDLUsToPixels
	 */
	private static int convertVerticalDLUsToPixels(int dlus) {
		return (int) Math.round(dlus * fgVerticalDialogUnitSize);
	}
	/**
	 * @see DialogPage#convertWidthInCharsToPixels
	 */
	public static int convertWidthInCharsToPixels(int chars, Text text) {
		if (fgHorizontalDialogUnitSize == 0.0)
			initializeDialogUnits(text);
		return convertHorizontalDLUsToPixels(chars * 4);
	}
	/**
	 * Returns a width hint for a button control.
	 */
	public static int getButtonWidthHint(Button button) {
		if (fgHorizontalDialogUnitSize == 0.0) {
			initializeDialogUnits(button);
		}
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(
			widthHint,
			button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	/**
	 * Returns a height hint for a button control.
	 */
	public static int getButtonHeigthHint(Button button) {
		if (fgHorizontalDialogUnitSize == 0.0) {
			initializeDialogUnits(button);
		}
		return convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	}
	/**
	 * Sets width and height hint for the button control.
	 * <b>Note:</b> This is a NOP if the button's layout data is not
	 * an instance of <code>GridData</code>.
	 * 
	 * @param	the button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).heightHint = SWTUtil.getButtonHeigthHint(button);
			((GridData) gd).widthHint = SWTUtil.getButtonWidthHint(button);
		}
	}
}