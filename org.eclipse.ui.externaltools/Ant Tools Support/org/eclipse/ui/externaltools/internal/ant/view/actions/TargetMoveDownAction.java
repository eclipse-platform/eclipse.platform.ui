package org.eclipse.ui.externaltools.internal.ant.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which affects the selected targets in the active targets pane of the
 * ant view. The selected targets are moved down in the order.
 */
public class TargetMoveDownAction extends Action implements IUpdate {
	
	private AntView view;

	public TargetMoveDownAction(AntView view) {
		super("Move Down", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_MOVE_DOWN));
		setDescription("Move the selected target down in the execution order");
		setToolTipText("Move the selected target down in the execution order");
		this.view= view;
	}
	
	/**
	 * Tells the Ant view to move the selected targets down.
	 */
	public void run() {
		view.moveDownTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		Table table= view.getTargetViewer().getTable();
		int indices[]= table.getSelectionIndices();
		if (indices.length == 0) {
			setEnabled(false);
		} else {
			setEnabled(indices[indices.length - 1] != table.getItemCount() - 1);
		}
	}

}
