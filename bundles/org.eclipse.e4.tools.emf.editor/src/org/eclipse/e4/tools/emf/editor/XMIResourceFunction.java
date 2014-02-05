package org.eclipse.e4.tools.emf.editor;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.RGB;

public class XMIResourceFunction extends ContextFunction {

	static {
		JFaceResources.getColorRegistry().put(JFacePreferences.COUNTER_COLOR,new RGB(0, 127, 174));
		JFaceResources.getColorRegistry().put(JFacePreferences.DECORATIONS_COLOR,new RGB(149, 125, 71));
		JFaceResources.getColorRegistry().put(JFacePreferences.QUALIFIER_COLOR,new RGB(128, 128, 128));
	}

	@Override
	public Object compute(IEclipseContext context) {
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
