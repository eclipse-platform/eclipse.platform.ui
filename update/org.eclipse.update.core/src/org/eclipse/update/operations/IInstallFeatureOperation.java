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
 * An installation operation. This operation should not be executed by itself, it should be 
 * aggregated into a IBatchOperation, together with other IInstallOperations, so that the
 * validation checks are done on the group, not per installation job.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public interface IInstallFeatureOperation extends IFeatureOperation {
	/**
	 * Returns the list of optional features to be installed.
	 * @return
	 */
	public IFeatureReference[] getOptionalFeatures();
}