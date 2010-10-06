package org.eclipse.ui.internal.cocoa;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author Prakash G.R. (grprakash@gmail.com)
 * @since 3.7 
 *
 */
public class MinimizeWindowHandler extends AbstractWindowHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell activeShell = HandlerUtil.getActiveShell(event);
		NSWindow window = activeShell.view.window();
		if(window!=null)
			window.miniaturize(window);
		return null;
	}

}
