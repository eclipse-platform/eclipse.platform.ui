package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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