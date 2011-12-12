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
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		Display display = shell == null ? Display.getCurrent() : shell.getDisplay();
		Control focusControl = display.getFocusControl();
		if (focusControl != null) {
			Menu menu = focusControl.getMenu();
			if (menu != null) {
				menu.setVisible(true);
			} else {
				Point size = focusControl.getSize();
				Point center = focusControl.toDisplay(Geometry.divide(size, 2));

				Point location = focusControl.toDisplay(0, 0);

				Event mouseEvent = new Event();
				mouseEvent.widget = focusControl;
				mouseEvent.x = center.x;
				mouseEvent.y = center.y;

				Point cursorLoc = display.getCursorLocation();
				if (cursorLoc.x < location.x || location.x + size.x <= cursorLoc.x
						|| cursorLoc.y < location.y || location.y + size.y <= cursorLoc.y) {
					mouseEvent.type = SWT.MouseMove;
					display.post(mouseEvent);
				}

				mouseEvent.button = 2;
				mouseEvent.type = SWT.MouseDown;
				display.post(mouseEvent);

				mouseEvent.type = SWT.MouseUp;
				display.post(mouseEvent);
			}
		}
		return null;
	}
}
