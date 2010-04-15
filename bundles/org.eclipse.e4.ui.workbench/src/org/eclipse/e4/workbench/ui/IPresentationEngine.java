/*******************************************************************************
 * Copyright (c) 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

/**
 * Engine the workbench delegates the rendering of the ui to a service
 */
public interface IPresentationEngine {
	/**
	 * The ID to access the service in the {@link IEclipseContext}
	 */
	public static final String SERVICE_NAME = IPresentationEngine.class.getName();

	/**
	 * Create the UI element for this model element
	 * 
	 * @param element
	 *            the model element
	 * @param parent
	 *            the parent
	 * @return the created UI element
	 */
	public Object createGui(MUIElement element, Object parent);

	/**
	 * Create UI element which is at the top of the widget hierarchy
	 * 
	 * @param element
	 *            the model element
	 * @return the create UI element
	 */
	public Object createGui(MUIElement element);

	/**
	 * Remove the UI element create for this model element
	 * 
	 * @param element
	 *            the model element whose UI element should removed
	 */
	public void removeGui(MUIElement element);

	/**
	 * Run the UI. This method is responsible for creating the initial UI and (if necessary)
	 * spinning the event loop for the life of the application.
	 * 
	 * @param uiRoot
	 * @param appContext
	 * 
	 * @return The application's return value
	 */
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext);

	/**
	 * Shuts down the rendering engine
	 */
	public void stop();
}
