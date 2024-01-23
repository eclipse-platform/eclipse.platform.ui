/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.harness.util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.Assert;

/**
 * @since 3.1
 */
public class SWTEventHelper {

	public static void pressKeyCode(Display display, int keyCode) {
		pressKeyCode(display, keyCode, true);
	}

	public static void pressKeyCode(Display display, int keyCode,
			boolean runEventQueue) {
		keyCodeDown(display, keyCode, runEventQueue);
		keyCodeUp(display, keyCode, runEventQueue);
	}

	public static void pressKeyCodeCombination(Display display, int[] keyCodes) {
		pressKeyCodeCombination(display, keyCodes, true);
	}

	public static void pressKeyCodeCombination(Display display, int[] keyCodes,
			boolean runEventQueue) {
		for (int keyCode : keyCodes)
			keyCodeDown(display, keyCode, runEventQueue);
		for (int i = keyCodes.length - 1; i >= 0; i--)
			keyCodeUp(display, keyCodes[i], runEventQueue);
	}

	public static void keyCodeDown(Display display, int keyCode) {
		keyCodeEvent(display, SWT.KeyDown, keyCode, true);
	}

	public static void keyCodeDown(Display display, int keyCode,
			boolean runEventQueue) {
		keyCodeEvent(display, SWT.KeyDown, keyCode, runEventQueue);
	}

	public static void keyCodeUp(Display display, int keyCode) {
		keyCodeEvent(display, SWT.KeyUp, keyCode, true);
	}

	public static void keyCodeUp(Display display, int keyCode,
			boolean runEventQueue) {
		keyCodeEvent(display, SWT.KeyUp, keyCode, runEventQueue);
	}

	private static Event fgKeyCodeEvent = new Event();

	public static void keyCodeEvent(Display display, int type, int keyCode,
			boolean runEventQueue) {
		fgKeyCodeEvent.type = type;
		fgKeyCodeEvent.keyCode = keyCode;
		postEvent(display, fgKeyCodeEvent, runEventQueue);
	}

	public static void pressKeyChar(Display display, char keyChar) {
		pressKeyChar(display, keyChar, true);
	}

	public static void pressKeyChar(Display display, char keyChar,
			boolean runEventQueue) {
		keyCharDown(display, keyChar, runEventQueue);
		keyCharUp(display, keyChar, runEventQueue);
	}

	public static void pressKeyCharCombination(Display display, char[] keyChars) {
		pressKeyCharCombination(display, keyChars, true);
	}

	public static void pressKeyCharCombination(Display display,
			char[] keyChars, boolean runEventQueue) {
		for (char keyChar : keyChars)
			keyCharDown(display, keyChar, runEventQueue);
		for (int i = keyChars.length - 1; i >= 0; i--)
			keyCharUp(display, keyChars[i], runEventQueue);
	}

	public static void keyCharDown(Display display, char keyChar,
			boolean runEventQueue) {
		keyCharEvent(display, SWT.KeyDown, keyChar, runEventQueue);
	}

	public static void keyCharUp(Display display, char keyChar,
			boolean runEventQueue) {
		keyCharEvent(display, SWT.KeyUp, keyChar, runEventQueue);
	}

	private static Event fgKeyCharEvent = new Event();

	public static void keyCharEvent(Display display, int type, char keyChar,
			boolean runEventQueue) {
		fgKeyCharEvent.type = type;
		fgKeyCharEvent.character = keyChar;
		postEvent(display, fgKeyCharEvent, runEventQueue);
	}

	private static void postEvent(final Display display, final Event event,
			boolean runEventQueue) {
		DisplayHelper helper = new DisplayHelper() {
			@Override
			public boolean condition() {
				return display.post(event);
			}
		};
		Assert.assertTrue(helper.waitForCondition(display, 1000));

		if (runEventQueue)
			EditorTestHelper.runEventQueue();

	}

	private static Event fgMouseMoveEvent = new Event();

