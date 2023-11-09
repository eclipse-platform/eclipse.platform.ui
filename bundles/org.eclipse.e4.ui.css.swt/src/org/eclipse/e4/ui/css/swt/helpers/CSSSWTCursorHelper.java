/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
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
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSSWTCursorHelper {
	private static final String DEFAULT_CURSOR = "defaultCursor";

	/**
	 * @see <a href=
	 *      "http://www.w3schools.com/css/pr_class_cursor.asp">http://www.w3schools.com/css/pr_class_cursor.asp</a>
	 */
	public static Cursor getSWTCursor(CSSValue value, Display display) {
		if (!(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
			return null;
		}
		int i = getSWTCursorId((CSSPrimitiveValue) value);
		if (i == SWT.NONE) {
			return null;
		}

		return new Cursor(display, i);
	}

	public static String getCSSCursor(Cursor cursor) {
		if (cursor == null) {
			return "auto";
		}
		// switch (cursor.getType()) {
		// case Cursor.DEFAULT_CURSOR:
		// // The default cursor (often an arrow)
		// return "default";
		// case Cursor.CROSSHAIR_CURSOR:
		// // The cursor render as a crosshair
		// return "crosshair";
		// case Cursor.HAND_CURSOR:
		// // The cursor render as a pointer (a hand) that indicates a link
		// return "pointer";
		// case Cursor.MOVE_CURSOR:
		// // The cursor indicates something that should be moved
		// return "move";
		// case Cursor.E_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved
		// // right (east)
		// return "e-resize";
		// case Cursor.NE_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved up
		// // and right (north/east)
		// return "ne-resize";
		// case Cursor.NW_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved up
		// // and left (north/west)
		// return "nw-resize";
		// case Cursor.N_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved up
		// // (north)
		// return "n-resize";
		// case Cursor.SE_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved
		// // down and right (south/east)
		// return "se-resize";
		// case Cursor.SW_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved
		// // down and left (south/west)
		// return "sw-resize";
		// case Cursor.S_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved
		// // down (south)
		// return "s-resize";
		// case Cursor.W_RESIZE_CURSOR:
		// // The cursor indicates that an edge of a box is to be moved
		// // left (west)
		// return "w-resize";
		// case Cursor.TEXT_CURSOR:
		// // The cursor indicates text
		// return "text";
		// case Cursor.WAIT_CURSOR:
		// // The cursor indicates that the program is busy (often a watch
		// // or an hourglass)
		// return "wait";
		// // TODO : manage help cursor
		// }
		return "auto";
	}

	public static int getSWTCursorId(CSSPrimitiveValue value) {
		String cursorName = value.getStringValue();
		switch (cursorName) {
		case "default":
			// The default cursor (often an arrow)
			return SWT.NONE;
		case "auto":
			// TODO : manage auto
			// Default. The browser sets a cursor
			return SWT.NONE;
		case "crosshair":
			// The cursor render as a crosshair
			return SWT.CURSOR_CROSS;
		case "pointer":
			// The cursor render as a pointer (a hand) that indicates a link
			return SWT.CURSOR_HAND;
		case "move":
			// The cursor indicates something that should be moved
			return SWT.CURSOR_UPARROW;
		case "e-resize":
			// The cursor indicates that an edge of a box is to be moved right
			// (east)
			return SWT.CURSOR_SIZEE;
		case "ne-resize":
			// The cursor indicates that an edge of a box is to be moved up and
			// right (north/east)
			return SWT.CURSOR_SIZENE;
		case "nw-resize":
			// The cursor indicates that an edge of a box is to be moved up and
			// left (north/west)
			return SWT.CURSOR_SIZENW;
		case "n-resize":
			// The cursor indicates that an edge of a box is to be moved up
			// (north)
			return SWT.CURSOR_SIZEN;
		case "se-resize":
			// The cursor indicates that an edge of a box is to be moved down
			// and right (south/east)
			return SWT.CURSOR_SIZESE;
		case "sw-resize":
			// The cursor indicates that an edge of a box is to be moved down
			// and left (south/west)
			return SWT.CURSOR_SIZESW;
		case "s-resize":
			// The cursor indicates that an edge of a box is to be moved down
			// (south)
			return SWT.CURSOR_SIZES;
		case "w-resize":
			// The cursor indicates that an edge of a box is to be moved left
			// (west)
			return SWT.CURSOR_SIZEW;
		case "text":
			// The cursor indicates text
			return SWT.CURSOR_UPARROW;
		case "wait":
			// The cursor indicates that the program is busy (often a watch or
			// an hourglass)
			return SWT.CURSOR_WAIT;
		case "help":
			// The cursor indicates that help is available (often a question
			// mark or a balloon)
			return SWT.CURSOR_HELP;
		default:
			return SWT.NONE;
		}
	}

	public static void storeDefaultCursor(Control control) {
		if (control.getData(DEFAULT_CURSOR) == null) {
			control.setData(DEFAULT_CURSOR, control.getCursor());
		}
	}

	public static void restoreDefaultCursor(Control control) {
		Cursor defaultCursor = (Cursor) control.getData(DEFAULT_CURSOR);
		if (defaultCursor != null) {
			control.setCursor(
					defaultCursor.isDisposed() ? control.getDisplay().getSystemCursor(SWT.ARROW) : defaultCursor);
		}
	}
}
