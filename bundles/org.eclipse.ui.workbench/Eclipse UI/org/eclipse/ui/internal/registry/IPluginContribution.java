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
package org.eclipse.ui.internal.registry;

/**
 * An interface that descriptor classes should inherit in addition to their
 * descriptor interface. This indicates that they may or may not originate from
 * a plugin contribution.
 * 
 * @since 3.0
 */
public interface IPluginContribution {
    
	/**
	 * @return whether or not this contribution originated from a plugin.
	 */
	public boolean fromPlugin();

	/**
	 * @return the local id of the contribution. Must not be <code>null</code>.
	 */
	public String getLocalId();

	/**
	 * @return the id of the originating plugin. Must not be <code>null</code>
	 *         if <code>fromPlugin</code> returns <code>true</code>.
	 */
	public String getPluginId();
}
