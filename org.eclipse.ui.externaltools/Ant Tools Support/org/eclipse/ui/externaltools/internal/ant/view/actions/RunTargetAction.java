package org.eclipse.ui.externaltools.internal.ant.view.actions;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Actions which runs the selected target or the default target of the selected
 * project in the AntView.
 */
public class RunTargetAction extends Action implements IUpdate {
	
	private AntView view;
	
	public RunTargetAction(AntView view) {
		super("Run", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_RUN));
		setToolTipText("Run the default target of the selected build file");
		this.view= view;
	}

	public void run() {
		TargetNode target= getSelectedTarget();
		if (target == null) {
			return;
		}
		IFile file= AntUtil.getFile(target.getProject().getBuildFileName());
		AntLaunchShortcut shortcut= new AntLaunchShortcut();
		shortcut.launch(file, ILaunchManager.RUN_MODE, target.getName());
	}
	
	private void handleException(CoreException exception, TargetNode target) {
		MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Running Target", MessageFormat.format("An exception occurred attempting to launch target {0}. See log for details.", new String[] {target.getName()}));
		ExternalToolsPlugin.getDefault().log(MessageFormat.format("An exception occurred attempting to launch target {0}.", new String[] {target.getName()}), exception);
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
		Object data= iter.next();
		if (iter.hasNext() || (!(data instanceof TargetNode) && !(data instanceof ProjectNode))) {
			// Only enable for single selection of a TargetNode or ProjectNode
			return null;
		}
		if (data instanceof TargetNode) {
			return (TargetNode)selection.getFirstElement();
		} else {
			return ((ProjectNode)selection.getFirstElement()).getDefaultTarget();
		}
	}

}
