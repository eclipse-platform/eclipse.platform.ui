/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A description of a single extension registered with the 
 * <code>org.eclipse.team.ui.teamContentProviders</code>
 * extension point.
 * @since 3.2
 */
public interface ITeamContentProviderDescriptor {

	/**
	 * Return the id of the content extension registered with
	 * the <code>org.eclipse.ui.navigator.navigatorContent</code> extension point
	 * that applies to the descriptors model provider.
	 * @return id of the content extension registered with
	 * the <code>org.eclipse.ui.navigator.navigatorContent</code> extension point
	 */
	public abstract String getContentExtensionId();

	/**
	 * Return the id of the model provider to which this content provider applies.
	 * @return the id of the model provider to which this content provider applies
	 */
	public abstract String getModelProviderId();

	/**
	 * Return an image descriptor that can be displayed with this content 
	 * extension. 
	 * @return an image descriptor that can be displayed with this content 
	 * extension
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Return a preference page that can be displayed to configure
	 * the content provider of this extension.
	 * @return a preference page that can be displayed to configure
	 * the content provider of this extension
	 * @throws CoreException
	 */
	public abstract IPreferencePage createPreferencePage() throws CoreException;

}