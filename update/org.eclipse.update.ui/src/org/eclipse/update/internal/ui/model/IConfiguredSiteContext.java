/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;

/**
 * @version 	1.0
 * @author
 */
public interface IConfiguredSiteContext {
	public IInstallConfiguration getInstallConfiguration();
	public IConfiguredSite getConfigurationSite();
}
