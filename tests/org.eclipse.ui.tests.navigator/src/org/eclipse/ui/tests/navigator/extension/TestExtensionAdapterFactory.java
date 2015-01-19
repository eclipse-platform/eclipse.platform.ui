/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

public class TestExtensionAdapterFactory implements IAdapterFactory {
	
	private static final Class IRESOURCE_TYPE = IResource.class;
	private static final Class IFILE_TYPE = IFile.class;

	private static final Class[] ADAPTED_TYPES = new Class[] { IRESOURCE_TYPE, IFILE_TYPE };

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		 if(IRESOURCE_TYPE == adapterType || IFILE_TYPE == adapterType) {
			 TestExtensionTreeData data = (TestExtensionTreeData) adaptableObject;
			 return data.getFile();
		 }
		 return null;
	}

	@Override
	public Class[] getAdapterList() { 
		return ADAPTED_TYPES;
	}

}
