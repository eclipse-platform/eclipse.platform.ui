/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

/**
 * The IObjectContributionRecord is the object that holds onto the
 * plug-in and local id of a contribution for lookup of mappings.
 */
public interface IObjectContributionRecord {
	/**
	 * Return the local id of this object contribution
	 * @return String
	 */
	public abstract String getLocalId();
	/**
	 * @return the plugin id of this object contribution
	 */
	public abstract String getPluginId();
}