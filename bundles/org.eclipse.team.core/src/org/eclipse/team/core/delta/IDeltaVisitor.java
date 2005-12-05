/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.delta;

import org.eclipse.core.runtime.CoreException;

/**
 * An objects that visits sync deltas.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements ISyncDeltaVisitor {
 *     public boolean visit(ISyncDelta delta) {
 *         switch (delta.getKind()) {
 *         case IResourceDelta.ADDED :
 *             // handle added resource
 *             break;
 *         case IResourceDelta.REMOVED :
 *             // handle removed resource
 *             break;
 *         case IResourceDelta.CHANGED :
 *             // handle changed resource
 *             break;
 *         }
 *     return true;
 *     }
 * }
 * ISyncDelta rootDelta = ...;
 * rootDelta.accept(new Visitor());
 * </pre>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IDelta#accept(ISyncDeltaVisitor)
 * 
 * @since 3.2
 */
public interface IDeltaVisitor {

	/** 
	 * Visits the given delta.
	 * 
	 * @return <code>true</code> if the delta's children should
	 *		be visited; <code>false</code> if they should be skipped.
	 * @exception CoreException if the visit fails for some reason.
	 */
	public boolean visit(IDelta delta) throws CoreException;
}
