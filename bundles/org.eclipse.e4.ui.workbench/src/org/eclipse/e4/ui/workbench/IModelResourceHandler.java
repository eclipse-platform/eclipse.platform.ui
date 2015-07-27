/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tristan Hume - <trishume@gmail.com> -
 *     		Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 *     		Implemented workbench auto-save to correctly restore state in case of crash.
 ******************************************************************************/

package org.eclipse.e4.ui.workbench;

import java.io.IOException;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * This handler allows clients load, create and save model resources
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface IModelResourceHandler {

	/**
	 * Loads an returns the most recent model that was persisted
	 *
	 * @return the most recent model state
	 */
	public Resource loadMostRecentModel();

	/**
	 * Creates a resource with an app Model, used for saving copies of the main app model.
	 *
	 * @param theApp
	 *            the application model to add to the resource
	 * @return a resource with a proper save path with the model as contents
	 */
	public Resource createResourceWithApp(MApplication theApp);

	/**
	 * Saves the model
	 *
	 * @throws IOException
	 *             if storing fails
	 *
	 */
	public void save() throws IOException;

}
