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
package org.eclipse.team.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.core.*;
import org.eclipse.team.internal.core.registry.DeploymentProviderDescriptor;

/**
 * A deployment provider allows synchronization of workspace resources with a remote location. At a minimum
 * it allows pushing resources in the workspace to a remote location and pulling resources from a
 * remote location into the workspace.
 * <p>
 * The difference between a deployment provider and repository provider is the following:
 * <ul>
 * <li>a deployment provider doesn't have full control of workspace resources whereas the repository
 * provider can hook into the IMoveDeleteHook and IFileModificationValidator.
 * <li>multiple deployment providers can be mapped to the same folder whereas there is only one
 * repository provider per project.
 * <li>a deployment provider can be mapped to any folder
 * whereas the repository provider must be mapped at the project.
 * </ul>
 * </p>
 * <p>
 * Deployment providers can be dfined in the plugin manifest using the following XML.
 * <pre>
 *    &gt;extension
         point="org.eclipse.team.core.deployment"&lt;
      &gt;deployment
            name="Example Deployment Provider"
            class="org.eclipse.team.internal.example.DeploymentProviderClass"
            id="org.eclipse.team.example.DeploymentProvider"&lt;
      &gt;/deployment&lt;
   &gt;/extension&lt;
   </pre>
 * @see RepositoryProvider
 * @see IDeploymentProviderManager
 * @since 3.0
 */
public abstract class DeploymentProvider implements IExecutableExtension, IAdaptable {
	
	private String id;
	private IContainer container;
	private String name;
	
	/**
	 * Returns the id of the deployment provider as defined in the plugin manifest
	 * @return the id
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Returns the name of the deployment provider as defined in the plugin manifest.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Return the container (folder or project) that this provider is mapped to.
	 * @return a folder or project
	 */
	public IContainer getMappedContainer() {
		return this.container;
	}
	
	/**
	 * Method that is invoked when a deployment provider is first mapped to its folder
	 * using <code>IDeploymentProviderManager#map</code>.
	 * Mappings are persisted accross workbench invocations. However, this method is
	 * only invoked when the provider is mapped. The <code>restoreState</code> method
	 * is invoked on subsequent workbench invocations.
	 */
	abstract public void init();
	
	/**
	 * Method that is invoked when a providers is unmapped from a folder using
	 * <code>IDeploymentProviderManager#unmap</code>.
	 */
	abstract public void dispose();
	
	/**
	 * Method that is invoked after a deployment provider is first mapped or recreated on workbench
	 * invocation. This method is invoked before <code>init</code> or <code>restoreState</code>. 
	 * This method is not intended to be called by clients.
	 * @param container the container the provider is mapped to
	 */
	public final void setContainer(IContainer container) {
		this.container = container;
	}
	
	abstract public void saveState(IMemento memento);
	
	abstract public void restoreState(IMemento memento);
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// TODO: This doesn't work well since the Provider is created programmatically
		// when initially mapped
		this.id = config.getAttribute(DeploymentProviderDescriptor.ATT_ID);
		this.name = config.getAttribute(DeploymentProviderDescriptor.ATT_NAME);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {		
		return null;
	}
	
	/**
	 * Returns whether a resource can be mapped to multiple deployment providers
	 * of this type. Even if this method returns <code>false</code>, a resource can 
	 * still be mapped to multiple providers whose id differs. By default,
	 * multiple mappings are not supported. Subclasses must override this method
	 * to change this behavior.
	 * @return whether multiple mappings to providers of this type are supported
	 */
	public boolean isMultipleMappingsSupported() {
		return false;
	}
}