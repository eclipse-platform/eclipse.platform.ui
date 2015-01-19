/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.eclipse.ui.part.ViewPart;

/**
 *
 */
public class NonCommonViewerView extends ViewPart {

	private TreeViewer _viewer;

	@Override
	public void createPartControl(Composite parent) {
		_viewer = new TreeViewer(parent);

		INavigatorContentService service = NavigatorContentServiceFactory.INSTANCE
				.createContentService(
						NavigatorTestBase.TEST_VIEWER_NON_COMMONVIEWER, _viewer);

		service.bindExtensions(new String[] { "org.eclipse.ui.navigator.resourceContent" },
				true);
		service.getActivationService().activateExtensions(
				new String[] { "org.eclipse.ui.navigator.resourceContent" }, false);

		_viewer.setContentProvider(service.createCommonContentProvider());
		_viewer.setLabelProvider(new DecoratingLabelProvider(service
				.createCommonLabelProvider(), PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator()));

		_viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

	}

	@Override
	public void setFocus() {
	}

	public TreeViewer getViewer() {
		return _viewer;
	}

}
