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
 * Provisional.
 * 
 * This class represents things you can ask/do with a type of provider. This
 * is in the absence of a project, as opposed to RepositoryProvider which
 * requires a concrete project in order to be instantiated.
 */

public abstract class RepositoryProviderType {
	private static Map allProviderTypes = new HashMap();
	
	private String id;

	/**
	 * Constructor for RepositoryProviderType.
	 */
	public RepositoryProviderType() {
	}

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
	 * Answer the id of this provider instance. The id will be the repository
	 * provider type's id as defined in the provider plugin's plugin.xml.
	 * 
	 * @return the nature id of this provider
	 */
	public final String getID() {
		return this.id;
	}

	/**
	 * Answers the ProjectSetCapability that implements methods to import and
	 * create project sets.  If the provider doesn't wish to provide this
	 * feature, return null.
	 * 
	 * @return ProjectSetCapability
	 */
	
	public ProjectSetCapability getProjectSetCapability() {
		return null;
	}
}
