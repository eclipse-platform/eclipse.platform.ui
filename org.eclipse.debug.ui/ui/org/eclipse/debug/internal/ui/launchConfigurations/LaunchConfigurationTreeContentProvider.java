/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import java.util.ArrayList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

/**
 * Content provider for representing launch configuration types & launch configurations in a tree.
 * 
 * @since 2.1
 */
public class LaunchConfigurationTreeContentProvider implements ITreeContentProvider {

	/**
	 * Empty Object array
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];	
	
	/**
	 * The mode in which the tree is being shown, one of <code>RUN_MODE</code> 
	 * or <code>DEBUG_MODE</code> defined in <code>ILaunchManager</code>.
	 * If this is <code>null</code>, then it means both modes are being shown.
	 */
	private String fMode;
	
	/**
	 * The Shell context
	 */
	private Shell fShell;
	
	public LaunchConfigurationTreeContentProvider(String mode, Shell shell) {
		setMode(mode);
		setShell(shell);
	}

	/**
	 * Actual launch configurations have no children.  Launch configuration types have
	 * all configurations of that type as children, minus any configurations that are 
	 * marked as private.
	 * <p>
	 * In 2.1, the <code>category</code> attribute was added to launch config
	 * types. The debug UI only displays those configs that do not specify a
	 * category.
	 * </p>
	 * 
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ILaunchConfiguration) {
			return EMPTY_ARRAY;
		} else if (parentElement instanceof ILaunchConfigurationType) {
			try {
				ILaunchConfigurationType type = (ILaunchConfigurationType)parentElement;
				return getLaunchManager().getLaunchConfigurations(type);
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations_20"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			return getLaunchManager().getLaunchConfigurationTypes();
		}
		return EMPTY_ARRAY;
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof ILaunchConfiguration) {
			if (!((ILaunchConfiguration)element).exists()) {
				return null;
			}
			try {
				return ((ILaunchConfiguration)element).getType();
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations_20"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if (element instanceof ILaunchConfigurationType) {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof ILaunchConfiguration) {
			return false;
		} else {
			return getChildren(element).length > 0;
		}
	}

	/**
	 * Return only the launch configuration types that support the current mode AND
	 * are marked as 'public'.
	 * 
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		ILaunchConfigurationType[] allTypes = getLaunchManager().getLaunchConfigurationTypes();
		ArrayList list = new ArrayList(allTypes.length);
		String mode = getMode();
		for (int i = 0; i < allTypes.length; i++) {
			ILaunchConfigurationType configType = allTypes[i];
			if (isVisible(configType, mode)) {
				list.add(configType);
			}
		}			
		return list.toArray();
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Return <code>true</code> if the specified launch configuration type should
	 * be visible in the specified mode, <code>false</code> otherwise.
	 */
	private boolean isVisible(ILaunchConfigurationType configType, String mode) {
		if (!configType.isPublic()) {
			return false;
		}
		if (mode == null) {
			return true;
		}
		return configType.supportsMode(mode);
	}

	/**
	 * Convenience method to get the singleton launch manager.
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Write accessor for the mode value
	 */
	private void setMode(String mode) {
		fMode = mode;
	}
	
	/**
	 * Read accessor for the mode value
	 */
	private String getMode() {
		return fMode;
	}

	/**
	 * Write accessor for the shell value
	 */
	private void setShell(Shell shell) {
		fShell = shell;
	}
	
	/**
	 * Read accessor for the shell value
	 */
	private Shell getShell() {
		return fShell;
	}
}
