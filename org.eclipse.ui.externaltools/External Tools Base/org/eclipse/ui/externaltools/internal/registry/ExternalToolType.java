package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolRunner;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents the definition of an external
 * tool type.
 */
public final class ExternalToolType implements IAdaptable {
	private ToolTypeWorkbenchAdapter workbenchAdapter;
	
	private String id;
	private String name;
	private IConfigurationElement element;
	private ImageDescriptor imageDescriptor;
	private IExternalToolRunner runner;
	
	/**
	 * Create a new external tool type.
	 * 
	 * @param id the unique identifier of this type
	 * @param name a user readable label for this type
	 * @param element the configuration element for the extension
	 */
	/*package*/ ExternalToolType(String id, String name, IConfigurationElement element) {
		super();
		this.id = id;
		this.name = name;
		this.element = element;
	}

	/* (non-Javadoc)
	 * Method declared on IAdaptable.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			if (workbenchAdapter == null)
				workbenchAdapter = new ToolTypeWorkbenchAdapter();
			return workbenchAdapter;
		}
		return null;
	}

	/**
	 * Returns a short description of this type.
	 */
	public String getDescription() {
		String description = element.getAttribute(ExternalToolTypeRegistry.TAG_DESCRIPTION);
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}
	
	/**
	 * Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the image descriptor for this tool type icon.
	 */
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor == null) {
			IExtension extension = element.getDeclaringExtension();
			IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
			String location = element.getAttribute(ExternalToolTypeRegistry.TAG_ICON);
			if (location != null && location.length() > 0) {
				URL fullPath = pluginDescriptor.find(new Path(location));
				if (fullPath != null) {
					imageDescriptor = ImageDescriptor.createFromURL(fullPath);
				} else {
					try {
						URL installURL = pluginDescriptor.getInstallURL();
						fullPath = new URL(installURL, location);
						imageDescriptor = ImageDescriptor.createFromURL(fullPath);
					} catch (MalformedURLException e) {
					}
				}
			}
			if (imageDescriptor == null)
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return imageDescriptor;
	}
	
	/**
	 * Returns the runner to run external tools of this type
	 * or <code>null</code> if none provided by client.
	 */
	public IExternalToolRunner getRunner() {
		if (runner == null) {
			try {
				runner = (IExternalToolRunner) element.createExecutableExtension(ExternalToolTypeRegistry.TAG_RUN_CLASS);
			} catch (CoreException e) {
				ExternalToolsPlugin.getDefault().getLog().log(e.getStatus());
			}
		}
		return runner;
	}


	/**
	 * Internal workbench adapter implementation
	 */
	private static class ToolTypeWorkbenchAdapter implements IWorkbenchAdapter {
		public Object[] getChildren(Object o) {
			ExternalToolType type = (ExternalToolType)o;
			return ExternalToolsPlugin.getDefault().getToolRegistry(null).getToolsOfType(type.getId());
		}
		
		public ImageDescriptor getImageDescriptor(Object o) {
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		}

		public String getLabel(Object o) {
			return ((ExternalToolType)o).getName();
		}
		
		public Object getParent(Object o) {
			return null;
		}
	}
}
