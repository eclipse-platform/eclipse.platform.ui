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
