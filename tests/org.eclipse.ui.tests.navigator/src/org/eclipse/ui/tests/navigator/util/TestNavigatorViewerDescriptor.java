/*******************************************************************************
 * Copyright (c) 2015 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.util;

import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.MenuInsertionPoint;

/**
 * A "mock" INavigatorViewerDescriptor.
 */
public class TestNavigatorViewerDescriptor implements INavigatorViewerDescriptor {
	@Override
	public String getViewerId() {
		return null;
	}

	@Override
	public String getPopupMenuId() {
		return null;
	}

	@Override
	public boolean isVisibleContentExtension(String aContentExtensionId) {
		return false;
	}

	@Override
	public boolean isVisibleActionExtension(String anActionExtensionId) {
		return false;
	}

	@Override
	public boolean isRootExtension(String aContentExtensionId) {
		return false;
	}

	@Override
	public boolean hasOverriddenRootExtensions() {
		return false;
	}

	@Override
	public boolean allowsPlatformContributionsToContextMenu() {
		return false;
	}

	@Override
	public MenuInsertionPoint[] getCustomInsertionPoints() {
		return null;
	}

	@Override
	public String getStringConfigProperty(String aPropertyName) {
		return null;
	}

	@Override
	public boolean getBooleanConfigProperty(String aPropertyName) {
		return false;
	}

	@Override
	public String getHelpContext() {
		return null;
	}
}
