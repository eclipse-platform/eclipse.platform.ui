/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.runtime.CoreException;

public interface IUnifiedTreeVisitor {
/**
 * Returns true to visit the members of this node and false otherwise.
 */
public boolean visit(UnifiedTreeNode node) throws CoreException;
}
