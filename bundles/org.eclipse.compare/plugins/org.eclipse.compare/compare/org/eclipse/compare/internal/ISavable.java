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
package org.eclipse.compare.internal;

import org.eclipse.compare.contentmergeviewer.IFlushable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

/**
 * The precursor to {@link IFlushable}. This interface has been kept around since
 * it was needed by clients in some situations.
 * 
 * @deprecated use {@link IFlushable}
 */
public interface ISavable {
	
	void save(IProgressMonitor pm) throws CoreException;
}
