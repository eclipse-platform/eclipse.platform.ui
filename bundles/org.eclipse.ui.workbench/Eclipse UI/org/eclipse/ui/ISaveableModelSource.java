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
 */
public interface ISaveableModelSource {

	/**
	 * Returns the saveable models presented by the workbench part.
	 * 
	 * @return the saveable models presented by the workbench part
	 */
	ISaveableModel[] getModels();

	/**
	 * Returns the saveable models currently active in the workbench part.
	 * <p>
	 * Certain workbench actions, such as Save, target only the active models in
	 * the active part.
	 * </p>
	 * 
	 * @return the saveable models currently active in the workbench part
	 */
	ISaveableModel[] getActiveModels();
}
