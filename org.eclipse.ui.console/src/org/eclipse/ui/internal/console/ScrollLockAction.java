package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsoleView;

/**
 * Toggles console auto-scroll
 * 
 * @since 3.1
 */
public class ScrollLockAction extends Action {

    private IConsoleView fConsoleView;
	
	public ScrollLockAction(IConsoleView consoleView) {
		super(ConsoleMessages.getString("ScrollLockAction.0")); //$NON-NLS-1$
        fConsoleView = consoleView;
		
		setToolTipText(ConsoleMessages.getString("ScrollLockAction.1"));  //$NON-NLS-1$
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_LCL_LOCK));		
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_LOCK));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_LOCK));

		boolean checked = fConsoleView.getScrollLock();  
		setChecked(checked);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
        fConsoleView.setScrollLock(isChecked());
	}
	
	public void dispose() {
        fConsoleView = null;
	}

}

