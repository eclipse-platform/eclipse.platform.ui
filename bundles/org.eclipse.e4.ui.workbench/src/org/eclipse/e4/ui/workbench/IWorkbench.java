/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;

/**
 * A running instance of the workbench.
 * 
 * This instance is published through:
 * <ul>
 * <li>the {@link IEclipseContext} of the application</li>
 * <li>the OSGi-Service-Registry</lI>
 * </ul>
 * <b>It is possible that there are multiple active {@link IWorkbench} instances in one
 * OSGi-Instance</b>
 */
public interface IWorkbench {
	/**
	 * @return unique id of the instance
	 */
	public String getId();

	/**
	 * @return the application model driving the workbench
	 */
	public MApplication getApplication();

	/**
	 * Close the workbench instance
	 * 
	 * @return <code>true</code> if the shutdown succeeds
	 */
	public boolean close();

}
