package org.eclipse.e4.ui.workbench.swt.factories;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

public interface IRendererFactory {
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent);

	// public void init(IEclipseContext context);
}
