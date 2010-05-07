package org.eclipse.e4.tools.emf.editor;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.emf.common.util.URI;

public class XMIResourceFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		final MInputPart part = context.get(MInputPart.class);
		if( part != null ) {
			final XMIModelResource resource = new XMIModelResource(URI.createURI(part.getInputURI()));
			resource.addModelListener(new ModelListener() {
				
				public void dirtyChanged() {
					part.setDirty(resource.isDirty());
				}

				public void commandStackChanged() {
					
				}
			});
			return resource;			
		}
		
		return null;
	}
}
