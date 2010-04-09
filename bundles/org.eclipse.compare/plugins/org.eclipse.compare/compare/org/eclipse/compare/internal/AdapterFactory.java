package org.eclipse.compare.internal;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

public class AdapterFactory implements IAdapterFactory {

	public Object getAdapter(final Object adaptableObject, Class adapterType) {
		if (IContributorResourceAdapter.class.equals(adapterType)
				&& adaptableObject instanceof CompareEditorInput) {
			return new IContributorResourceAdapter() {
				public IResource getAdaptedResource(IAdaptable adaptable) {
					Object ei = ((CompareEditorInput) adaptableObject)
							.getAdapter(IEditorInput.class);
					if (ei instanceof IFileEditorInput) {
						return ((IFileEditorInput) ei).getFile();
					}
					return null;
				}
			};
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { IContributorResourceAdapter.class };
	}
}