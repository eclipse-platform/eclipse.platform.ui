/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ide.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.json.internal.ProviderLoader;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Early startup to run the TipManager in the IDE.
 *
 */
@SuppressWarnings("restriction")
public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		openManager();
		loadProviders();
	}

	/**
	 * Reloads the tip providers.
	 */
	public static void loadProviders() {
		loadExternalProviders();
		loadInternalProviders();
	}

	private static void loadExternalProviders() {
		String baseURL = System.getProperty("org.eclipse.tips.ide.provider.url");
		if (baseURL == null) {
			baseURL = "http://www.eclipse.org/downloads/download.php?r=1&file=/e4/tips/";
		}
		try {
			ProviderLoader.loadProviderData(IDETipManager.getInstance(), baseURL, IDETipManager.getStateLocation());
		} catch (Exception e) {
			IDETipManager.getInstance()
					.log(new Status(IStatus.ERROR, FrameworkUtil.getBundle(Startup.class).getSymbolicName(),
							"Failure getting the Tips state location.", e));
		}
	}

	private static void loadInternalProviders() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.tips.core.tips");
		for (IConfigurationElement element : elements) {
			if (element.getName().equals("provider")) {
				try {
					TipProvider provider = (TipProvider) element.createExecutableExtension("class");
					provider.setExpression(getExpression(element));
					IDETipManager.getInstance().register(provider);
				} catch (CoreException e) {
					log(e);
				}
			}
		}
	}

	/**
	 * @return the core expression
	 */
	private static String getExpression(IConfigurationElement element) {
		IConfigurationElement[] enablements = element.getChildren("enablement");
		if (enablements.length == 0) {
			return null;
		}
		IConfigurationElement enablement = enablements[0];
		String result = getXML(enablement.getChildren());
		return result;
	}

	private static String getXML(IConfigurationElement[] children) {
		String result = "";
		for (IConfigurationElement element : children) {
			IConfigurationElement[] myChildren = element.getChildren();
			result += "<" + element.getName() + " " + getXMLAttributes(element) + ">";
			if (myChildren.length > 0) {
				result += getXML(myChildren);
			} else {
				String value = element.getValue();
				result += value == null ? "" : value;
			}
			result += "</" + element.getName() + ">";
		}
		return result;
	}

	private static String getXMLAttributes(IConfigurationElement element) {
		String result = "";
		for (String name : element.getAttributeNames()) {
			result += name;
			result += "=\"";
			result += element.getAttribute(name);
			result += "\" ";
		}
		return result;
	}

	private static void openManager() {
		UIJob job = new UIJob(PlatformUI.getWorkbench().getDisplay(), "Tip of the Day") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IDETipManager.getInstance().open(true);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static void log(CoreException e) {
		Bundle bundle = FrameworkUtil.getBundle(Startup.class);
		Status status = new Status(IStatus.ERROR, bundle.getSymbolicName(), e.getMessage(), e);
		Platform.getLog(bundle).log(status);
	}
}