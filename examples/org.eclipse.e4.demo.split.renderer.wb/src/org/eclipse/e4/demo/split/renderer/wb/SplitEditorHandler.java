package org.eclipse.e4.demo.split.renderer.wb;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.demo.split.model.split.MStackSashContainer;
import org.eclipse.e4.demo.split.model.split.impl.SplitFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class SplitEditorHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		try {
			IEditorPart newEditor = page.openEditor(editor.getEditorInput(),
					editor.getSite().getId(), false, IWorkbenchPage.MATCH_NONE);
			split(editor, newEditor);
		} catch (PartInitException e) {
			throw new ExecutionException("Unable to open editors", e);
		}
		return null;
	}

	private void split(IEditorPart editor1, IEditorPart editor2) {
		MPart part1 = (MPart) editor1.getSite().getService(MPart.class);
		MPart part2 = (MPart) editor2.getSite().getService(MPart.class);

		MStackSashContainer stackedSash = SplitFactoryImpl.eINSTANCE.createStackSashContainer();
		stackedSash.setHorizontal(false);
		stackedSash.getChildren().add(part2);
		part1.getParent().getChildren().add(stackedSash);
		stackedSash.getChildren().add(0, part1);
	}
}
