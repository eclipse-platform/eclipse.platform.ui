package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

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
}