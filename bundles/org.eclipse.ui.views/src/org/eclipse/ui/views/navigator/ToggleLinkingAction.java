
package org.eclipse.ui.views.navigator;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 * 
 * @since 2.1
 */
public class ToggleLinkingAction extends ResourceNavigatorAction {

	/**
	 * Constructs a new action.
	 */
	public ToggleLinkingAction(IResourceNavigator navigator, String label) {
		super(navigator, label);
		setChecked(navigator.isLinkingEnabled());
	}

	/**
	 * Runs the action.
	 */
	public void run() {
		getNavigator().setLinkingEnabled(isChecked());
	}

}
