package org.eclipse.e4.demo.split.renderer.wb;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.demo.split.model.split.MStackSashContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class OrientationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		MPart model = (MPart) part.getSite().getService(MPart.class);
		MUIElement parent = model.getParent();
		if (parent instanceof MStackSashContainer) {
			MStackSashContainer sash = (MStackSashContainer) parent;
			sash.setHorizontal(!sash.isHorizontal());
		}
		return null;
	}
}
