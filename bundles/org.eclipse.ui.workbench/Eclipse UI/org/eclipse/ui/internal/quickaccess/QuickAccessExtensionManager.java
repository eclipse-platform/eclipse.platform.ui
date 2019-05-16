/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
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
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 *
 */
public class QuickAccessExtensionManager {

	private static final String EXTENSION_POINT_ID = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchRegistryConstants.PL_QUICK_ACCESS;
	private static final String COMPUTER_TAG = "computer"; //$NON-NLS-1$

	private static class QuickAccessProviderExtensionProxy extends QuickAccessProvider {
		private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
		private static final String REQUIRES_UI_ACCESS_ATTRIBUTE = "requiresUIAccess"; //$NON-NLS-1$

		private final Bundle bundle;
		private final IConfigurationElement extension;
		private final QuickAccessElement[] activateElement;
		private IQuickAccessComputer computer;

		public QuickAccessProviderExtensionProxy(IConfigurationElement extension, Runnable onActivate) {
			this.bundle = Platform.getBundle(extension.getContributor().getName());
			this.extension = extension;
			this.activateElement = new QuickAccessElement[] { new QuickAccessElement() {
				@Override
				public String getLabel() {
					return NLS.bind(QuickAccessMessages.QuickAccessContents_activate,
							QuickAccessProviderExtensionProxy.this.getName());
				}

				@Override
				public ImageDescriptor getImageDescriptor() {
					return null;
				}

				@Override
				public String getId() {
					return "activate-" + QuickAccessProviderExtensionProxy.this.getId(); //$NON-NLS-1$
				}

				@Override
				public void execute() {
					try {
						bundle.start();
						reset();
						if (onActivate != null) {
							onActivate.run();
						}
					} catch (BundleException e) {
						WorkbenchPlugin.log(e);
					}
				}
			} };
		}

		private boolean canDelegate() {
			if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
				if (computer == null) {
					try {
						computer = (IQuickAccessComputer) extension
								.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
					} catch (CoreException e) {
						WorkbenchPlugin.log(e);
					}
				}
				return computer != null;
			}
			return false;
		}

		@Override
		public boolean requiresUiAccess() {
			return Boolean.parseBoolean(extension.getAttribute(REQUIRES_UI_ACCESS_ATTRIBUTE));
		}

		@Override
		public String getName() {
			return extension.getAttribute(NAME_ATTRIBUTE);
		}

		@Override
		public String getId() {
			return bundle.getSymbolicName() + '/' + extension.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public boolean isAlwaysPresent() {
			return false;
		}

		@Override
		public QuickAccessElement[] getElementsSorted() {
			if (canDelegate()) {
				if (computer.needsRefresh()) {
					reset();
				}
				return super.getElementsSorted();
			}
			return activateElement;
		}

		@Override
		public QuickAccessElement[] getElements() {
			if (canDelegate()) {
				return computer.computeElements();
			}
			return activateElement;
		}

		@Override
		protected void doReset() {
			if (canDelegate()) {
				computer.resetState();
			}
		}

	}

	public static Collection<QuickAccessProvider> getProviders(Runnable onActivate) {
		return Arrays.stream(Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID))
				.filter(element -> COMPUTER_TAG.equals(element.getName()))
				.map(element -> new QuickAccessProviderExtensionProxy(element, onActivate))
				.collect(Collectors.toList());
	}
}
