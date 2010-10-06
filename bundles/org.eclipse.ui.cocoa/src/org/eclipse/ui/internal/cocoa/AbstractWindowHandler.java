package org.eclipse.ui.internal.cocoa;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Prakash G.R. (grprakash@gmail.com)
 * @since 3.7 
 *
 */

public abstract class AbstractWindowHandler extends AbstractHandler {

	public boolean isEnabled() {
		boolean enabled = false;
		Shell activeShell = Display.getDefault().getActiveShell();
		if(activeShell !=null) {
			NSWindow window = activeShell.view.window();
			if(window!=null)
				enabled = !window.isMiniaturized();
		}
		return enabled;
	}
}