package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsole;

/**
 * Toggles console auto-scroll
 * 
 * @since 3.1
 */
public class ScrollLockAction extends Action implements IPropertyChangeListener {

	private IPreferenceStore prefStore = ConsolePlugin.getDefault().getPreferenceStore();
    private IOConsole fConsole;
	
	public ScrollLockAction(IOConsole console) {
		super(ConsoleMessages.getString("ScrollLockAction.0")); //$NON-NLS-1$
		fConsole = console;
		
		setToolTipText(ConsoleMessages.getString("ScrollLockAction.1"));  //$NON-NLS-1$
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_LCL_LOCK));		
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_LOCK));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_LOCK));

		boolean checked = prefStore.getBoolean(IInternalConsoleConstants.PREF_CONSOLE_SCROLL_LOCK); 
		setChecked(checked);
		fConsole.setAutoScroll(!checked);
		prefStore.addPropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
	    // There may be mulitple consoles open but scroll lock is considered global to all of them.
	    // Need to fire the event so that all consoles have a chance to update their state.
		prefStore.setValue(IInternalConsoleConstants.PREF_CONSOLE_SCROLL_LOCK, isChecked());
	}
	
	public void dispose() {
		prefStore.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IInternalConsoleConstants.PREF_CONSOLE_SCROLL_LOCK)) {
		    boolean checked = prefStore.getBoolean(IInternalConsoleConstants.PREF_CONSOLE_SCROLL_LOCK);
			setChecked(checked);
			fConsole.setAutoScroll(!checked);
		}
		
	}
}

