/*******************************************************************************
 * Copyright (c) 2008, 2020 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 */
public class SWTStyleHelpers {

	private record SwtStyle(int mask, String name) {
	}

	/**
	 * SWT style bits and the name each one contributes to the style string.
	 * Several constants share a bit value (SWT.DOWN and SWT.BOTTOM for example),
	 * and every name of a matched bit is emitted, so stylesheets can select on
	 * either spelling.
	 */
	private static final SwtStyle[] SWT_STYLES = {
			new SwtStyle(SWT.BAR, "SWT.BAR"),
			new SwtStyle(SWT.DROP_DOWN, "SWT.DROP_DOWN"),
			new SwtStyle(SWT.POP_UP, "SWT.POP_UP"),
			new SwtStyle(SWT.SEPARATOR, "SWT.SEPARATOR"),
			new SwtStyle(SWT.TOGGLE, "SWT.TOGGLE"),
			new SwtStyle(SWT.ARROW, "SWT.ARROW"),
			new SwtStyle(SWT.PUSH, "SWT.PUSH"),
			new SwtStyle(SWT.RADIO, "SWT.RADIO"),
			new SwtStyle(SWT.CHECK, "SWT.CHECK"),
			new SwtStyle(SWT.CASCADE, "SWT.CASCADE"),
			new SwtStyle(SWT.MULTI, "SWT.MULTI"),
			new SwtStyle(SWT.SINGLE, "SWT.SINGLE"),
			new SwtStyle(SWT.READ_ONLY, "SWT.READ_ONLY"),
			new SwtStyle(SWT.WRAP, "SWT.WRAP"),
			new SwtStyle(SWT.SEARCH, "SWT.SEARCH"),
			new SwtStyle(SWT.SIMPLE, "SWT.SIMPLE"),
			new SwtStyle(SWT.PASSWORD, "SWT.PASSWORD"),
			new SwtStyle(SWT.SHADOW_IN, "SWT.SHADOW_IN"),
			new SwtStyle(SWT.SHADOW_OUT, "SWT.SHADOW_OUT"),
			new SwtStyle(SWT.SHADOW_ETCHED_IN, "SWT.SHADOW_ETCHED_IN"),
			new SwtStyle(SWT.SHADOW_ETCHED_OUT, "SWT.SHADOW_ETCHED_OUT"),
			new SwtStyle(SWT.SHADOW_NONE, "SWT.SHADOW_NONE"),
			new SwtStyle(SWT.INDETERMINATE, "SWT.INDETERMINATE"),
			new SwtStyle(SWT.TOOL, "SWT.TOOL"),
			new SwtStyle(SWT.NO_TRIM, "SWT.NO_TRIM"),
			new SwtStyle(SWT.RESIZE, "SWT.RESIZE"),
			new SwtStyle(SWT.TITLE, "SWT.TITLE"),
			new SwtStyle(SWT.CLOSE, "SWT.CLOSE"),
			new SwtStyle(SWT.MENU, "SWT.MENU"),
			new SwtStyle(SWT.MIN, "SWT.MIN"),
			new SwtStyle(SWT.MAX, "SWT.MAX"),
			new SwtStyle(SWT.H_SCROLL, "SWT.H_SCROLL"),
			new SwtStyle(SWT.V_SCROLL, "SWT.V_SCROLL"),
			new SwtStyle(SWT.BORDER, "SWT.BORDER"),
			new SwtStyle(SWT.CLIP_CHILDREN, "SWT.CLIP_CHILDREN"),
			new SwtStyle(SWT.CLIP_SIBLINGS, "SWT.CLIP_SIBLINGS"),
			new SwtStyle(SWT.ON_TOP, "SWT.ON_TOP"),
			new SwtStyle(SWT.SHELL_TRIM, "SWT.SHELL_TRIM"),
			new SwtStyle(SWT.DIALOG_TRIM, "SWT.DIALOG_TRIM"),
			new SwtStyle(SWT.MODELESS, "SWT.MODELESS"),
			new SwtStyle(SWT.PRIMARY_MODAL, "SWT.PRIMARY_MODAL"),
			new SwtStyle(SWT.APPLICATION_MODAL, "SWT.APPLICATION_MODAL"),
			new SwtStyle(SWT.SYSTEM_MODAL, "SWT.SYSTEM_MODAL"),
			new SwtStyle(SWT.HIDE_SELECTION, "SWT.HIDE_SELECTION"),
			new SwtStyle(SWT.FULL_SELECTION, "SWT.FULL_SELECTION"),
			new SwtStyle(SWT.FLAT, "SWT.FLAT"),
			new SwtStyle(SWT.SMOOTH, "SWT.SMOOTH"),
			new SwtStyle(SWT.NO_BACKGROUND, "SWT.NO_BACKGROUND"),
			new SwtStyle(SWT.NO_FOCUS, "SWT.NO_FOCUS"),
			new SwtStyle(SWT.NO_REDRAW_RESIZE, "SWT.NO_REDRAW_RESIZE"),
			new SwtStyle(SWT.NO_MERGE_PAINTS, "SWT.NO_MERGE_PAINTS"),
			new SwtStyle(SWT.NO_RADIO_GROUP, "SWT.NO_RADIO_GROUP"),
			new SwtStyle(SWT.LEFT_TO_RIGHT, "SWT.LEFT_TO_RIGHT"),
			new SwtStyle(SWT.RIGHT_TO_LEFT, "SWT.RIGHT_TO_LEFT"),
			new SwtStyle(SWT.MIRRORED, "SWT.MIRRORED"),
			new SwtStyle(SWT.VIRTUAL, "SWT.VIRTUAL"),
			new SwtStyle(SWT.DOUBLE_BUFFERED, "SWT.DOUBLE_BUFFERED"),
			new SwtStyle(SWT.UP, "SWT.UP"),
			new SwtStyle(SWT.TOP, "SWT.TOP"),
			new SwtStyle(SWT.DOWN, "SWT.DOWN"),
			new SwtStyle(SWT.BOTTOM, "SWT.BOTTOM"),
			new SwtStyle(SWT.LEAD, "SWT.LEAD"),
			new SwtStyle(SWT.LEFT, "SWT.LEFT"),
			new SwtStyle(SWT.TRAIL, "SWT.TRAIL"),
			new SwtStyle(SWT.RIGHT, "SWT.RIGHT"),
			new SwtStyle(SWT.CENTER, "SWT.CENTER"),
			new SwtStyle(SWT.HORIZONTAL, "SWT.HORIZONTAL"),
			new SwtStyle(SWT.VERTICAL, "SWT.VERTICAL"),
			new SwtStyle(SWT.DATE, "SWT.DATE"),
			new SwtStyle(SWT.TIME, "SWT.TIME"),
			new SwtStyle(SWT.CALENDAR, "SWT.CALENDAR"),
			new SwtStyle(SWT.SHORT, "SWT.SHORT"),
			new SwtStyle(SWT.MEDIUM, "SWT.MEDIUM"),
			new SwtStyle(SWT.LONG, "SWT.LONG"),
			new SwtStyle(SWT.BALLOON, "SWT.BALLOON"),
			new SwtStyle(SWT.BEGINNING, "SWT.BEGINNING"),
			new SwtStyle(SWT.FILL, "SWT.FILL"),
			new SwtStyle(SWT.DBCS, "SWT.DBCS"),
			new SwtStyle(SWT.ALPHA, "SWT.ALPHA"),
			new SwtStyle(SWT.NATIVE, "SWT.NATIVE"),
			new SwtStyle(SWT.PHONETIC, "SWT.PHONETIC"),
			new SwtStyle(SWT.ROMAN, "SWT.ROMAN"),
			new SwtStyle(SWT.ICON_ERROR, "SWT.ICON_ERROR"),
			new SwtStyle(SWT.ICON_INFORMATION, "SWT.ICON_INFORMATION"),
			new SwtStyle(SWT.ICON_QUESTION, "SWT.ICON_QUESTION"),
			new SwtStyle(SWT.ICON_WARNING, "SWT.ICON_WARNING"),
			new SwtStyle(SWT.ICON_WORKING, "SWT.ICON_WORKING"),
			new SwtStyle(SWT.OK, "SWT.OK"),
			new SwtStyle(SWT.YES, "SWT.YES"),
			new SwtStyle(SWT.NO, "SWT.NO"),
			new SwtStyle(SWT.CANCEL, "SWT.CANCEL"),
			new SwtStyle(SWT.ABORT, "SWT.ABORT"),
			new SwtStyle(SWT.RETRY, "SWT.RETRY"),
			new SwtStyle(SWT.IGNORE, "SWT.IGNORE"),
			new SwtStyle(SWT.OPEN, "SWT.OPEN"),
			new SwtStyle(SWT.SAVE, "SWT.SAVE"),
			new SwtStyle(SWT.INHERIT_NONE, "SWT.INHERIT_NONE"),
			new SwtStyle(SWT.INHERIT_DEFAULT, "SWT.INHERIT_DEFAULT"),
			new SwtStyle(SWT.INHERIT_FORCE, "SWT.INHERIT_FORCE"),
			new SwtStyle(SWT.ERROR_MENU_NOT_DROP_DOWN, "SWT.ERROR_MENU_NOT_DROP_DOWN"),
			new SwtStyle(SWT.ERROR_MENUITEM_NOT_CASCADE, "SWT.ERROR_MENUITEM_NOT_CASCADE"),
			new SwtStyle(SWT.ERROR_MENU_NOT_BAR, "SWT.ERROR_MENU_NOT_BAR"),
			new SwtStyle(SWT.ERROR_MENU_NOT_POP_UP, "SWT.ERROR_MENU_NOT_POP_UP"),
			new SwtStyle(SWT.NORMAL, "SWT.NORMAL"),
			new SwtStyle(SWT.BOLD, "SWT.BOLD"),
			new SwtStyle(SWT.ITALIC, "SWT.ITALIC"),
			new SwtStyle(SWT.CAP_FLAT, "SWT.CAP_FLAT"),
			new SwtStyle(SWT.CAP_ROUND, "SWT.CAP_ROUND"),
			new SwtStyle(SWT.CAP_SQUARE, "SWT.CAP_SQUARE"),
			new SwtStyle(SWT.JOIN_MITER, "SWT.JOIN_MITER"),
			new SwtStyle(SWT.JOIN_BEVEL, "SWT.JOIN_BEVEL"),
			new SwtStyle(SWT.LINE_SOLID, "SWT.LINE_SOLID"),
			new SwtStyle(SWT.LINE_DASH, "SWT.LINE_DASH"),
			new SwtStyle(SWT.LINE_DOT, "SWT.LINE_DOT"),
			new SwtStyle(SWT.LINE_DASHDOT, "SWT.LINE_DASHDOT"),
			new SwtStyle(SWT.LINE_DASHDOTDOT, "SWT.LINE_DASHDOTDOT"),
			new SwtStyle(SWT.LINE_CUSTOM, "SWT.LINE_CUSTOM") };

	/**
	 * Return SWT style constant from {@link Widget} <code>widget</code> as
	 * String. Each SWT style are separate with space character.
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
	 */
	public static String getSWTWidgetStyleAsString(int style, String separator) {
		if (style == 0) {
			return "";
		}
		StringBuilder swtStyles = new StringBuilder();
		for (SwtStyle swtStyle : SWT_STYLES) {
			if ((style & swtStyle.mask()) != 0) {
				if (swtStyles.length() > 0) {
					swtStyles.append(separator);
				}
				swtStyles.append(swtStyle.name());
			}
		}
		return swtStyles.length() == 0 ? "" : swtStyles.toString().intern();
	}
}
