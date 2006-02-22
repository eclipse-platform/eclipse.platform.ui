package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

public class TestExtensionAdapterFactory implements IAdapterFactory {
	
	private static final Class IRESOURCE_TYPE = IResource.class;
	private static final Class IFILE_TYPE = IFile.class;

	private static final Class[] ADAPTED_TYPES = new Class[] { IRESOURCE_TYPE, IFILE_TYPE };

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		 if(IRESOURCE_TYPE == adapterType || IFILE_TYPE == adapterType) {
			 TestExtensionTreeData data = (TestExtensionTreeData) adaptableObject;
			 return data.getFile();
		 }
		 return null;
	}

	public Class[] getAdapterList() { 
		return ADAPTED_TYPES;
	}

}
