/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;


/**
 * An objects that visits model deltas.
 * 
 * @since 3.3
 */
public interface IModelDeltaVisitor {
	
	/** 
	 * Visits the given model delta.
	 * 
	 * @param delta the delta to visit
	 * @param depth depth in the delta where 0 == root node
	 * @return <code>true</code> if the model delta's children should
	 *		be visited; <code>false</code> if they should be skipped.
	 */
	public boolean visit(IModelDelta delta, int depth);

}
