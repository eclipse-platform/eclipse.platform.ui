/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.update.operations;

import org.eclipse.update.core.*;

/**
 * An installation operation.
 * @since 3.0
 */
public interface IInstallFeatureOperation extends IFeatureOperation {
	/**
	 * Returns the list of optional features to be installed.
	 * @return
	 */
	public IFeatureReference[] getOptionalFeatures();
}