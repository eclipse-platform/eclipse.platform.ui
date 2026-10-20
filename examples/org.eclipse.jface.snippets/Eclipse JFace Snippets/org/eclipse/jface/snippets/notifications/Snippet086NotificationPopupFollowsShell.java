/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform contributors.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.snippets.notifications;

import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows a {@link NotificationPopup} that stays anchored to the bottom-right
 * corner of a top-level shell. Moving or resizing the shell repositions the
 * popup, so it appears to be attached to the window.
 */
public class Snippet086NotificationPopupFollowsShell {

	/** Same edge padding the popup uses for its initial placement. */
	private static final int PADDING_EDGE = 5;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Move or resize me"); //$NON-NLS-1$
		shell.setSize(500, 350);
		shell.open();

		NotificationPopup popup = NotificationPopup.forShell(shell) //
				.text("I follow the window. Drag the shell around!") //$NON-NLS-1$
				.title("Anchored notification", true) //$NON-NLS-1$
				.delay(0) // 0 disables the auto-close so the popup stays while you move the shell
				.build();
		popup.open();

		Shell popupShell = popup.getShell();

		// Re-anchor the popup whenever the parent shell moves or is resized.
		ControlListener follower = new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				anchorToBottomRight(shell, popupShell);
			}

			@Override
			public void controlResized(ControlEvent e) {
				anchorToBottomRight(shell, popupShell);
			}
		};
		shell.addControlListener(follower);

		// Stop tracking once the popup is gone (e.g. closed via its close button).
		popupShell.addDisposeListener(e -> {
			if (!shell.isDisposed()) {
				shell.removeControlListener(follower);
			}
		});

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

	private static void anchorToBottomRight(Shell parent, Shell popupShell) {
		if (popupShell == null || popupShell.isDisposed()) {
			return;
		}
		Rectangle clientArea = parent.getClientArea();
		Point clientOrigin = parent.toDisplay(0, 0);
		Point size = popupShell.getSize();
		int x = clientOrigin.x + clientArea.width - size.x - PADDING_EDGE;
		int y = clientOrigin.y + clientArea.height - size.y - PADDING_EDGE;
		popupShell.setLocation(x, y);
	}
}
