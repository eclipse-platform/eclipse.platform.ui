package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

public class PreservedConfiguration extends ModelObject {
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
}