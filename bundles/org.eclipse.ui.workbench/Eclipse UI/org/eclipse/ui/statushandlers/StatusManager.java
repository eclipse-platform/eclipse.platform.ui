/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.WorkbenchErrorHandlerProxy;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * Status manager is responsible for creating status handlers and handling
 * statuses due to the set handling policy.
 * </p>
 * 
 * <p>
 * Handlers shoudn't be used directly but through the StatusManager singleton
 * which keeps the status handling policy and chooses handlers due to it.
 * <code>StatusManager.getManager().handle(IStatus)</code> and
 * <code>handle(IStatus status, int
 * hint)</code> methods are used for passing
 * all problems to the facility.
 * </p>
 * 
 * <p>
 * Handling hints
 * <ul>
 * <li>NONE - nothing should be done with the status</li>
 * <li>LOG - the status should be logged</li>
 * <li>SHOW - the status should be shown to an user</li>
 * <li>SHOWANDLOG - the status should be logged and shown to an user</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Default policy (steps):
 * <ul>
 * <li>manager tries to handle the status with a default handler</li>
 * <li>manager tries to find a right handler for the status</li>
 * <li>manager delegates the status to workbench handler</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Each status handler defined in "statusHandlers" extension can have package
 * prefix assigned to it. During step 2 status manager is looking for the most
 * specific handler for given status checking status pluginId against these
 * prefixes. The default handler is not used in this step.
 * </p>
 * 
 * <p>
 * The default handler can be set for product using
 * "statusHandlerProductBinding" element in "statusHandlers" extension.
 * </p>
 * 
 * <p>
 * Workbench handler is the
 * {@link org.eclipse.ui.internal.WorkbenchErrorHandlerProxy} object which
 * passes handling to handler assigned to the workbench advisor. This handler
 * doesn't have to be added as "statusHandlers" extension.
 * </p>
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class StatusManager {
	/**
	 * A handling hint indicating that nothing should be done with a problem
	 */
	public static final int NONE = 0;

	/**
	 * A handling hint indicating that handlers should log a problem
	 */
	public static final int LOG = 0x01;

	/**
	 * A handling hint indicating that handlers should show a problem to an user
	 */
	public static final int SHOW = 0x02;
	
	private static StatusManager MANAGER;

	private StatusHandlersMap statusHandlersMap;

	private AbstractStatusHandler defaultHandler;

	private AbstractStatusHandler workbenchHandler;

	private List loggedStatuses = new ArrayList();

	/**
	 * Returns StatusManager singleton instance
	 * 
	 * @return StatusManager instance
	 */
	public static StatusManager getManager() {
		if (MANAGER == null) {
			MANAGER = new StatusManager();
			MANAGER.initErrorHandlers();
		}

		return MANAGER;
	}

	private StatusManager() {
		statusHandlersMap = new StatusHandlersMap();
		Platform.addLogListener(new StatusManagerLogListener());
	}

	private AbstractStatusHandler createStatusHandler(
			IConfigurationElement configElement) {
		try {

			configElement.getName();

			AbstractStatusHandler statusHandler = (AbstractStatusHandler) configElement
					.createExecutableExtension("class"); //$NON-NLS-1$

			statusHandler.setId(configElement.getAttribute("id")); //$NON-NLS-1$

			IConfigurationElement parameters[] = configElement
					.getChildren("parameter"); //$NON-NLS-1$

			Map params = new HashMap();

			for (int i = 0; i < parameters.length; i++) {
				params.put(parameters[i].getAttribute("name"), //$NON-NLS-1$
						parameters[i].getAttribute("value")); //$NON-NLS-1$
			}

			statusHandler.setParams(params);

			return statusHandler;
		} catch (CoreException ex) {
			WorkbenchPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
							IStatus.ERROR, "Status handling initialization problem", ex)); //$NON-NLS-1$
		}

		return null;
	}

	private void initErrorHandlers() {

		String productId = Platform.getProduct() != null ? Platform
				.getProduct().getId() : null;

		List allHandlers = new ArrayList();

		String defaultHandlerId = null;

		IExtension[] extensions = Platform
				.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.statusHandlers").getExtensions(); //$NON-NLS-1$

		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();

			for (int j = 0; j < configElements.length; j++) {
				if (configElements[j].getName().equals("statusHandler")) { //$NON-NLS-1$
					AbstractStatusHandler handler = createStatusHandler(configElements[j]);
					if (handler != null) {
						allHandlers.add(handler);
					}
				} else if (configElements[j].getName().equals(
						"statusHandlerProductBinding")) //$NON-NLS-1$	
				{
					if (configElements[j]
							.getAttribute("productId").equals(productId)) //$NON-NLS-1$
					{
						defaultHandlerId = configElements[j]
								.getAttribute("handlerId"); //$NON-NLS-1$
					}
				}
			}
		}

		AbstractStatusHandler handler = null;

		for (Iterator it = allHandlers.iterator(); it.hasNext();) {
			handler = (AbstractStatusHandler) it.next();

			if (handler.getId().equals(defaultHandlerId)) {
				defaultHandler = handler;
			} else {
				statusHandlersMap.addHandler(handler);
			}
		}
	}

	/**
	 * @return the workbech status handler
	 */
	private AbstractStatusHandler getWorkbenchHandler() {
		if (workbenchHandler == null) {
			workbenchHandler = new WorkbenchErrorHandlerProxy();
		}

		return workbenchHandler;
	}

	/**
	 * Handles status due to the prefix policy.
	 * 
	 * @param status
	 *            status to handle
	 * @param hint
	 *            handling hint
	 */
	public void handle(IStatus status, int hint) {
		StatusHandlingState handlingState = new StatusHandlingState(status,
				hint);

		// tries to handle the problem with default (product) handler
		if (defaultHandler != null) {
			boolean shouldContinue = defaultHandler.handle(handlingState);

			if (!shouldContinue) {
				return;
			}
		}

		// tries to handle the problem with any handler due to the prefix policy
		List okHandlers = statusHandlersMap.getHandlers(status.getPlugin());

		if (okHandlers != null && okHandlers.size() > 0) {
			AbstractStatusHandler handler = null;

			for (Iterator it = okHandlers.iterator(); it.hasNext();) {
				handler = (AbstractStatusHandler) it.next();
				boolean shouldContinue = handler.handle(handlingState);

				if (!shouldContinue) {
					return;
				}
			}
		}

		// delegates the problem to workbench handler
		getWorkbenchHandler().handle(handlingState);
	}

	/**
	 * Handles status due to the prefix policy.
	 * 
	 * @param status
	 *            status to handle
	 */
	public void handle(IStatus status) {
		handle(status, LOG);
	}

	/*
	 * Helper class supporting the prefix based status handling policy.
	 */
	private class StatusHandlersMap {

		private final String ASTERISK = "*"; //$NON-NLS-1$

		HashMap map;

		/**
		 * Creates a new instance of the class
		 */
		public StatusHandlersMap() {
			map = new HashMap();
		}

		/**
		 * Adds a new handler to the prefix tree
		 * 
		 * @param handler
		 *            the handler to add
		 */
		public void addHandler(AbstractStatusHandler handler) {
			add(this.map, (String) handler.getParam("prefix"), handler); //$NON-NLS-1$
		}

		/*
		 * Recursively searches the tree for the best place for the handler
		 */
		private void add(Map map, String prefix, AbstractStatusHandler handler) {
			if (prefix == null) {
				if (map.get(ASTERISK) == null) {
					map.put(ASTERISK, new ArrayList());
				}

				((List) map.get(ASTERISK)).add(handler);
			} else {
				int delimIndex = prefix.indexOf("."); //$NON-NLS-1$

				String pre = null;
				String post = null;

				if (delimIndex != -1) {
					pre = prefix.substring(0, delimIndex);

					if (delimIndex < prefix.length() - 1) {
						post = prefix.substring(delimIndex + 1);
					}
				} else {
					pre = prefix;
				}

				if (map.get(pre) == null) {
					map.put(pre, new HashMap());
				}

				add((Map) map.get(pre), post, handler);
			}
		}

		/**
		 * Returns status handlers whose prefixes are the most specific for
		 * given pluginId.
		 * 
		 * @param pluginId
		 * @return handlers list
		 */
		public List getHandlers(String pluginId) {
			return get(pluginId, this.map);
		}

		/*
		 * Recursively searches the prefix tree for the most specific handler
		 * for the given pluginId.
		 */
		private List get(String pluginId, Map map) {
			if (pluginId == null) {
				return getAsteriskList(map);
			}

			int delimIndex = pluginId.indexOf("."); //$NON-NLS-1$

			String pre = null;
			String post = null;

			if (delimIndex != -1) {
				pre = pluginId.substring(0, delimIndex);

				if (delimIndex < pluginId.length() - 1) {
					post = pluginId.substring(delimIndex + 1);
				}
			} else {
				pre = pluginId;
			}

			if (map.get(pre) == null) {
				return getAsteriskList(map);
			}

			return get(post, (Map) map.get(pre));
		}

		private List getAsteriskList(Map map) {
			Object list = map.get(ASTERISK);
			if (list != null) {
				return (List) list;
			}

			return null;
		}
	}

	/**
	 * This method informs the StatusManager that this IStatus is being handled
	 * by the WorkbenchErrorHandler and to ignore it when it shows up in our
	 * ILogListener.
	 * 
	 * @param status
	 *            already handled and logged status
	 */
	void addLoggedStatus(IStatus status) {
		loggedStatuses.add(status);
	}

	/**
	 * This log listener handles statuses added to a plug-in's log. If our own
	 * WorkbenchErrorHandler inserts it into the log, then ignore it.
	 * 
	 * @see #addLoggedStatus(IStatus)
	 * @since 3.3
	 */
	private class StatusManagerLogListener implements ILogListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus,
		 *      java.lang.String)
		 */
		public void logging(IStatus status, String plugin) {
			if (!loggedStatuses.contains(status)) {
				handle(status, NONE);
			} else {
				loggedStatuses.remove(status);
			}
		}
	}
}
