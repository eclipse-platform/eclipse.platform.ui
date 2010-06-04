/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench;

import java.io.IOException;
import org.eclipse.emf.ecore.resource.Resource;

/**
 *
 */
public interface IModelResourceHandler {

	/**
	 * @return the most recent model state
	 */
	Resource loadMostRecentModel();

	/**
	 * @throws IOException
	 *             if storing fails
	 * 
	 */
	void save() throws IOException;

}
