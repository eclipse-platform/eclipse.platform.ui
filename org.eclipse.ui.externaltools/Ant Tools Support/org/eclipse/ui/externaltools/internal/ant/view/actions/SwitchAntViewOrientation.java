package org.eclipse.ui.externaltools.internal.ant.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;

/**
 * Action that switches the orientation of the Ant View, toggling it between
 * vertical and horizontal alignment.
 */
public class SwitchAntViewOrientation extends Action {
	
	private AntView view;
	
	public SwitchAntViewOrientation(AntView view) {
		super("Switch view orientation");
		this.view= view;
		setToolTipText("Toggle the orientation of the view between horizontal and vertical splitting");
	}

	/**
	 * Toggle's the ant view's orientation
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		view.switchViewOrientation();
	}

}
