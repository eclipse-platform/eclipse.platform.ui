package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MTrimContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;

public class WorkbenchRendererFactory implements IRendererFactory {

	private MenuRenderer menuRenderer;
	private MenuItemRenderer menuItemRenderer;
	private ToolBarRenderer toolbarRenderer;
	private ToolItemRenderer toolItemRenderer;
	private ContributedPartRenderer contributedPartRenderer;
	private PerspectiveStackRenderer perspStackRenderer;
	private PerspectiveRenderer perspRenderer;
	private SashRenderer partSashRenderer;
	private StackRenderer stackRenderer;
	private TrimRenderer trimRenderer;
	private WBWRenderer wbwRenderer;

	private IEclipseContext context;

	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		if (uiElement instanceof MPart) {
			if (contributedPartRenderer == null) {
				contributedPartRenderer = new ContributedPartRenderer();
				initRenderer(contributedPartRenderer);
			}
			return contributedPartRenderer;
		} else if (uiElement instanceof MMenuItem) {
			if (menuItemRenderer == null) {
				menuItemRenderer = new MenuItemRenderer();
				initRenderer(menuItemRenderer);
			}
			return menuItemRenderer;
		} else if (uiElement instanceof MMenu) {
			if (menuRenderer == null) {
				menuRenderer = new MenuRenderer();
				initRenderer(menuRenderer);
			}
			return menuRenderer;
		} else if (uiElement instanceof MToolBar) {
			if (toolbarRenderer == null) {
				toolbarRenderer = new ToolBarRenderer();
				initRenderer(toolbarRenderer);
			}
			return toolbarRenderer;
		} else if (uiElement instanceof MToolItem) {
			if (toolItemRenderer == null) {
				toolItemRenderer = new ToolItemRenderer();
				initRenderer(toolItemRenderer);
			}
			return toolItemRenderer;
		} else if (uiElement instanceof MPerspective) {
			if (perspRenderer == null) {
				perspRenderer = new PerspectiveRenderer();
				initRenderer(perspRenderer);
			}
			return perspRenderer;
		} else if (uiElement instanceof MPerspectiveStack) {
			if (perspStackRenderer == null) {
				perspStackRenderer = new PerspectiveStackRenderer();
				initRenderer(perspStackRenderer);
			}
			return perspStackRenderer;
		} else if (uiElement instanceof MPartSashContainer) {
			if (partSashRenderer == null) {
				partSashRenderer = new SashRenderer();
				initRenderer(partSashRenderer);
			}
			return partSashRenderer;
		} else if (uiElement instanceof MPartStack) {
			if (stackRenderer == null) {
				stackRenderer = new StackRenderer();
				initRenderer(stackRenderer);
			}
			return stackRenderer;
		} else if (uiElement instanceof MTrimContainer<?>) {
			if (trimRenderer == null) {
				trimRenderer = new TrimRenderer();
				initRenderer(trimRenderer);
			}
			return trimRenderer;
		} else if (uiElement instanceof MWindow) {
			if (wbwRenderer == null) {
				wbwRenderer = new WBWRenderer();
				initRenderer(wbwRenderer);
			}
			return wbwRenderer;
		}

		// We could return an 'no renderer' renderer here ??
		return null;
	}

	protected void initRenderer(AbstractPartRenderer renderer) {
		renderer.init(context);
		ContextInjectionFactory.inject(renderer, context);
	}

	public void init(IEclipseContext context) {
		this.context = context;
	}

}
