/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.dependencies;

/**
 * Collects changes that happened during a resolution operation. 
 * Not to be implemented by clients.
 * @see IElementChange
 */
public interface IResolutionDelta {
 	IElementChange[] getAllChanges();
	IElementChange getChange(Object id, Object versionId);
}

