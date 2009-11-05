/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor)- initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IFileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.IPath;

/**
 * @since 3.6
 *
 */

public abstract class UIResourceFilterDescription {
	/**
	 * @return
	 */
	abstract public IPath getPath();
	/**
	 * @return
	 */
	abstract public IProject getProject();
	/**
	 * @return
	 */
	abstract public int getType();
	/**
	 * @return
	 */
	abstract public IFileInfoMatcherDescription getFileInfoMatcherDescription();
	
	/**
	 * @param iResourceFilterDescription
	 * @return a UIResourceFilterDescription
	 */
	public static UIResourceFilterDescription wrap(
			final IResourceFilterDescription iResourceFilterDescription) {
		return new UIResourceFilterDescription() {
			public IFileInfoMatcherDescription getFileInfoMatcherDescription() {
				return iResourceFilterDescription.getFileInfoMatcherDescription();
			}
			public IPath getPath() {
				return iResourceFilterDescription.getPath();
			}
	
			public IProject getProject() {
				return iResourceFilterDescription.getProject();
			}
	
			public int getType() {
				return iResourceFilterDescription.getType();
			}
		};
	}
}