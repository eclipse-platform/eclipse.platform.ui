/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.ui.IMemento;

/**
 * A {@link ModelProvider} adaptation for persisting the model elements
 * associated with a model synchronization.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IResourceMappingPersistenceAdapter {

	/**
	 * Save the given resource mappings from this adapters 
	 * model provider into the given memento in a form
	 * that can be restored at a future time.
	 * @param mappings the resource mappings to save
	 * @param memento the memento where the mappings should be saved
	 */
	public void save(ResourceMapping[] mappings, IMemento memento);
	
	/**
	 * Restore the previosuly saved resource mappings.
	 * @param memento a memento
	 * @return the mappings restored from the given memento
	 */
	public ResourceMapping[] restore(IMemento memento);
}
