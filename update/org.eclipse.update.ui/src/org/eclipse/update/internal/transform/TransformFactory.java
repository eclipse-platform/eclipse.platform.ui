package org.eclipse.update.internal.transform;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.ui.internal.model.ChecklistJob;

public class TransformFactory implements IAdapterFactory {
	private FeatureTransform ft = new FeatureTransform();

	/**
	 * @see IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object object, Class clazz) {
		if (clazz.equals(ITransform.class)) {
			if (object instanceof IFeature)
		   		return ft;
			if (object instanceof ChecklistJob)
		   		return ft;
		}
		return null;
	}
	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class [] { ITransform.class };
	}
}

