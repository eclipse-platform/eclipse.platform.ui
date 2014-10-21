package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.editor3x.emf.EditUIUtil;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorInput;

public class XMIResourceFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context, String contextKey) {
		final IEditorInput input = context.get(IEditorInput.class);
		final IDirtyProviderService dirtyProvider = context.get(IDirtyProviderService.class);

		if (input != null) {
			final URI resourceURI = EditUIUtil.getURI(input);
			final XMIModelResource resource = new XMIModelResource(resourceURI);
			resource.addModelListener(new ModelListener() {

				@Override
				public void dirtyChanged() {
					dirtyProvider.setDirtyState(resource.isDirty());
				}

				@Override
				public void commandStackChanged() {

				}
			});
			return resource;
		}

		return null;
	}

}
