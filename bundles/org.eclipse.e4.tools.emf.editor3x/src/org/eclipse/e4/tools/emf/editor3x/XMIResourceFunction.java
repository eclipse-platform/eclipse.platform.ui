package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.editor3x.emf.EditUIUtil;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorInput;

public class XMIResourceFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		final IEditorInput input = context.get(IEditorInput.class);
		if( input != null ) {
			URI resourceURI = EditUIUtil.getURI(input);
			return new XMIModelResource(resourceURI);
		}

		return null;
	}

}
