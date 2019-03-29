/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.quickaccess;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.QuickAccessElement;

public class LaunchQuickAccessElement extends QuickAccessElement {

	private ILaunchConfiguration launch;
	private ILaunchMode launchMode;

	public LaunchQuickAccessElement(ILaunchConfiguration launch, ILaunchMode launchMode) {
		this.launch = launch;
		this.launchMode = launchMode;
	}

	@Override
	public String getLabel() {
		return launch.getName();
	}

	@Override
	public String getMatchLabel() {
		return getLabel() + ' ' + launchMode.getLabel();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		try {
			return DebugPluginImages.getImageDescriptor(launch.getType().getIdentifier());
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
			return null;
		}
	}

	@Override
	public String getId() {
		return launch.getName() + '/' + launchMode.getIdentifier();
	}

	@Override
	public void execute() {
		DebugUITools.launch(launch, launchMode.getIdentifier());
	}

}
