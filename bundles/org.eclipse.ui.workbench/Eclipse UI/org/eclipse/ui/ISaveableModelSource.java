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
 * Workbench parts that work in terms of saveable models should implement this
 * interface in order to provide better integration with workbench facilities
 * like the Save command, prompts to save on part close or shutdown, etc.
 * 
 * @since 3.2
 * @deprecated replaced by {@link ISaveablesSource}
 */
public interface ISaveableModelSource {

	/**
	 * Returns the saveable models presented by the workbench part. If the
	 * return value of this method changes during the lifetime of this part, the
	 * model manager must be notified about these changes by calling
	 * {@link ISaveableModelManager#handleModelLifecycleEvent(ModelLifecycleEvent)}.
	 * <p>
	 * The model manager is available as a service from the part site, by
	 * calling <code>partSite.getService(ISaveableModelManager.class)</code>.
	 * </p>
	 * 
	 * @return the saveable models presented by the workbench part
	 * 
	 * @see ISaveableModelManager
	 */
	ISaveableModel[] getModels();

	/**
	 * Returns the saveable models currently active in the workbench part.
	 * <p>
	 * Certain workbench actions, such as Save, target only the active models in
	 * the active part. For example, the active saveable models could be
	 * determined based on the current selection in the part.
	 * </p>
	 * 
	 * @return the saveable models currently active in the workbench part
	 */
	ISaveableModel[] getActiveModels();
}
