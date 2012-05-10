package org.eclipse.e4.demo.split.renderer.swt;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory;

@SuppressWarnings("restriction")
public class SplitRendererFactory extends WorkbenchRendererFactory {
	private LazyStackRenderer stackRenderer;
	
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		if (uiElement instanceof MPartStack) {
			if (stackRenderer == null) {
				stackRenderer = new SplitStackRenderer(); 
				initRenderer(stackRenderer);
			}
			return stackRenderer;
		}
		return super.getRenderer(uiElement, parent);
	}
}
