package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.editor3x.compat.E4CompatEditorPart;
import org.eclipse.e4.tools.emf.editor3x.emf.EditUIUtil;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.EditorPart;

public class XMIResourceFunction extends ContextFunction {

	@Override
	public Object compute(final IEclipseContext context) {
		final IEditorInput input = context.get(IEditorInput.class);
		final E4CompatEditorPart part = (E4CompatEditorPart) context.get(EditorPart.class);
		
		if( input != null ) {
			URI resourceURI = EditUIUtil.getURI(input);
			final XMIModelResource resource = new XMIModelResource(resourceURI);
			resource.addModelListener(new ModelListener() {
				
				public void dirtyChanged() {
					context.set(EditorPart.class.getName()+".dirty", resource.isDirty());
					part.firePropertyChange(EditorPart.PROP_DIRTY);
				}

				public void commandStackChanged() {
					
				}
			});
			return resource;
		}

		return null;
	}

}
