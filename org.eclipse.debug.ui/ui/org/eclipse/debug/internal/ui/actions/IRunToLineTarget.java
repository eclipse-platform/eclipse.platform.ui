/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * <p>
 * EXPERIMENTAL
 * </p>
 * @since 3.0
 */
public interface IRunToLineTarget {
	
	public boolean canPerformRunToLine();
	
	public void runToLine(ISuspendResume target) throws CoreException;
	
	public void dispose();
	
}
