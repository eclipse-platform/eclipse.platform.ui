/*******************************************************************************
 * Copyright (c) 2020 Alex Blewitt and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Blewitt - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

/**
 * Provides automatic registration of {@link IResourceChangeListener} instances
 * with {@link IWorkspace} instances.
 *
 * <p>
 * This allows clients to register their {@link IResourceChangeListener}
 * instances as services, and be called back when changes occur, in the same way
 * that (for example) {@link DebugOptionsListener} is used to receive callbacks.
 * </p>
 * <p>
 * The services can also be registered with Declarative Services, which allows a
 * bundle to not require that the Workspace bundle be started prior to accessing
 * the resources, as until the IWorkspace is available the bundle will not need
 * any callbacks. This will also save potential NPEs when the {@link IWorkspace}
 * shuts down, because the OSGi runtime will handle the deregistration of
 * services automatically.
 * </p>
 * <p>
 * Services registered with an <code>event.mask</code> property can be used to
 * receive a sub-set of the events, by registering the value with the
 * {@link IWorkspace#addResourceChangeListener(IResourceChangeListener, int)}
 * method. This allows (for example) {@link IResourceChangeEvent#POST_CHANGE}
 * events to be received by setting <code>event.mask=1</code> in the service
 * registration.
 * </p>
 * <p>
 * The following can be used to register a listener with Declarative Services:
 * </p>
 *
 * <pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.4.0" immediate="true" name="ExampleResourceListener"&gt;
   &lt;implementation class="org.example.ExampleResourceListener"/&gt;
   &lt;service&gt;
      &lt;provide interface="org.eclipse.core.resources.IResourceChangeListener"/&gt;
   &lt;/service&gt;
   &lt;!-- 1 == IResourceChangeEvent.POST_CHANGE -->
   &lt;property name="event.mask" type="Integer" value="1"/&gt;
&lt;/scr:component&gt;
 * </pre>
 */
public final class ResourceChangeListenerRegistrar {
	private final IWorkspace workspace;
	private Logger logger;

	/**
	 * Create an ResourceChangeListenerRegistrar with the given workspace.
	 *
	 * @param workspace the workspace to associate listeners with
	 */
	public ResourceChangeListenerRegistrar(IWorkspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Register the {@link IResourceChangeListener} with the associated workspace
	 *
	 * @param listener   the listener to register
	 * @param properties the properties, including <code>event.mask</code> if
	 *                   required
	 *                   {@link IWorkspace#addResourceChangeListener(IResourceChangeListener, int)}
	 */
	public void addResourceChangeListener(IResourceChangeListener listener, Map<String, Object> properties) {
		// TODO Add as public API https://bugs.eclipse.org/bugs/show_bug.cgi?id=564985
		Object mask = properties.get("event.mask"); //$NON-NLS-1$
		if (mask instanceof Integer) {
			workspace.addResourceChangeListener(listener, ((Integer) mask).intValue());
		} else {
			Logger local = this.logger;
			if (mask != null && local != null) {
				local.warn("event.mask of IResourceChangeListener service: expected Integer but was {} (from {}): {}", //$NON-NLS-1$
						mask.getClass(), listener.getClass(), mask);
			}
			workspace.addResourceChangeListener(listener);
		}
	}

	/**
	 * Deregister the {@link IResourceChangeListener} from this workspace
	 *
	 * @param listener the listener to deregister
	 */
	public void removeResourceChangeListener(IResourceChangeListener listener) {
		workspace.removeResourceChangeListener(listener);
	}

	/**
	 * Set the logger factory that can be used by this component
	 *
	 * @param factory the factory
	 */
	public void setLoggerFactory(LoggerFactory factory) {
		this.logger = factory.getLogger(ResourceChangeListenerRegistrar.class);
	}

	/**
	 * Unsets the logger generated from the associated logger factory
	 */
	public void unsetLogger() {
		this.logger = null;
	}
}
