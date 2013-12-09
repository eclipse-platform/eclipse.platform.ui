package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SplitHandler extends AbstractHandler {
	private EModelService modelService;
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public SplitHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Only works for the active editor
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor == null)
			return null;
		
		MPart editorPart = (MPart) activeEditor.getSite().getService(MPart.class);
		if (editorPart == null)
			return null;
		
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Get services
		modelService =  editorPart.getContext().get(EModelService.class);
		
		MPartStack stack = getStackFor(editorPart);
		if (stack == null)
			return null;

		window.getShell().setRedraw(false);
		try {
			MStackElement stackSelElement = stack.getSelectedElement();
			if (stackSelElement instanceof MCompositePart) {
				List<MPart> innerElements = modelService.findElements(stackSelElement, null, MPart.class, null);
				MPart originalEditor = innerElements.get(1); // '0' is the composite part
				
				originalEditor.getTags().remove(IPresentationEngine.SPLIT_HORIZONTAL);
				originalEditor.getTags().remove(IPresentationEngine.SPLIT_VERTICAL);
			} else {
				if ("false".equals(event.getParameter("Splitter.isHorizontal"))) { //$NON-NLS-1$ //$NON-NLS-2$
					editorPart.getTags().add(IPresentationEngine.SPLIT_VERTICAL);
				} else {
					editorPart.getTags().add(IPresentationEngine.SPLIT_HORIZONTAL);
				}
			}
		} finally {
			window.getShell().setRedraw(true);
		}
		
		return null;
	}
	
	private MPartStack getStackFor(MPart part) {
		MUIElement presentationElement = part.getCurSharedRef() == null ? part : part.getCurSharedRef();
		MUIElement parent = presentationElement.getParent();
		while (parent != null && !(parent instanceof MPartStack))
			parent = parent.getParent();
		
		return (MPartStack) parent;
	}
}
