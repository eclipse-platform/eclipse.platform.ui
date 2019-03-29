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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.quickaccess.QuickAccessProvider;

public abstract class AbstractLaunchQuickAccessProvider extends QuickAccessProvider {

	protected final ILaunchManager manager;
	protected final ILaunchMode launchMode;

	public AbstractLaunchQuickAccessProvider(ILaunchMode launchMode) {
		super();
		manager = DebugPlugin.getDefault().getLaunchManager();
		this.launchMode = launchMode;
		manager.addLaunchConfigurationListener(new ILaunchConfigurationListener() {
			@Override
			public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
				reset();
			}

			@Override
			public void launchConfigurationChanged(ILaunchConfiguration configuration) {
				reset();
			}

			@Override
			public void launchConfigurationAdded(ILaunchConfiguration configuration) {
				reset();
			}
		});
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public String getName() {
		return Action.removeMnemonics(launchMode.getLabel());
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_LAUNCH_RUN);
	}

	@Override
	public QuickAccessElement[] getElements() {
		try {
			return Arrays.stream(manager.getLaunchConfigurations()).filter(config -> {
				try {
					return config.getType().supportsMode(launchMode.getIdentifier());
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
					return false;
				}
			}).map(config -> new LaunchQuickAccessElement(this, config, launchMode)).toArray(QuickAccessElement[]::new);
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
			return new QuickAccessElement[0];
		}
	}

	@Override
	protected void doReset() {
	}

}