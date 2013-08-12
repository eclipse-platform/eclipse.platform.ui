package org.eclipse.debug.internal.examples.mixedmode;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Class defines an action used to clear all of the preferred launch delegate
 * information from the launch configuration types and from the debug
 * preferences
 */
public class ClearAllDelegateInformationAction implements IWorkbenchWindowActionDelegate {

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				try {
					ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
					ILaunchConfigurationType[] types = lm.getLaunchConfigurationTypes();
					Set<Set<String>> modes = null;
					Set<String> mode = null;
					for (int i = 0; i < types.length; i++) {
						modes = types[i].getSupportedModeCombinations();
						for (Iterator<Set<String>> iter = modes.iterator(); iter.hasNext();) {
							mode = iter.next();
							types[i].setPreferredDelegate(mode, null);
						}
					}
				} catch (CoreException ce) {
					DebugPlugin.log(ce);
				}
			}
		};
		DebugUIPlugin.getStandardDisplay().asyncExec(runner);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
