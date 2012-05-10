package org.eclipse.e4.demo.split.renderer.wb;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.demo.split.model.split.MStackSashContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

public class SwitchHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		MPart model = (MPart) part.getSite().getService(MPart.class);
		MUIElement parent = model.getParent();
		if (parent instanceof MStackSashContainer) {
			MStackSashContainer sash = (MStackSashContainer) parent;
			for (final MUIElement element : sash.getChildren()) {
				if (element != sash.getSelectedElement()) {
					UIJob job = new UIJob("Swap") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							EPartService partService = (EPartService) HandlerUtil
									.getVariable(event,
											EPartService.class.getName());
							partService.activate((MPart) element);
							return Status.OK_STATUS;
						}
					};
					job.schedule();
					break;
				}
			}
		}
		return null;
	}
}
