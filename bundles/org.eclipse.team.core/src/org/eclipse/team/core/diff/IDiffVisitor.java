/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.diff;

import org.eclipse.core.runtime.CoreException;

/**
 * An objects that visits diffs in a diff tree.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements IDiffVisitor {
 *     public boolean visit(IDiffNode diff) {
 *         switch (diff.getKind()) {
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
 * IDiffTree tree = ...;
 * tree.accept(new Visitor());
 * </pre>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see IDiffTree#accept(org.eclipse.core.runtime.IPath, IDiffVisitor, int)
 * 
 * @since 3.2
 */
public interface IDiffVisitor {

	/** 
	 * Visits the given diff.
	 * @param diff the diff being visited
	 * @return <code>true</code> if the diff's children should
	 *		be visited; <code>false</code> if they should be skipped.
	 * @exception CoreException if the visit fails for some reason.
	 */
	public boolean visit(IDiffNode diff) throws CoreException;
}
