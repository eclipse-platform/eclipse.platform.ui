package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.e4.workbench.ui.IWorkbenchWindowHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WorkbenchWindowHandler implements IWorkbenchWindowHandler {

	public void dispose(Object appWindow) {
		((Shell)appWindow).dispose();
	}

	public void open(Object appWindow) {
		((Shell)appWindow).open();
	}

	public void setBounds(Object appWindow, int x, int y, int width, int height) {
		// A position of 0 is not possible on OS-X because then the title-bar is
		// hidden
		// below the MMenu-Bar
		// TODO is there a better method to find out the height of the title bar
		if (y == 0 && SWT.getPlatform().equals("carbon")) { //$NON-NLS-1$
			y = 20;
		}
		((Shell)appWindow).setBounds(x, y, width, height);
	}

	public void layout(Object appWindow) {
		((Composite) appWindow).layout(true);
	}

	public void runEvenLoop(Object appWindow) {
		Shell window = (Shell) appWindow;
		Display display = window.getDisplay();
		
		while (appWindow != null && !window.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		display.update();
	}

	public void close(Object widget) {
		// needs proper closing protocol
		 ((Shell) widget).close();
	}

}
