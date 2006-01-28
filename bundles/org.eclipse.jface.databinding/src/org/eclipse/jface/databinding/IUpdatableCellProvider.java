/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding;

/**
 * This guy fires FUNCTION_CHANGED events only, and only for the cell values.
 * 
 * @since 3.2
 */
public interface IUpdatableCellProvider extends ICellProvider, IUpdatable {
	
	public IReadableSet getReadableSet();

}
