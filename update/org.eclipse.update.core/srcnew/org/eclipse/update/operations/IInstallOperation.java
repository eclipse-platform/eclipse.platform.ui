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
import org.eclipse.update.internal.operations.*;

/**
 * IConfigOperation
 */
public interface IInstallOperation extends ISingleOperation {
	public FeatureHierarchyElement2[] getOptionalElements();
	public IFeatureReference[] getOptionalFeatures();
}