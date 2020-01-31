/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 490755
 *     Axel Richard (Obeo) - Bug 41353 - Launch configurations prototypes
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

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

	/**
	 * Constructor
	 * @param mode the mode
	 * @param shell the parent shell
	 */
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
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		try {
			if (parentElement instanceof ILaunchConfiguration) {
				if (((ILaunchConfiguration) parentElement).isPrototype()) {
					return ((ILaunchConfiguration) parentElement).getPrototypeChildren().toArray();
				}
			} else if (parentElement instanceof ILaunchConfigurationType) {
				List<ILaunchConfiguration> configs = new ArrayList<>();
				ILaunchConfigurationType type = (ILaunchConfigurationType) parentElement;
				ILaunchConfiguration[] launchConfigurations = getLaunchManager().getLaunchConfigurations(type, ILaunchConfiguration.CONFIGURATION);
				for (ILaunchConfiguration launchConfig : launchConfigurations) {
					if (launchConfig.getPrototype() == null) {
						configs.add(launchConfig);
					}
				}
				configs = filterUniqueLaunchConfigurations(configs);

				ILaunchConfiguration[] prototypes = getLaunchManager().getLaunchConfigurations(type, ILaunchConfiguration.PROTOTYPE);
				Collections.addAll(configs, prototypes);
				return configs.toArray(new ILaunchConfiguration[0]);
			} else {
				return getLaunchManager().getLaunchConfigurationTypes();
			}
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, LaunchConfigurationsMessages.LaunchConfigurationDialog_An_exception_occurred_while_retrieving_launch_configurations_20, e); //
		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ILaunchConfiguration) {
			if (!((ILaunchConfiguration)element).exists()) {
				return null;
			}
			try {
				ILaunchConfiguration prototype = ((ILaunchConfiguration) element).getPrototype();
				if (prototype != null) {
					return prototype;
				} else {
					return ((ILaunchConfiguration) element).getType();
				}
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, LaunchConfigurationsMessages.LaunchConfigurationDialog_An_exception_occurred_while_retrieving_launch_configurations_20, e); //
			}
		} else if (element instanceof ILaunchConfigurationType) {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ILaunchConfiguration) {
			if (((ILaunchConfiguration) element).isPrototype()) {
				try {
					return ((ILaunchConfiguration) element).getPrototypeChildren().size() > 0;
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, LaunchConfigurationsMessages.LaunchConfigurationDialog_An_exception_occurred_while_retrieving_launch_configurations_20, e); //
				}
			}
			return false;
		}
		return getChildren(element).length > 0;
	}

	/**
	 * Return only the launch configuration types that support the current mode AND
	 * are marked as 'public'.
	 *
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		ILaunchConfigurationType[] allTypes = getLaunchManager().getLaunchConfigurationTypes();
		return filterTypes(allTypes).toArray();
	}

	/**
	 * Returns a list containing the given types minus any types that
	 * should not be visible. A type should not be visible if it doesn't match
	 * the current mode or if it matches a disabled activity.
	 *
	 * @param allTypes the types
	 * @return the given types minus any types that should not be visible.
	 */
	private List<ILaunchConfigurationType> filterTypes(ILaunchConfigurationType[] allTypes) {
		List<ILaunchConfigurationType> filteredTypes = new ArrayList<>();
		String mode = getMode();
		LaunchConfigurationTypeContribution contribution;
		for (ILaunchConfigurationType type : allTypes) {
			contribution= new LaunchConfigurationTypeContribution(type);
			if (isVisible(type, mode) && !WorkbenchActivityHelper.filterItem(contribution)) {
				filteredTypes.add(type);
			}
		}
		return filteredTypes;
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

	/**
	 * Returns unique launch configurations from the input list Launch
	 * configurations are unique if the following criteria are fulfilled:
	 * <ol>
	 * <li>The name is unique within the list</li>
	 * <li>The set of attributes (key and value) for the launch configuration is
	 * unique within the list</li>
	 * </ol>
	 *
	 * @param launchConfigurations The list of launch configurations to filter
	 * @return List of unique launch configurations based on criteria above
	 * @throws CoreException If unable to fetch attributes for launch configuration
	 */
	private List<ILaunchConfiguration> filterUniqueLaunchConfigurations(List<ILaunchConfiguration> launchConfigurations) throws CoreException {
		List<ILaunchConfiguration> configs = new ArrayList<>();

		// Create map between name and list of launch configurations with same
		// name
		Map<String, List<ILaunchConfiguration>> configNameMap = new HashMap<>();

		for (ILaunchConfiguration launchConfig : launchConfigurations) {
			String name = launchConfig.getName();
			if (!configNameMap.containsKey(name)) {
				configNameMap.put(name, new ArrayList<>());
			}
			configNameMap.get(name).add(launchConfig);
		}

		// Identify unique configurations
		for (Entry<String, List<ILaunchConfiguration>> entry : configNameMap.entrySet()) {
			List<ILaunchConfiguration> configsWithSameName = entry.getValue();
			if (configsWithSameName.size() == 1) {
				// Only one configuration, add it
				configs.add(configsWithSameName.get(0));
			} else if (configsWithSameName.size() > 1) {
				// More than one configuration detected with same name,
				// verify that they are unique
				Set<Map<String, Object>> seenConfigContent = new HashSet<>();
				for (ILaunchConfiguration config : configsWithSameName) {
					Map<String, Object> content = config.getAttributes();
					if (!seenConfigContent.contains(content)) {
						// Only add the first one with the same content
						seenConfigContent.add(content);
						configs.add(config);
					}
				}
			}
		}

		return configs;
	}
}
