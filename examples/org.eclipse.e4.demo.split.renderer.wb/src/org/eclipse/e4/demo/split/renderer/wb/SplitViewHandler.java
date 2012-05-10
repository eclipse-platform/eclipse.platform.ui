package org.eclipse.e4.demo.split.renderer.wb;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.demo.split.model.split.MStackSashContainer;
import org.eclipse.e4.demo.split.model.split.impl.SplitFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class SplitViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof IViewPart) {
			EPartService partService = (EPartService) HandlerUtil.getVariable(
					event, EPartService.class.getName());
			MPart part2 = partService.createPart(part.getSite().getId());
			split(part, part2);
		}
		return null;
	}

	private void split(IWorkbenchPart workbenchPart1, MPart part2) {
		part2.getTags().add("3x-secondary:" + System.currentTimeMillis()); //$NON-NLS-1$
		MPart part1 = (MPart) workbenchPart1.getSite().getService(MPart.class);
		MPlaceholder placeholder = part1.getCurSharedRef();

		MStackSashContainer stackedSash = SplitFactoryImpl.eINSTANCE.createStackSashContainer();
		stackedSash.setHorizontal(false);
		stackedSash.getChildren().add(part2);
		placeholder.getParent().getChildren().add(stackedSash);
		stackedSash.getChildren().add(placeholder);
	}
}
