/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * This class represents provisional API. A provider is not required to implement this API.
 * Implementers, and those who reference it, do so with the awareness that this class may be
 * removed or substantially changed at future times without warning.
 * <p>
 * This class represents things you can ask/do with a type of provider. This
 * is in the absence of a project, as opposed to RepositoryProvider which
 * requires a concrete project in order to be instantiated.
 * <p>
 * A repository provider type class is asscoaited with it's provider ID along with it's 
 * corresponding repository provider class. To add a
 * repository provider type and have it registered with the platform, a client
 * must minimally:
 * <ol>
 * 	<li>extend <code>RepositoryProviderType</code>
 * 	<li>add the typeClass field to the repository extension in <code>plugin.xml</code>. 
 *     Here is an example extension point definition:
 * 
 *  <code>
 *	<br>&lt;extension point="org.eclipse.team.core.repository"&gt;
 *  <br>&nbsp;&lt;repository
 *  <br>&nbsp;&nbsp;class="org.eclipse.myprovider.MyRepositoryProvider"
 *  <br>&nbsp;&nbsp;typeClass="org.eclipse.myprovider.MyRepositoryProviderType"
 *  <br>&nbsp;&nbsp;id="org.eclipse.myprovider.myProviderID"&gt;
 *  <br>&nbsp;&lt;/repository&gt;
 *	<br>&lt;/extension&gt;
 *  </code>
 * </ol></p>
 * 
 * <p>
 * Once a repository provider type is registered with Team, then you
 * can access the singleton instance of the class by invoking <code>RepositoryProviderType.getProviderType()</code>.
 * </p>
 * 
 * @see RepositoryProviderType.getProviderType(String)
 * 
 * @since 2.1
 */

public abstract class RepositoryProviderType {
	private static Map allProviderTypes = new HashMap();
	
	private String id;

	public RepositoryProviderType() {
	}

	/**
	 * Return the RepositoryProviderType for the given provider ID.
	 * 
	 * @param id the ID of the provider
	 * @return RepositoryProviderType
	 * 
	 * @see getID()
	 */
	public static RepositoryProviderType getProviderType(String id) {
		RepositoryProviderType type = (RepositoryProviderType) allProviderTypes.get(id);

		if(type != null)
			return type;
			
		//If there isn't one in the table, we'll try to create one from the extension point
		//Its possible that newProviderType() will return null, but in that case it will have also logged the error	so just return the result
		return newProviderType(id);
	}
	
	private void setID(String id) {
		this.id = id;
	}
	
	private static RepositoryProviderType newProviderType(String id) {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.REPOSITORY_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						
						if (extensionId != null && extensionId.equals(id)) {
							try {
								RepositoryProviderType providerType;
								//Its ok not to have a typeClass extension.  In this case, a default instance will be created.
								if(configElements[j].getAttribute("typeClass") == null) {
									providerType = new DefaultRepositoryProviderType();
								} else {
									providerType = (RepositoryProviderType) configElements[j].createExecutableExtension("typeClass"); //$NON-NLS-1$
								}
								
								providerType.setID(id);
								allProviderTypes.put(id, providerType);
								return providerType;
							} catch (CoreException e) {
								TeamPlugin.log(e.getStatus());
							} catch (ClassCastException e) {
								String className = configElements[j].getAttribute("typeClass"); //$NON-NLS-1$
								TeamPlugin.log(IStatus.ERROR, Policy.bind("RepositoryProviderType.invalidClass", id, className), e); //$NON-NLS-1$
							}
							return null;
						}
					}
				}
			}		
		}
		return null;
	}	
	
	/**
	 * Answer the id of this provider type. The id will be the repository
	 * provider type's id as defined in the provider plugin's plugin.xml.
	 * 
	 * @return the id of this provider type
	 */
	public final String getID() {
		return this.id;
	}

	/**
	 * Answers the ProjectSetCapability that is intended to implement methods to import and
	 * create project sets.  If the provider doesn't wish to provide this
	 * feature, return null.
	 * <p>
	 * Note that at the current time, the <code>IProjectSetSerializer</code> is still used to import
	 * and export project sets. The <code>ProjectSetCapability</code> only provides a mechanism
	 * by which repository providers can be notified when a project set is created and exported.
	 * In the future, it is intended that IProjectSetSerializer be replaced by <code>IProjectSetSerializer</code>.
	 * 
	 * @return ProjectSetCapability
	 */
	
	public ProjectSetCapability getProjectSetCapability() {
		return null;
	}
}
