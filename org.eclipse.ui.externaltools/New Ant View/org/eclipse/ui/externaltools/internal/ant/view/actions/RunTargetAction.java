package org.eclipse.ui.externaltools.internal.ant.view.actions;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class RunTargetAction extends Action implements IUpdate {
	
	private AntView view;
	
	public RunTargetAction(AntView view) {
		super("Run Target");
		setDescription("Run the selected target");
		this.view= view;
	}

	public void run() {
		TargetNode target= getSelectedTarget();
		if (target == null) {
			return;
		}
		ILaunchConfigurationType type= DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		ILaunchConfigurationWorkingCopy config= null;
		try {
			config= type.newInstance(null, "Temp");
		} catch (CoreException e) {
			handleException(e, target);
			return;
		}
		config.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, target.getName());
		config.setAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, true);
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, ((ProjectNode)target.getParent()).getBuildFileName());
		config.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
		DebugUITools.launch(config, ILaunchManager.RUN_MODE);
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
		while (iter.hasNext()) {
			Object data= iter.next();
			if (iter.hasNext() || !(data instanceof TargetNode)) {
				// Only enable for single selection of a TargetNode
				return null;
			}
		}
		return (TargetNode)selection.getFirstElement();
	}

}