	public static void mouseMoveEvent(Display display, int x, int y,
			boolean runEventQueue) {
		fgMouseMoveEvent.type = SWT.MouseMove;
		fgMouseMoveEvent.x = x;
		fgMouseMoveEvent.y = y;
		postEvent(display, fgMouseMoveEvent, runEventQueue);
	}

	public static void mouseDownEvent(Display display, int button,
			boolean runEventQueue) {
		mouseButtonEvent(display, SWT.MouseDown, button, runEventQueue);
	}

	public static void mouseUpEvent(Display display, int button,
			boolean runEventQueue) {
		mouseButtonEvent(display, SWT.MouseUp, button, runEventQueue);
	}

	private static Event fgMouseButtonEvent = new Event();

	public static void mouseButtonEvent(Display display, int type, int button,
			boolean runEventQueue) {
		fgMouseButtonEvent.type = type;
		fgMouseButtonEvent.button = button;
		postEvent(display, fgMouseButtonEvent, runEventQueue);
	}

	private static boolean _dragDetected;

	// Returns true if it worked
	public static boolean performDnD(Widget startItem, Widget dropItem) {

		Control startControl;

		Rectangle boundsStart = null, boundsEnd = null;

		if (startItem instanceof TreeItem t) {
			startControl = t.getParent();
			boundsStart = Display.getCurrent().map(startControl, null,
					((TreeItem)startItem).getBounds());
		} else if (startItem instanceof Control c) {
			startControl = c;
			boundsStart = Display.getCurrent().map(startControl, null, startControl.getBounds());
		} else {
			throw new RuntimeException(startItem.getClass().getName());
		}

		if (dropItem instanceof TreeItem) {
			boundsEnd = Display.getCurrent().map(((TreeItem)dropItem).getParent(), null,
					((TreeItem)dropItem).getBounds());
		} else if (dropItem instanceof Control) {
			boundsEnd = Display.getCurrent().map(((Control)dropItem), null,
					((Control)dropItem).getBounds());
		}

		startControl.addDragDetectListener(e -> _dragDetected = true);


		int count = 0;
		_dragDetected = false;

		// On some platforms (Windows and Mac), drag desture detection does not
		// always happen
		while (!_dragDetected && ++count < 4) {
			performDnDInternal(boundsStart, boundsEnd);
			if (!_dragDetected) {
				if (count < 4)
					System.out.println("WARNING: DnD failed - drag gesture not detected retrying");
				DisplayHelper.sleep(Display.getCurrent(), 1000);
			}
		}

		if (!_dragDetected) {
			System.out
					.println("WARNING: DnD FAILED after " + count + " tries, giving up");
		}
		return _dragDetected;
	}

	public static void performDnDInternal(Rectangle boundsStart,
			Rectangle boundsEnd) {

		int fudge = 0;
		int gestureSize = 10;

		int xstart = boundsStart.x + fudge;
		int ystart = boundsStart.y + fudge;
		int xend = boundsEnd.x + fudge;
		int yend = boundsEnd.y + fudge;

		boolean possibleTrue = true;

		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			possibleTrue = false;
			gestureSize = 3;
		}

		SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xstart, ystart,
				false);
		SWTEventHelper.mouseDownEvent(Display.getCurrent(), 1, false);

		// Make it see a drag gesture
		// FIXME - this is not detected sometimes on Windows and the Mac
		SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xstart
				+ gestureSize, ystart, possibleTrue);
		SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xstart, ystart,
				possibleTrue);

		while (xstart != xend) {
			SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xstart, ystart,
					false);
			if (xstart < xend)
				xstart++;
			else
				xstart--;
		}

		while (ystart != yend) {
			SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xstart, ystart,
					false);
			if (ystart < yend)
				ystart++;
			else
				ystart--;
		}

		SWTEventHelper.mouseMoveEvent(Display.getCurrent(), xend, yend,
				possibleTrue);
		SWTEventHelper.mouseUpEvent(Display.getCurrent(), 1, possibleTrue);

		DisplayHelper.sleep(Display.getCurrent(), 100);
	}

}
