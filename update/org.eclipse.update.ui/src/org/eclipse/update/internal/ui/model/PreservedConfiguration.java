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
package org.eclipse.update.internal.ui.model;
import org.eclipse.update.configuration.IInstallConfiguration;

public class PreservedConfiguration extends UIModelObject {
	private IInstallConfiguration config;
	
	public PreservedConfiguration(IInstallConfiguration config) {
		this.config = config;
	}
	
	public IInstallConfiguration getConfiguration() {
		return config;
	}
	
	public String toString() {
		return config.getLabel();
	}
	
	public boolean equals(Object object) {
		if (object==null) return false;
		if (object==this) return true;
		if (object instanceof PreservedConfiguration) {
			return ((PreservedConfiguration)object).getConfiguration().equals(config);
		}
		return false;
	}
}
