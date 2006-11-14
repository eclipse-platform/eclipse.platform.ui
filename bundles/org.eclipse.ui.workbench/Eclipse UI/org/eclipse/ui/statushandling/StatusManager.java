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

package org.eclipse.ui.statushandling;

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
 * StatusManager.getManager().handle(IStatus) method is one point of entry for
 * all problems to report.
 * </p>
 * 
 * <p>
 * Default policy (steps):
 * <ul>
 * <li>manager tries to handle the problem with default handler</li>
 * <li>manager tries to find a right handler for status plugin </li>
 * <li>manager delegates the problem to workbench handler</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Each status handler defined as "statusHandler" extension can have package
 * prefix assigned to it. During step 2 status manager looks for the most
 * specific handler for given status checking status pluginId against these
 * prefixes. If handler is the default one it is not used in this step.
 * </p>
 * 
 * <p>
 * Default handler can be set for product using "statusHandlerProductBinding"
 * element in extension for "statusHandler" ext. point.
 * </p>
 * 
 * <p>
 * Workbench handler is the
 * {@link org.eclipse.ui.internal.WorkbenchErrorHandlerProxy} object which
 * passes handling to handler assigned to the workbench advisor. This handler
 * doesn't have to be added as "statusHandler" extension.
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
	 * Constant indicating that a problem should be ignored
	 */
	public static final int IGNORE = 0;

	/**
	 * Constant indicating that handlers should log a problem
	 */
	public static final int LOG = 1;

	/**
	 * Constant indicating that handlers should log and show a problem to an
	 * user
	 */
	public static final int SHOWANDLOG = 2;

	/**
	 * Constant indicating that handlers should show a problem to an user
	 */
	public static final int SHOW = 3;

	private static StatusManager MANAGER;

	private StatusHandlersMap statusHandlersMap;

	private AbstractStatusHandler defaultHandler;

	private AbstractStatusHandler workbenchHandler;

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
		Platform.addLogListener(new EHLogListener());
	}

	private AbstractStatusHandler createErrorHandler(
			IConfigurationElement configElement) {
		try {

			configElement.getName();

			AbstractStatusHandler errorHandler = (AbstractStatusHandler) configElement
					.createExecutableExtension("class"); //$NON-NLS-1$

			errorHandler.setId(configElement.getAttribute("id")); //$NON-NLS-1$

			IConfigurationElement parameters[] = configElement
					.getChildren("parameter"); //$NON-NLS-1$

			Map params = new HashMap();

			for (int i = 0; i < parameters.length; i++) {
				params.put(parameters[i].getAttribute("name"), //$NON-NLS-1$
						parameters[i].getAttribute("value")); //$NON-NLS-1$
			}

			errorHandler.setParams(params);

			errorHandler.setContributorName(configElement.getContributor()
					.getName());

			return errorHandler;
		} catch (CoreException ex) {
			WorkbenchPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
							IStatus.ERROR, "EH initialization problem", ex)); //$NON-NLS-1$
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
				.getExtensionPoint("org.eclipse.ui.statusHandler").getExtensions(); //$NON-NLS-1$

		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();

			for (int j = 0; j < configElements.length; j++) {
				if (configElements[j].getName().equals("statusHandler")) { //$NON-NLS-1$
					AbstractStatusHandler handler = createErrorHandler(configElements[j]);
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
	 * @return the workbech error handler
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
	 *            error status to handle
	 * @param hint
	 *            handling hint
	 */
	public void handle(IStatus status, int hint) {
		StatusHandlingState handlingState = new StatusHandlingState(status,
				hint);

		// tries to handle the problem with default (product) handler
		if (defaultHandler != null) {
			defaultHandler.handle(handlingState);

			if (handlingState.getHandlingHint() == IGNORE) {
				return;
			}
		}

		// tries to handle the problem with any handler due to the prefix policy
		List okHandlers = statusHandlersMap.getHandlers(status.getPlugin());

		if (okHandlers != null && okHandlers.size() > 0) {
			AbstractStatusHandler handler = null;

			for (Iterator it = okHandlers.iterator(); it.hasNext();) {
				handler = (AbstractStatusHandler) it.next();
				handler.handle(handlingState);

				if (handlingState.getHandlingHint() == IGNORE) {
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
	 *            error status to handle
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
			add(this.map, getPattern(handler), handler);
		}

		private void add(Map map, String pattern, AbstractStatusHandler handler) {
			if (pattern == null) {
				if (map.get(ASTERISK) == null) {
					map.put(ASTERISK, new ArrayList());
				}

				((List) map.get(ASTERISK)).add(handler);
			} else {

				String s[] = pattern.split("\\.|$", 2); //$NON-NLS-1$

				if (map.get(s[0]) == null) {
					map.put(s[0], new HashMap());
				}

				add((Map) map.get(s[0]),
						(s[1].equals("") ? null : s[1]), handler); //$NON-NLS-1$
			}
		}

		private String getPattern(AbstractStatusHandler handler) {
			Object pattern = handler.getParam("pattern"); //$NON-NLS-1$

			if (pattern != null) {
				return (String) pattern;
			}

			return null;
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

		private List get(String pluginId, Map map) {
			if (pluginId == null) {
				return getAsteriskList(map);
			}

			String s[] = pluginId.split("\\.|$", 2); //$NON-NLS-1$

			if (map.get(s[0]) == null) {
				return getAsteriskList(map);
			}

			return get((s[1].equals("") ? null : s[1]), (Map) map.get(s[0])); //$NON-NLS-1$
		}

		private List getAsteriskList(Map map) {
			Object list = map.get(ASTERISK);
			if (list != null) {
				return (List) list;
			}

			return null;
		}
	}

	private class EHLogListener implements ILogListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus,
		 *      java.lang.String)
		 */
		public void logging(IStatus status, String plugin) {
			// System.out.println(status.getMessage());
		}
	}
}
