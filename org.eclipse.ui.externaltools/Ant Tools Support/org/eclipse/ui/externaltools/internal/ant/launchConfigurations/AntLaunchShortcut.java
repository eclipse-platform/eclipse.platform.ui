package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 *
 */
public class AntLaunchShortcut implements ILaunchShortcut {

	/**
	 * Constructor for AntLaunchShortcut.
	 */
	public AntLaunchShortcut() {
		super();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			Object object = structuredSelection.getFirstElement();
			if (object instanceof IFile) {
				IFile file = (IFile)object;
				launch(file, mode);
				return;
			}
		}
		antFileNotFound();
	}
	
	/**
	 * Inform the user that an ant file was not found to run.
	 */
	protected void antFileNotFound() {
		reportError("Unable to determine which Ant file to run.", null);
	}
	
	/**
	 * Launch the given file in the specified mode.
	 * 
	 * @param file
	 * @param mode
	 */
	protected void launch(IFile file, String mode) {
		if ("xml".equalsIgnoreCase(file.getFileExtension())) {
			if (verifyMode(mode)) {
				ILaunchConfiguration configuration= null;
				List configurations = findExistingLaunchConfigurations(file);
				if (configurations.isEmpty()) {
					configuration = createDefaultLaunchConfiguration(file);
					if (configuration != null) {
						DebugUITools.openLaunchConfigurationDialogOnGroup(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), new StructuredSelection(configuration), IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
					}
				} else {
					if (configurations.size() == 1) {
						configuration= (ILaunchConfiguration)configurations.get(0);
					} else {
						configuration= chooseConfig(configurations);
					}
					if (configuration != null) {
						DebugUITools.launch(configuration, mode);
					}
				}
			}			
		} else {
			antFileNotFound();
		}
	}
	
	/**
	 * Creates and returns a default launch configuration for the given file.
	 * 
	 * @param file
	 * @return default launch configuration
	 */
	protected ILaunchConfiguration createDefaultLaunchConfiguration(IFile file) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		IPath path = file.getFullPath();
		if (path.segmentCount() > 2) {
			path = path.removeFirstSegments(path.segmentCount() - 2);
		}
		StringBuffer buffer = new StringBuffer();
		String[] segments = path.segments();
		for (int i = 0; i < segments.length; i++) {
			String string = segments[i];
			buffer.append(string);
			buffer.append(" ");
		}
		String name = buffer.toString().trim();
		name= manager.generateUniqueLaunchConfigurationNameFrom(name);
		try {
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);
			StringBuffer buf = new StringBuffer();
			ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, file.getFullPath().toString(), buf);
			workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, buf.toString());
			workingCopy.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, true);
			
			// set default for common settings
			CommonTab tab = new CommonTab();
			tab.setDefaults(workingCopy);
			tab.dispose();
			
			return workingCopy.doSave();
		} catch (CoreException e) {
			reportError(MessageFormat.format("An exception occurred while creating a default Ant launch configuration for {0}", new String[]{file.toString()}), e);
		}
		return null;
	}
	
	/**
	 * Returns a list of existing launch configuration for the given file.
	 * 
	 * @param file
	 * @return list of launch configurations
	 */
	protected List findExistingLaunchConfigurations(IFile file) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		List validConfigs= new ArrayList();
		if (type != null) {
				ILaunchConfiguration[] configs = null;
				try {
					configs = manager.getLaunchConfigurations(type);
				} catch (CoreException e) {
					reportError("An exception occurred while retrieving Ant launch configurations.", e);
				}
				if (configs != null && configs.length > 0) {
					IPath filePath = file.getLocation();
					ExpandVariableContext context = ExternalToolsUtil.getVariableContext();
					for (int i = 0; i < configs.length; i++) {
						ILaunchConfiguration configuration = configs[i];
						IPath location;
						try {
							location = ExternalToolsUtil.getLocation(configuration, context);
							if (filePath.equals(location)) {
								validConfigs.add(configuration);
							}
						} catch (CoreException e) {
							// error occurred in variable expand - ignore
						}
					}
				}
		}
		return validConfigs;
	}
	
	/**
	 * Prompts the user to choose from the list of given launch configurations
	 * and returns the config the user choose of <code>null</code> if the user
	 * pressed Cancel or if the given list is empty.
	 */
	private ILaunchConfiguration chooseConfig(List configs) {
		if (configs.isEmpty()) {
			return null;
		}
		ILabelProvider labelProvider = DebugUIPlugin.getModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(Display.getCurrent().getActiveShell(), labelProvider);
		dialog.setElements((ILaunchConfiguration[]) configs.toArray(new ILaunchConfiguration[configs.size()]));
		dialog.setTitle("Ant Configuration Selection");
		dialog.setMessage("Choose an ant configuration to run");
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == ElementListSelectionDialog.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}
	
	/**
	 * Verifies the mode is supported
	 * 
	 * @param mode
	 * @return boolean
	 */
	protected boolean verifyMode(String mode) {
		if (!mode.equals(ILaunchManager.RUN_MODE)) {
			reportError("Ant scripts only support 'run' mode.", null);
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IFile file = (IFile)input.getAdapter(IFile.class);
		if (file != null) {
			launch(file, mode);
			return;
		}
		antFileNotFound();
	}
	
	protected void reportError(String message, Throwable throwable) {
		IStatus status = null;
		if (throwable instanceof CoreException) {
			status = ((CoreException)throwable).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, throwable);
		}
		ErrorDialog.openError(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), "Error", message, status);
	}

}
