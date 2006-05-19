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
package org.eclipse.team.examples.model.ui;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.core.mapping.IResourceMappingMerger;
import org.eclipse.team.examples.model.ModelObject;
import org.eclipse.team.examples.model.mapping.*;
import org.eclipse.team.examples.model.ui.mapping.CompareAdapter;
import org.eclipse.team.ui.mapping.ISynchronizationCompareAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AdapterFactory implements IAdapterFactory {

	private IWorkbenchAdapter modelAdapter = new ModelWorkbenchAdapter();
	private ModelMerger modelMerger;
	private CompareAdapter compareAdapter;
	
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof ModelObject)
			return modelAdapter;
		if (adapterType == ResourceMapping.class && adaptableObject instanceof ModelObject)
			return ModelResourceMapping.create((ModelObject)adaptableObject);
		if (adapterType == IResourceMappingMerger.class && adaptableObject instanceof ExampleModelProvider) {
			if (modelMerger == null) {
				modelMerger = new ModelMerger((ExampleModelProvider)adaptableObject);
			}
			return modelMerger;
		}
		if (adapterType == ISynchronizationCompareAdapter.class && adaptableObject instanceof ExampleModelProvider) {
			if (compareAdapter == null) {
				compareAdapter = new CompareAdapter((ExampleModelProvider)adaptableObject);
			}
			return compareAdapter;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, ResourceMapping.class, IResourceMappingMerger.class, ISynchronizationCompareAdapter.class };
	}

}
