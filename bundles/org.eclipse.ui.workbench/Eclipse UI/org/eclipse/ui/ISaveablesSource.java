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

package org.eclipse.ui;

/**
 * Workbench parts that work in terms of units of saveability should implement
 * this interface in order to provide better integration with workbench
 * facilities like the Save command, prompts to save on part close or shutdown,
 * etc.
 * 
 * @since 3.2
 */
public interface ISaveablesSource {

	/**
	 * Returns the saveables presented by the workbench part. If the return
	 * value of this method changes during the lifetime of this part, the part
	 * must notify an implicit listener using
	 * {@link ISaveablesLifecycleListener#handleLifecycleEvent(SaveablesLifecycleEvent)}.
	 * <p>
	 * The listener must be obtained from the part site by calling
	 * <code>partSite.getService(ISaveablesLifecycleListener.class)</code>.
	 * </p>
	 * 
	 * @return the saveables presented by the workbench part
	 * 
	 * @see ISaveablesLifecycleListener
	 */
	Saveable[] getSaveables();

	/**
	 * Returns the saveables currently active in the workbench part.
	 * <p>
	 * Certain workbench actions, such as Save, target only the active saveables
	 * in the active part. For example, the active saveables could be determined
	 * based on the current selection in the part.
	 * </p>
	 * 
	 * @return the saveables currently active in the workbench part
	 */
	Saveable[] getActiveSaveables();
}
