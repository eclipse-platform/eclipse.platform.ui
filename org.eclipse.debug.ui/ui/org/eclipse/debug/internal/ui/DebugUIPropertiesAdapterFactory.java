package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This factory and the IWorkbenchAdapter that it provides exist so that
 * a properties dialog that is realized on a launches view element will have a 
 * title.
 */
/*package*/ class DebugUIPropertiesAdapterFactory implements IAdapterFactory {

	class DebugUIPropertiesAdapter implements IWorkbenchAdapter {
	
		/**
		 * @see IWorkbenchAdapter#getChildren(Object)
		 */
		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		/**
		 * @see IWorkbenchAdapter#getImageDescriptor(Object)
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			return DebugUITools.getDefaultImageDescriptor(object);
		}

		/**
		 * @see IWorkbenchAdapter#getLabel(Object)
		 */
		public String getLabel(Object o) {
			IDebugModelPresentation presentation= DebugUIPlugin.getModelPresentation();
			return presentation.getText(o);
		}

		/**
		 * @see IWorkbenchAdapter#getParent(Object)
		 */
		public Object getParent(Object o) {
			return null;
		}
	}
	
	/**
	 * @see IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object obj, Class adapterType) {
		if (adapterType.isInstance(obj)) {
			return obj;
		}
		if (adapterType == IWorkbenchAdapter.class) {
			if (obj instanceof IDebugElement) {
				return new DebugUIPropertiesAdapter();
			}
			if (obj instanceof IProcess) {
				return new DebugUIPropertiesAdapter();
			}
		}
		return null;
	}

	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {
			IWorkbenchAdapter.class
		};
	}
}

