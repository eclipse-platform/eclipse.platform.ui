package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

public class EditorProjectFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, String contextKex) {
		final IEditorInput input = context.get(IEditorInput.class);
		if (input != null && input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile().getProject();
		}
		return null;
	}

}
