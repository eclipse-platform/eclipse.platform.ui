/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public interface IConfigurationSiteContext {
	public IInstallConfiguration getInstallConfiguration();
	public IConfigurationSite getConfigurationSite();
}
