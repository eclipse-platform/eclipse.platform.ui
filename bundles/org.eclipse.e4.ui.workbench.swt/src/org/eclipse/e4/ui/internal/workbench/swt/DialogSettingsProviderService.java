/*******************************************************************************
 * Copyright (c) 2020, 2020 Remain Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.jface.dialogs.IDialogSettingsProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Provides access to DialogSettingsProvider objects.
 *
 */
public final class DialogSettingsProviderService {

	public static final DialogSettingsProviderService instance = new DialogSettingsProviderService();

	private static Map<Bundle, DialogSettingsProvider> fTrackedBundles = Collections
			.synchronizedMap(new WeakHashMap<>());

	/**
	 * Hook in a listener to save all dialog settings on when the platform is about
	 * to shutdown.
	 */
	static {
		BundleContext ctx = FrameworkUtil.getBundle(DialogSettingsProviderService.class).getBundleContext();
		Dictionary<String, String[]> topics = new Hashtable<>();
		topics.put(EventConstants.EVENT_TOPIC, new String[] { UILifeCycle.APP_SHUTDOWN_STARTED });
		ctx.registerService(EventHandler.class, new EventHandler() {

			private boolean fSaved;

			@Override
			public void handleEvent(Event event) {
				if (event.getTopic().equals(UILifeCycle.APP_SHUTDOWN_STARTED)) {
					if (!fSaved) {
						if (Platform.inDebugMode()) {
							Platform.getLog(ctx.getBundle()).info("Saving dialog settings"); //$NON-NLS-1$
						}
						fTrackedBundles.forEach((bundle, service) -> service.saveDialogSettings());
						fSaved = true;
					}
				}

			}
		}, topics);
	}

	/**
	 * Gets or creates the {@link IDialogSettingsProvider} for this bundle.
	 *
	 * @param bundle the bundle to get the dialog settings for
	 * @return the {@link IDialogSettingsProvider}
	 */
	public synchronized IDialogSettingsProvider getProvider(Bundle bundle) {
		if (fTrackedBundles.containsKey(bundle)) {
			return fTrackedBundles.get(bundle);
		}
		DialogSettingsProvider dialogSettingsProvider = new DialogSettingsProvider(bundle);
		fTrackedBundles.put(bundle, dialogSettingsProvider);
		return dialogSettingsProvider;
	}

}
