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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
		loadProviders();
		openManager();
	}

	/**
	 * Reloads the tip providers.
	 */
	public static void loadProviders() {
		loadInternalProviders();
		loadExternalProviders();
	}

	private static void loadInternalProviders() {
		getInternalProvidersJob().schedule();
	}

	private static Job getInternalProvidersJob() {
		Job job = new Job("Load default IDE Tip Providers") {

			@Override
			protected IStatus run(IProgressMonitor pArg0) {
				String baseURL = System.getProperty("org.eclipse.tips.ide.provider.url");
				if (baseURL == null) {
					baseURL = "http://www.eclipse.org/downloads/download.php?r=1&file=/e4/tips/";
				}
				try {
					ProviderLoader.loadProviderData(IDETipManager.getInstance(), baseURL,
							IDETipManager.getStateLocation());
				} catch (Exception e) {
					Status status = new Status(IStatus.ERROR, FrameworkUtil.getBundle(Startup.class).getSymbolicName(),
							"Failure getting the Tips state location.", e);
					IDETipManager.getInstance().log(status);
					return status;
				}
				return Status.OK_STATUS;
			};
		};
		return job;
	}

	private static void loadExternalProviders() {
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
		if (IDETipManager.getInstance().hasContent()) {
			getOpenUIJob().schedule();
		} else {
			getWaitJob().schedule();
		}
	}

	private static Job getWaitJob() {
		Job waitJob = new Job("Tip Delay") {

			@Override
			protected IStatus run(IProgressMonitor pMonitor) {
				int attempts = 3;
				SubMonitor monitor = SubMonitor.convert(pMonitor, attempts);
				for (int i = 0; i < attempts; i++) {
					monitor.setTaskName("Checking for content " + i);
					if (openOrSleep(monitor)) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						} else {
							monitor.done();
							return Status.OK_STATUS;
						}
					}
					monitor.worked(1);
				}
				monitor.done();
				return Status.OK_STATUS;
			}

			private boolean openOrSleep(SubMonitor pMonitor) {
				if (IDETipManager.getInstance().hasContent()) {
					getOpenUIJob().schedule();
					return true;
				}
				if (sleep(1000)) {
					pMonitor.setCanceled(true);
					return true;
				}
				return false;
			}

			private boolean sleep(int millis) {
				try {
					Thread.sleep(millis);
					return false;
				} catch (InterruptedException e) {
					return true;
				}
			}
		};
		return waitJob;
	}

	private static UIJob getOpenUIJob() {
		UIJob uiJob = new UIJob(PlatformUI.getWorkbench().getDisplay(), "Tip of the Day") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IDETipManager.getInstance().open(true);
				return Status.OK_STATUS;
			}
		};
		return uiJob;
	}

	private static void log(CoreException e) {
		Bundle bundle = FrameworkUtil.getBundle(Startup.class);
		Status status = new Status(IStatus.ERROR, bundle.getSymbolicName(), e.getMessage(), e);
		Platform.getLog(bundle).log(status);
	}
}