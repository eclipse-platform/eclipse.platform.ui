package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.provisional.ide.IEditorOpenStrategy;
import org.eclipse.ui.internal.provisional.ide.OpenWithInfo;

public class ModelProviderBasedEditorOpenStrategy implements
		IEditorOpenStrategy {

	public OpenWithInfo getOpenWithInfo(Object element) {
		OpenWithInfo result = new OpenWithInfo();
		ResourceMapping mapping = ResourceUtil.getResourceMapping(element);
		try {
			boolean anyFound = false;
			ModelProvider[] providers = ModelProviderUtil.getModelProvidersFor(mapping);
			for (int i = 0; i < providers.length; i++) {
				ModelProvider provider = providers[i];
				IEditorOpenStrategy openStrategy = (IEditorOpenStrategy) ResourceUtil.getAdapter(provider, IEditorOpenStrategy.class, true);
				if (openStrategy != null) {
					OpenWithInfo info = openStrategy.getOpenWithInfo(element);
					result = result.mergeWith(info);
					anyFound = true;
				}
			}
			// TODO: The following is just to handle the case where model providers and/or the corresponding
			// open strategies cannot be found due to an error in the configuration.  It may not be needed.
			if (!anyFound) {
				OpenWithInfo info = new ResourceEditorOpenStrategy().getOpenWithInfo(element);
				result = result.mergeWith(info);
			}
		} catch (CoreException e) {
			IDEWorkbenchPlugin.log(getClass(), "getOpenWithInfo", e); //$NON-NLS-1$
		}
		return result;
	}

}
