/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;

/**
 * SWT Helper to get SWT styles {@link Widget} as String.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class SWTStyleHelpers {

	/**
	 * Return SWT style constant from {@link Widget} <code>widget</code> as
	 * String. Each SWT style are separate with space character.
	 *
	 * @param style
	 * @return
	 */
	public static String getSWTWidgetStyleAsString(Widget widget) {
		if (widget.isDisposed()) {
			return "";
		}
		return getSWTWidgetStyleAsString(widget.getStyle(), " ");
	}

	/**
	 * Return SWT style constant <code>style</code> as String. Each SWT style
	 * are separate with <code>separator</code> String.
	 *
	 * @param style
	 * @return
	 */
	public static String getSWTWidgetStyleAsString(int style, String separator) {
		StringBuffer swtStyles = new StringBuffer();
		// Use catch error if SWT version doesn't provide
		// the SWT constant
		try {
			if ((style & SWT.BAR) != 0)
				addSWTStyle(swtStyles, "SWT.BAR", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DROP_DOWN) != 0)
				addSWTStyle(swtStyles, "SWT.DROP_DOWN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.POP_UP) != 0)
				addSWTStyle(swtStyles, "SWT.POP_UP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SEPARATOR) != 0)
				addSWTStyle(swtStyles, "SWT.SEPARATOR", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TOGGLE) != 0)
				addSWTStyle(swtStyles, "SWT.TOGGLE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ARROW) != 0)
				addSWTStyle(swtStyles, "SWT.ARROW", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.PUSH) != 0)
				addSWTStyle(swtStyles, "SWT.PUSH", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.RADIO) != 0)
				addSWTStyle(swtStyles, "SWT.RADIO", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CHECK) != 0)
				addSWTStyle(swtStyles, "SWT.CHECK", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CASCADE) != 0)
				addSWTStyle(swtStyles, "SWT.CASCADE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MULTI) != 0)
				addSWTStyle(swtStyles, "SWT.MULTI", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SINGLE) != 0)
				addSWTStyle(swtStyles, "SWT.SINGLE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.READ_ONLY) != 0)
				addSWTStyle(swtStyles, "SWT.READ_ONLY", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.WRAP) != 0)
				addSWTStyle(swtStyles, "SWT.WRAP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SEARCH) != 0)
				addSWTStyle(swtStyles, "SWT.SEARCH", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SIMPLE) != 0)
				addSWTStyle(swtStyles, "SWT.SIMPLE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.PASSWORD) != 0)
				addSWTStyle(swtStyles, "SWT.PASSWORD", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHADOW_IN) != 0)
				addSWTStyle(swtStyles, "SWT.SHADOW_IN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHADOW_OUT) != 0)
				addSWTStyle(swtStyles, "SWT.SHADOW_OUT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHADOW_ETCHED_IN) != 0)
				addSWTStyle(swtStyles, "SWT.SHADOW_ETCHED_IN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHADOW_ETCHED_OUT) != 0)
				addSWTStyle(swtStyles, "SWT.SHADOW_ETCHED_OUT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHADOW_NONE) != 0)
				addSWTStyle(swtStyles, "SWT.SHADOW_NONE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.INDETERMINATE) != 0)
				addSWTStyle(swtStyles, "SWT.INDETERMINATE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TOOL) != 0)
				addSWTStyle(swtStyles, "SWT.TOOL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_TRIM) != 0)
				addSWTStyle(swtStyles, "SWT.NO_TRIM", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.RESIZE) != 0)
				addSWTStyle(swtStyles, "SWT.RESIZE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TITLE) != 0)
				addSWTStyle(swtStyles, "SWT.TITLE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CLOSE) != 0)
				addSWTStyle(swtStyles, "SWT.CLOSE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MENU) != 0)
				addSWTStyle(swtStyles, "SWT.MENU", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MIN) != 0)
				addSWTStyle(swtStyles, "SWT.MIN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MAX) != 0)
				addSWTStyle(swtStyles, "SWT.MAX", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.H_SCROLL) != 0)
				addSWTStyle(swtStyles, "SWT.H_SCROLL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.V_SCROLL) != 0)
				addSWTStyle(swtStyles, "SWT.V_SCROLL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.BORDER) != 0)
				addSWTStyle(swtStyles, "SWT.BORDER", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CLIP_CHILDREN) != 0)
				addSWTStyle(swtStyles, "SWT.CLIP_CHILDREN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CLIP_SIBLINGS) != 0)
				addSWTStyle(swtStyles, "SWT.CLIP_SIBLINGS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ON_TOP) != 0)
				addSWTStyle(swtStyles, "SWT.ON_TOP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHELL_TRIM) != 0)
				addSWTStyle(swtStyles, "SWT.SHELL_TRIM", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DIALOG_TRIM) != 0)
				addSWTStyle(swtStyles, "SWT.DIALOG_TRIM", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MODELESS) != 0)
				addSWTStyle(swtStyles, "SWT.MODELESS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MODELESS) != 0)
				addSWTStyle(swtStyles, "SWT.MODELESS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.PRIMARY_MODAL) != 0)
				addSWTStyle(swtStyles, "SWT.PRIMARY_MODAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.APPLICATION_MODAL) != 0)
				addSWTStyle(swtStyles, "SWT.APPLICATION_MODAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SYSTEM_MODAL) != 0)
				addSWTStyle(swtStyles, "SWT.SYSTEM_MODAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.HIDE_SELECTION) != 0)
				addSWTStyle(swtStyles, "SWT.HIDE_SELECTION", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.FULL_SELECTION) != 0)
				addSWTStyle(swtStyles, "SWT.FULL_SELECTION", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.FLAT) != 0)
				addSWTStyle(swtStyles, "SWT.FLAT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SMOOTH) != 0)
				addSWTStyle(swtStyles, "SWT.SMOOTH", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_BACKGROUND) != 0)
				addSWTStyle(swtStyles, "SWT.NO_BACKGROUND", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_FOCUS) != 0)
				addSWTStyle(swtStyles, "SWT.NO_FOCUS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_REDRAW_RESIZE) != 0)
				addSWTStyle(swtStyles, "SWT.NO_REDRAW_RESIZE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_MERGE_PAINTS) != 0)
				addSWTStyle(swtStyles, "SWT.NO_MERGE_PAINTS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO_RADIO_GROUP) != 0)
				addSWTStyle(swtStyles, "SWT.NO_RADIO_GROUP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LEFT_TO_RIGHT) != 0)
				addSWTStyle(swtStyles, "SWT.LEFT_TO_RIGHT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.RIGHT_TO_LEFT) != 0)
				addSWTStyle(swtStyles, "SWT.RIGHT_TO_LEFT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MIRRORED) != 0)
				addSWTStyle(swtStyles, "SWT.MIRRORED", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.VIRTUAL) != 0)
				addSWTStyle(swtStyles, "SWT.VIRTUAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DOUBLE_BUFFERED) != 0)
				addSWTStyle(swtStyles, "SWT.DOUBLE_BUFFERED", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.UP) != 0)
				addSWTStyle(swtStyles, "SWT.UP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TOP) != 0)
				addSWTStyle(swtStyles, "SWT.TOP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DOWN) != 0)
				addSWTStyle(swtStyles, "SWT.DOWN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.BOTTOM) != 0)
				addSWTStyle(swtStyles, "SWT.BOTTOM", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LEAD) != 0)
				addSWTStyle(swtStyles, "SWT.LEAD", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LEFT) != 0)
				addSWTStyle(swtStyles, "SWT.LEFT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TRAIL) != 0)
				addSWTStyle(swtStyles, "SWT.TRAIL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.RIGHT) != 0)
				addSWTStyle(swtStyles, "SWT.RIGHT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CENTER) != 0)
				addSWTStyle(swtStyles, "SWT.CENTER", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.HORIZONTAL) != 0)
				addSWTStyle(swtStyles, "SWT.HORIZONTAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.VERTICAL) != 0)
				addSWTStyle(swtStyles, "SWT.VERTICAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DATE) != 0)
				addSWTStyle(swtStyles, "SWT.DATE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.TIME) != 0)
				addSWTStyle(swtStyles, "SWT.TIME", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CALENDAR) != 0)
				addSWTStyle(swtStyles, "SWT.CALENDAR", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SHORT) != 0)
				addSWTStyle(swtStyles, "SWT.SHORT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MEDIUM) != 0)
				addSWTStyle(swtStyles, "SWT.MEDIUM", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LONG) != 0)
				addSWTStyle(swtStyles, "SWT.LONG", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.MOZILLA) != 0)
				addSWTStyle(swtStyles, "SWT.MOZILLA", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.BALLOON) != 0)
				addSWTStyle(swtStyles, "SWT.BALLOON", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.BEGINNING) != 0)
				addSWTStyle(swtStyles, "SWT.BEGINNING", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.FILL) != 0)
				addSWTStyle(swtStyles, "SWT.FILL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.DBCS) != 0)
				addSWTStyle(swtStyles, "SWT.DBCS", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ALPHA) != 0)
				addSWTStyle(swtStyles, "SWT.ALPHA", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NATIVE) != 0)
				addSWTStyle(swtStyles, "SWT.NATIVE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.PHONETIC) != 0)
				addSWTStyle(swtStyles, "SWT.PHONETIC", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ROMAN) != 0)
				addSWTStyle(swtStyles, "SWT.ROMAN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ICON_ERROR) != 0)
				addSWTStyle(swtStyles, "SWT.ICON_ERROR", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ICON_INFORMATION) != 0)
				addSWTStyle(swtStyles, "SWT.ICON_INFORMATION", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ICON_QUESTION) != 0)
				addSWTStyle(swtStyles, "SWT.ICON_QUESTION", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ICON_WARNING) != 0)
				addSWTStyle(swtStyles, "SWT.ICON_WARNING", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ICON_WORKING) != 0)
				addSWTStyle(swtStyles, "SWT.ICON_WORKING", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.OK) != 0)
				addSWTStyle(swtStyles, "SWT.OK", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.YES) != 0)
				addSWTStyle(swtStyles, "SWT.YES", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NO) != 0)
				addSWTStyle(swtStyles, "SWT.NO", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CANCEL) != 0)
				addSWTStyle(swtStyles, "SWT.CANCEL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ABORT) != 0)
				addSWTStyle(swtStyles, "SWT.ABORT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.RETRY) != 0)
				addSWTStyle(swtStyles, "SWT.RETRY", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.IGNORE) != 0)
				addSWTStyle(swtStyles, "SWT.IGNORE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.OPEN) != 0)
				addSWTStyle(swtStyles, "SWT.OPEN", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.SAVE) != 0)
				addSWTStyle(swtStyles, "SWT.SAVE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.INHERIT_NONE) != 0)
				addSWTStyle(swtStyles, "SWT.INHERIT_NONE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.INHERIT_DEFAULT) != 0)
				addSWTStyle(swtStyles, "SWT.INHERIT_DEFAULT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.INHERIT_FORCE) != 0)
				addSWTStyle(swtStyles, "SWT.INHERIT_FORCE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ERROR_MENU_NOT_DROP_DOWN) != 0)
				addSWTStyle(swtStyles, "SWT.ERROR_MENU_NOT_DROP_DOWN",
						separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ERROR_MENUITEM_NOT_CASCADE) != 0)
				addSWTStyle(swtStyles, "SWT.ERROR_MENUITEM_NOT_CASCADE",
						separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ERROR_MENU_NOT_BAR) != 0)
				addSWTStyle(swtStyles, "SWT.ERROR_MENU_NOT_BAR", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ERROR_MENU_NOT_POP_UP) != 0)
				addSWTStyle(swtStyles, "SWT.ERROR_MENU_NOT_POP_UP", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.NORMAL) != 0)
				addSWTStyle(swtStyles, "SWT.NORMAL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.BOLD) != 0)
				addSWTStyle(swtStyles, "SWT.BOLD", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.ITALIC) != 0)
				addSWTStyle(swtStyles, "SWT.ITALIC", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CAP_FLAT) != 0)
				addSWTStyle(swtStyles, "SWT.CAP_FLAT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CAP_ROUND) != 0)
				addSWTStyle(swtStyles, "SWT.CAP_ROUND", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.CAP_SQUARE) != 0)
				addSWTStyle(swtStyles, "SWT.CAP_SQUARE", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.JOIN_MITER) != 0)
				addSWTStyle(swtStyles, "SWT.JOIN_MITER", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.JOIN_BEVEL) != 0)
				addSWTStyle(swtStyles, "SWT.JOIN_BEVEL", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_SOLID) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_SOLID", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_DASH) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_DASH", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_DOT) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_DOT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_DASHDOT) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_DASHDOT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_DASHDOTDOT) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_DASHDOTDOT", separator);
		} catch (Exception e) {
		}
		try {
			if ((style & SWT.LINE_CUSTOM) != 0)
				addSWTStyle(swtStyles, "SWT.LINE_CUSTOM", separator);
		} catch (Exception e) {
		}
		return swtStyles.toString();
	}

	/**
	 * Add SWT String <code>style</code> to the {@link StringBuffer}
	 * <cod>swtStyles</code> and separate it with <code>separator</code>
	 * String.
	 *
	 * @param swtStyles
	 * @param style
	 * @param separator
	 */
	private static void addSWTStyle(StringBuffer swtStyles, String style,
			String separator) {
		if (swtStyles.length() > 0)
			swtStyles.append(separator);
		swtStyles.append(style);
	}
}
