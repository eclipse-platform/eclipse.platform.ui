package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;

/**
 * Toggles console auto-scroll
 * 
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 *
 */
public class ScrollLockAction extends Action implements IPropertyChangeListener {

	private IPreferenceStore prefStore = ConsolePlugin.getDefault().getPreferenceStore();
    private IOConsoleViewer viewer;
	
	public ScrollLockAction(IOConsoleViewer viewer) {
		super(ConsoleMessages.getString("ScrollLockAction.0")); //$NON-NLS-1$
		this.viewer = viewer;
		setToolTipText(ConsoleMessages.getString("ScrollLockAction.1"));  //$NON-NLS-1$
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_LCL_LOCK));		
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_LOCK));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_LOCK));

		boolean checked = prefStore.getBoolean(IInternalConsoleConstants.PREF_CONSOLE_SCROLL_LOCK); 
		setChecked(checked);
		viewer.setAutoScroll(!checked);
		prefStore.addPropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
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
			viewer.setAutoScroll(!checked);
		}
		
	}
}

