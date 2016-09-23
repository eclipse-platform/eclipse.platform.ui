/*******************************************************************************
 * Copyright (c) 2008, 2015 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

import java.net.URI;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;

/**
 * A running instance of the workbench.
 *
 * This instance is published through:
 * <ul>
 * <li>the {@link IEclipseContext} of the application</li>
 * <li>the OSGi-Service-Registry</lI>
 * </ul>
 * <b>It is possible that there are multiple active {@link IWorkbench} instances in one
 * OSGi-Instance</b>
 *
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbench {
	/**
	 * The argument for whether the persisted state should be cleared on startup <br>
	 * <br>
	 * Value is: <code>clearPersistedState</code>
	 */
	public static final String CLEAR_PERSISTED_STATE = "clearPersistedState"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the resources referenced from the application CSS file <br>
	 * <br>
	 * Value is: <code>applicationCSSResources</code>
	 *
	 * @since 0.12.0
	 */
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the application CSS file <br>
	 * <br>
	 * Value is: <code>applicationCSS</code>
	 *
	 * @since 0.12.0
	 */
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the life-cycle manager <br>
	 * <br>
	 * Value is: <code>lifeCycleURI</code>
	 *
	 * @since 0.12.0
	 */
	public static final String LIFE_CYCLE_URI_ARG = "lifeCycleURI"; //$NON-NLS-1$
	/**
	 * The argument for the resource handler to use <br>
	 * <br>
	 * Value is: <code>modelResourceHandler</code>
	 */
	public static final String MODEL_RESOURCE_HANDLER = "modelResourceHandler"; //$NON-NLS-1$
	/**
	 * The argument for whether the workbench should save and restore its state <br>
	 * <<br>
	 * Value is: <code>persistState</code>
	 */
	public static final String PERSIST_STATE = "persistState"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the application presentation <br>
	 * <br>
	 * Value is: <code>presentationURI</code>
	 *
	 * @since 0.12.0
	 */
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the applicaton.xmi file <br>
	 * <br>
	 * Value is: <code>applicationXMI</code>
	 *
	 * @since 0.12.0
	 */
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$

	/**
	 * Context key to retrieve the application context in most applications
	 * (like e.g., e4). This context is the direct child of the root context
	 * which is retrieved from
	 * {@link EclipseContextFactory#getServiceContext(org.osgi.framework.BundleContext)}
	 *
	 * @since 1.4
	 */
	public final static String APPLICATION_CONTEXT_KEY = "applicationContext"; //$NON-NLS-1$

	/**
	 * This named context parameter is used to specify whether a {@link MPart}
	 * or a {@link MPlaceholder} are shown on top, which means the contents of
	 * it can be seen by the user in the UI.
	 * <p>
	 * This means clients can obtain the state of being on top by asking the
	 * part's context for the {@link IWorkbench#ON_TOP} key.
	 * </p>
	 * <p>
	 * Note that also objects created with a parts' context can obtain this
	 * {@link IWorkbench#ON_TOP} key, e.g., {@link MToolControl}.
	 * </p>
	 *
	 * <pre>
	 * &#64;Inject
	 * &#64;Optional
	 * private void onTop(&#64;Named(IWorkbench.ON_TOP) Boolean onTop) {
	 * 	if (onTop != null && onTop.booleanValue()) {
	 * 		// ... do something when element is on top
	 * 	}
	 * }
	 * </pre>
	 *
	 * @since 1.5
	 */
	public static final String ON_TOP = "elementOnTop"; //$NON-NLS-1$

	/**
	 * Close the workbench instance
	 *
	 * @return <code>true</code> if the shutdown succeeds
	 */
	public boolean close();

	/**
	 * @return the application model driving the workbench
	 */
	public MApplication getApplication();

	/**
	 * @return unique id of the instance
	 */
	public String getId();

	/**
	 * restart the workbench
	 *
	 * @return <code>false</code> if the restart is aborted
	 */
	public boolean restart();

}
