package org.eclipse.core.tests.internal.plugin.a;

import org.eclipse.core.runtime.*;

public abstract class ConfigurableExtension extends BaseExtension implements IExecutableExtension {

	public IConfigurationElement config = null;
	public String propertyName = null;
	public Object data = null;
	
public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {

	this.config = config;
	this.propertyName = propertyName;
	this.data = data;
	
}
}