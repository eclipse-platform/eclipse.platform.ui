package org.eclipse.ui.externaltools.internal.ant.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which affects the selected targets in the active targets pane of the
 * ant view. The selected targets are moved up in the order.
 */
public class TargetMoveUpAction extends Action implements IUpdate {
	
	private AntView view;

	public TargetMoveUpAction(AntView view) {
		super("Move Up", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_MOVE_UP));
		setDescription("Move the selected target up in the execution order");
		setToolTipText("Move the selected target up in the execution order");
		this.view= view;
	}
	
	/**
	 * Tells the Ant view to move the selected targets up.
	 */
	public void run() {
		view.moveUpTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		int indices[]= view.getTargetViewer().getTable().getSelectionIndices();
		if (indices.length == 0) {
			setEnabled(false);
		} else {
			setEnabled(indices[0] != 0);
		} 
	}

}
