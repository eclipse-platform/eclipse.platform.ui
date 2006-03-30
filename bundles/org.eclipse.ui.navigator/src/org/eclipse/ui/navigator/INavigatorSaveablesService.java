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

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;

/**
 * @since 3.2
 *
 */
public interface INavigatorSaveablesService {
	
	/**
	 * @param source
	 * @param viewer
	 * @param listener
	 * @return the helper
	 */
	public ISaveablesSourceHelper createHelper(ISaveablesSource source, StructuredViewer viewer, ISaveablesLifecycleListener listener);

}
