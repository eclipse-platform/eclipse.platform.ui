package org.eclipse.ui.externaltools.internal.ant.view;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetErrorNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action for activating a target node selected in the ant view
 */
public class ActivateTargetAction extends Action implements IUpdate {
	private AntView view;
	
	public ActivateTargetAction(AntView view) {
		super("Activate Target");
		setDescription("Activate the selected target");
		this.view= view;
	}
	
	public void run() {
		TargetNode target= getSelectedTarget();
		if (target == null) {
			return;
		}
		view.activateTarget(target);
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		setEnabled(getSelectedTarget() != null);
	}

	/**
	 * Returns the selected target in the project viewer or <code>null</code> if
	 * no target is selected or more than one element is selected.
	 *
	 * @return TargetNode the selected target
	 */
	public TargetNode getSelectedTarget() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object data= iter.next();
			if (iter.hasNext() || !(data instanceof TargetNode) || (data instanceof TargetErrorNode)) {
				// Only enable for single selection of a TargetNode
				return null;
			}
		}
			return (TargetNode)selection.getFirstElement();
		}
	
}
