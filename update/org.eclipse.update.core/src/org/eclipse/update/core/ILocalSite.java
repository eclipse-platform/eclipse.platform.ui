package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public interface ILocalSite {
	IInstallConfiguration getCurrentConfiguration();
	IInstallConfiguration [] getConfigurationHistory();

}

