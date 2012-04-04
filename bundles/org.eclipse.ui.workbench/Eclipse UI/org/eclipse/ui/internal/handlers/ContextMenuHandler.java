package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class ContextMenuHandler extends AbstractHandler {
	/**
	 * @throws ExecutionException
	 *             {@inheritDoc}
	 */
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(exEvent);
		Display display = shell == null ? Display.getCurrent() : shell.getDisplay();
		Control focusControl = display.getFocusControl();
		if (focusControl != null) {
			Point pt = display.getCursorLocation();
			Event event = new Event();
			event.x = pt.x;
			event.y = pt.y;
			event.detail = SWT.MENU_KEYBOARD;
			focusControl.notifyListeners(SWT.MenuDetect, event);
			if (focusControl.isDisposed())
				return null;
			if (!event.doit)
				return null;
			Menu menu = focusControl.getMenu();

			if (menu != null && !menu.isDisposed()) {
				if (event.x != pt.x || event.y != pt.y) {
					menu.setLocation(event.x, event.y);
				}
				menu.setVisible(true);

			} else {
				Point size = focusControl.getSize();
				Point location = focusControl.toDisplay(0, 0);

				Event mouseEvent = new Event();
				mouseEvent.widget = focusControl;

				if (event.x < location.x || location.x + size.x <= event.x || event.y < location.y
						|| location.y + size.y <= event.y) {
					Point center = focusControl.toDisplay(Geometry.divide(size, 2));
					mouseEvent.x = center.x;
					mouseEvent.y = center.y;
					mouseEvent.type = SWT.MouseMove;
					display.post(mouseEvent);
				} else {
					mouseEvent.x = event.x;
					mouseEvent.y = event.y;
				}

				mouseEvent.button = 3;
				mouseEvent.type = SWT.MouseDown;
				display.post(mouseEvent);

				mouseEvent.type = SWT.MouseUp;
				display.post(mouseEvent);
			}
		}
		return null;
	}
}