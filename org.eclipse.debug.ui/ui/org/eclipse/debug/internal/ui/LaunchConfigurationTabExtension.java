/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.internal.core.IConfigurationElementConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Provides a proxy to a launchConfigurationTabs extension point
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This method has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 * @since 3.3
 */
public final class LaunchConfigurationTabExtension {

	/**
	 * The configuration element backing this proxy
	 */
	IConfigurationElement fElement = null;
	
	/**
	 * Constructor
	 * @param element the <code>IConfigurationElement</code> for this proxy
	 */
	public LaunchConfigurationTabExtension(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns the unique id ofthe tab
	 * @return the unique id of the tab
	 */
	public String getIdentifier() {
		return fElement.getAttribute(IConfigurationElementConstants.ID);
	}

	/**
	 * Returns the human readable name for the tab, not to be confused with the name that appears on the tab itself
	 * @return the name of the tab
	 */
	public String getName() {
		return fElement.getAttribute(IConfigurationElementConstants.NAME);
	}

	/**
	 * Returns the instantiated class of this tab
	 * @return the instantiated class of this tab
	 */
	public ILaunchConfigurationTab getTab() {
		try {
			Object object = fElement.createExecutableExtension(IConfigurationElementConstants.CLASS);
			if(object instanceof ILaunchConfigurationTab) {
				return (ILaunchConfigurationTab) object;
			}
		} catch (CoreException e) {DebugUIPlugin.log(e);}
		return null;
	}

	/**
	 * Returns the unique id of the <code>ILaunchConfigurationTabGroup</code> that this tab contributes to
	 * @return the id of the <code>ILaunchConfigurationTabGroup</code> this tab conributes to
	 */
	public String getTabGroupId() {
		return fElement.getAttribute(IConfigurationElementConstants.GROUP);
	}
	
	/**
	 * This method returns the id of the tab that this tab should be placed immediately after.
	 * @return the id of the relative tab or <code>null</code> if one has not been specified
	 * 
	 * @since 3.3
	 * 
	 * EXPERIMENTAL
	 */
	public String getRelativeTabId() {
		IConfigurationElement[] elems = fElement.getChildren(IConfigurationElementConstants.PLACEMENT);
		if(elems.length == 1) {
			return elems[0].getAttribute(IConfigurationElementConstants.AFTER);
		}
		return null;
	}

}
