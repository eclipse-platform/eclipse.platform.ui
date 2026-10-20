/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;

public class WorkbenchRendererFactory implements IRendererFactory {
	public static final String SHARED_ELEMENTS_STORE = "org.eclipse.e4.ui.workbench.renderers.swt.SHARED_ELEMENTS_STORE"; //$NON-NLS-1$
	private AreaRenderer areaRenderer;
	private MenuManagerRenderer menuRenderer;
	private ToolBarManagerRenderer toolbarRenderer;
	private SeparatorRenderer separatorRenderer;
	private ContributedPartRenderer contributedPartRenderer;
	private ElementReferenceRenderer elementRefRenderer;
	private PerspectiveStackRenderer perspStackRenderer;
	private PerspectiveRenderer perspRenderer;
	private SashRenderer partSashRenderer;
	private LazyStackRenderer stackRenderer;
	private TrimBarRenderer trimBarRenderer;
	private ToolControlRenderer toolControlRenderer;
	private WBWRenderer wbwRenderer;

	private IEclipseContext context;

	@Override
	public AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		return switch (uiElement) {
		case MArea a -> areaRenderer != null ? areaRenderer : initialized(areaRenderer = new AreaRenderer());
		case MPart p -> contributedPartRenderer != null ? contributedPartRenderer : initialized(contributedPartRenderer = new ContributedPartRenderer());
		case MMenu m -> menuRenderer != null ? menuRenderer : initialized(menuRenderer = new MenuManagerRenderer());
		case MToolBar t -> toolbarRenderer != null ? toolbarRenderer : initialized(toolbarRenderer = new ToolBarManagerRenderer());
		case MMenuSeparator s -> separatorRenderer != null ? separatorRenderer : initialized(separatorRenderer = new SeparatorRenderer());
		case MToolBarSeparator s -> separatorRenderer != null ? separatorRenderer : initialized(separatorRenderer = new SeparatorRenderer());
		case MPlaceholder p -> elementRefRenderer != null ? elementRefRenderer : initialized(elementRefRenderer = new ElementReferenceRenderer());
		case MPerspective p -> perspRenderer != null ? perspRenderer : initialized(perspRenderer = new PerspectiveRenderer());
		case MPerspectiveStack s -> perspStackRenderer != null ? perspStackRenderer : initialized(perspStackRenderer = new PerspectiveStackRenderer());
		case MPartSashContainer s -> partSashRenderer != null ? partSashRenderer : initialized(partSashRenderer = new SashRenderer());
		case MPartStack s -> stackRenderer != null ? stackRenderer : initialized(stackRenderer = new StackRenderer());
		case MTrimBar t -> trimBarRenderer != null ? trimBarRenderer : initialized(trimBarRenderer = new TrimBarRenderer());
		case MToolControl t -> toolControlRenderer != null ? toolControlRenderer : initialized(toolControlRenderer = new ToolControlRenderer());
		case MWindow w -> wbwRenderer != null ? wbwRenderer : initialized(wbwRenderer = new WBWRenderer());
		case null, default -> null;
		};
	}

	private <R extends AbstractPartRenderer> R initialized(R renderer) {
		initRenderer(renderer);
		return renderer;
	}

	protected void initRenderer(AbstractPartRenderer renderer) {
		renderer.init(context);
		ContextInjectionFactory.inject(renderer, context);
	}

	@PostConstruct
	public void init(IEclipseContext context) {
		this.context = context;
		this.context.set(SHARED_ELEMENTS_STORE, new HashMap<>());
	}

}
