package org.eclipse.e4.tools.emf.ui.internal.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ControlHighlighter {
	private static Shell s;

	public static void show(Control control) {
		if (s != null && !s.isDisposed()) {
			s.dispose();
		}
		s = new Shell(control.getShell(), SWT.NO_TRIM);
		Point p1 = control.toDisplay(0, 0);
		Point p2 = control.getSize();
		s.setBounds(p1.x, p1.y, p2.x, p2.y);
		s.setBackground(s.getDisplay().getSystemColor(SWT.COLOR_RED));
		s.setAlpha(100);
		s.open();
		s.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				hide();
			}
		});
	}

	public static void hide() {
		if (s != null) {
			s.dispose();
			s = null;
		}
	}
}
