/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SectionDescriptor {
	private IConfigurationElement config;
	
	public SectionDescriptor(IConfigurationElement config) {
		this.config = config;
	}
	
	public String getId() {
		return config.getAttribute("id");
	}
	
	public String getName() {
		return config.getAttribute("name");
	}
	public IConfigurationElement getConfig() {
		return config;
	}
}
