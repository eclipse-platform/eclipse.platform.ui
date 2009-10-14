package org.eclipse.e4.ui.workbench.swt.factories;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;

public interface IRendererFactory {
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent);

	public void init(PartRenderingEngine partRenderingEngine,
			IEclipseContext context, IContributionFactory contributionFactory);
}
