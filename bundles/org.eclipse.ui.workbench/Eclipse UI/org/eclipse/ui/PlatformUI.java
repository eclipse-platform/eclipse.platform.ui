/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - add method to get preference store for a class
 *******************************************************************************/
package org.eclipse.ui;

import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.DialogSettingsProviderService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.dialogs.IDialogSettingsProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * The central class for access to the Eclipse Platform User Interface. This
 * class cannot be instantiated; all functionality is provided by static
 * methods.
 *
 * Features provided:
 * <ul>
 * <li>creation of the workbench.</li>
 * <li>access to the workbench.</li>
 * </ul>
 *
 * @see IWorkbench
 */
public final class PlatformUI {
	/**
	 * Identifies the workbench plug-in.
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui"; //$NON-NLS-1$

	/**
	 * Return code (value 0) indicating that the workbench terminated normally.
	 *
	 * @see #createAndRunWorkbench
	 * @since 3.0
	 */
	public static final int RETURN_OK = 0;

	/**
	 * Return code (value 1) indicating that the workbench was terminated with a
	 * call to <code>IWorkbench.restart</code>.
	 *
	 * @see #createAndRunWorkbench
	 * @see IWorkbench#restart
	 * @since 3.0
	 */
	public static final int RETURN_RESTART = 1;

	/**
	 * Return code (value 2) indicating that the workbench failed to start.
	 *
	 * @see #createAndRunWorkbench
	 * @see IWorkbench#restart
	 * @since 3.0
	 */
	public static final int RETURN_UNSTARTABLE = 2;

	/**
	 * Return code (value 3) indicating that the workbench was terminated with a
	 * call to IWorkbenchConfigurer#emergencyClose.
	 *
	 * @see #createAndRunWorkbench
	 * @since 3.0
	 */
	public static final int RETURN_EMERGENCY_CLOSE = 3;

	/**
	 * A supplier that supplies an {@link IllegalStateException} that a Workbench is
	 * not running.
	 *
	 * @since 3.126
	 */
	public static final Supplier<RuntimeException> NO_WORKBENCH = () -> new IllegalStateException(
			WorkbenchMessages.PlatformUI_NoWorkbench);

	/**
	 * Block instantiation.
	 */
	private PlatformUI() {
		// do nothing
	}

	/**
	 * Returns the workbench. Fails if the workbench has not been created yet.
	 *
	 * @return the workbench
	 */
	public static IWorkbench getWorkbench() {
		if (Workbench.getInstance() == null) {
			// app forgot to call createAndRunWorkbench beforehand
			throw new IllegalStateException(WorkbenchMessages.PlatformUI_NoWorkbench);
		}
		return Workbench.getInstance();
	}

	/**
	 * Returns whether {@link #createAndRunWorkbench createAndRunWorkbench} has been
	 * called to create the workbench, and the workbench has yet to terminate.
	 * <p>
	 * Note that this method may return <code>true</code> while the workbench is
	 * still being initialized, so it may not be safe to call workbench API methods
	 * even if this method returns true. See bug 49316 for details.
	 * </p>
	 *
	 * @return <code>true</code> if the workbench has been created and is still
	 *         running, and <code>false</code> if the workbench has not yet been
	 *         created or has completed
	 * @since 3.0
	 */
	public static boolean isWorkbenchRunning() {
		return Workbench.getInstance() != null && Workbench.getInstance().isRunning();
	}

	/**
	 * Creates the workbench and associates it with the given display and workbench
	 * advisor, and runs the workbench UI. This entails processing and dispatching
	 * events until the workbench is closed or restarted.
	 * <p>
	 * This method is intended to be called by the main class (the "application").
	 * Fails if the workbench UI has already been created.
	 * </p>
	 * <p>
	 * Use {@link #createDisplay createDisplay} to create the display to pass in.
	 * </p>
	 * <p>
	 * Note that this method is intended to be called by the application. It must be
	 * called exactly once, and early on before anyone else asks
	 * <code>getWorkbench()</code> for the workbench.
	 * </p>
	 *
	 * @param display the display to be used for all UI interactions with the
	 *                workbench
	 * @param advisor the application-specific advisor that configures and
	 *                specializes the workbench
	 * @return return code {@link #RETURN_OK RETURN_OK} for normal exit;
	 *         {@link #RETURN_RESTART RETURN_RESTART} if the workbench was
	 *         terminated with a call to {@link IWorkbench#restart
	 *         IWorkbench.restart}; {@link #RETURN_UNSTARTABLE RETURN_UNSTARTABLE}
	 *         if the workbench could not be started; {@link #RETURN_EMERGENCY_CLOSE
	 *         RETURN_EMERGENCY_CLOSE} if the UI quit because of an emergency; other
	 *         values reserved for future use
	 * @since 3.0
	 */
	public static int createAndRunWorkbench(Display display, WorkbenchAdvisor advisor) {
		return Workbench.createAndRunWorkbench(display, advisor);
	}

	/**
	 * Creates the <code>Display</code> to be used by the workbench. It is the
	 * caller's responsibility to dispose the resulting <code>Display</code>, not
	 * the workbench's.
	 *
	 * @return the display
	 * @since 3.0
	 */
	public static Display createDisplay() {
		return Workbench.createDisplay();
	}

	/**
	 * Returns the testable object facade, for use by the test harness.
	 * <p>
	 * IMPORTANT: This method is only for use by the test harness. Applications and
	 * regular plug-ins should not call this method.
	 * </p>
	 * <p>
	 * To avoid depending on the the Workbench a {@link TestableObject} can be
	 * obtained via OSGi service.
	 * </p>
	 *
	 * @return the testable object facade
	 * @since 3.0
	 */
	public static TestableObject getTestableObject() {
		// Try finding a pre-registered TO in the OSGi service registry
		TestableObject testableObject = WorkbenchPlugin.getDefault().getTestableObject();
		if (testableObject == null) {
			return Workbench.getWorkbenchTestable();
		}
		return testableObject;
	}

	/**
	 * Returns the preference store used for publicly settable workbench
	 * preferences. Constants for these preferences are defined on
	 * {@link org.eclipse.ui.IWorkbenchPreferenceConstants}.
	 *
	 * @return the workbench public preference store
	 * @since 3.0
	 */
	public static IPreferenceStore getPreferenceStore() {
		return PrefUtil.getAPIPreferenceStore();
	}

	/**
	 * Returns the dialog settings provider for the passed bundle. The dialog
	 * settings is used to hold persistent state data for the various wizards and
	 * dialogs of the bundle in the context of a workbench.
	 * <p>
	 * If an error occurs reading the dialog store, an empty one is quietly created
	 * and returned (errors are logged).
	 * </p>
	 *
	 * @param bundle the bundle for which the dialog settings are to be returned
	 *
	 * @return the dialog settings provider
	 * @since 3.122
	 */

	public static IDialogSettingsProvider getDialogSettingsProvider(Bundle bundle) {
		return DialogSettingsProviderService.instance.getProvider(bundle);
	}

	/**
	 * Creates a {@link IPreferenceStore} store for the bundle that loaded that
	 * class
	 *
	 * @param clazz the class to use for determining the responsible bundle
	 * @return the instance scoped preference store for the given class
	 * @since 3.117
	 */
	public static IPreferenceStore createPreferenceStore(Class<?> clazz) {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, FrameworkUtil.getBundle(clazz).getSymbolicName());
	}

	/**
	 * This methods allows code of eclipse.ui to gracefully migrate to E4 as it has
	 * the following properties:
	 *
	 * <ol>
	 * <li>It does not throw an exception but returns an empty optional if something
	 * is not ready (yet or anymore) so code can choose how to handle this (e.g. use
	 * {@link Optional#orElseThrow()} if required or {@link Optional#orElse(Object)}
	 * if a fallback exits.</li>
	 * <li>If this is is actually a (ui) Workbench running, this is returned and
	 * could be used</li>
	 * <li>If not the service registry is searched for an
	 * org.eclipse.e4.ui.workbench.IWorkbench so code can run inside a pure E4 (e.g.
	 * RCP) application as well.</li>
	 * <li>With the Application at hand, it is possible to get access to the
	 * {@link IEclipseContext} (see {@link MApplication#getContext()} to further
	 * interact with the plain E4 Application Model.</li>
	 * </ol>
	 *
	 * The following example fails if run inside an E4 Application: <code>
	 *  PlatformUI.getWorkbench().getExtensionTracker()
	 *  </code> and could be rewritten as: <code>
	 *  PlatformUI.getApplication().map(MApplication::getContext).map(ctx->ctx.get(IExtensionTracker)).orElseThrow(PlatformUI.NO_WORKBENCH);
	 *  </code>
	 *
	 * @return a reference to the {@link MApplication} running the Workbench or an
	 *         empty optional if currently no Workbench is available.
	 * @since 3.126
	 */
	public static Optional<MApplication> getApplication() {

		Workbench instance = Workbench.getInstance();
		if (instance != null && instance.isRunning()) {
			return Optional.of((instance.getApplication()));
		}
		WorkbenchPlugin plugin = WorkbenchPlugin.getDefault();
		if (plugin == null) {
			return Optional.empty();
		}
		// We intentionally do not use a tracker here to not bind it to much to the
		// service in case E4 is used
		BundleContext bundleContext = plugin.getBundleContext();
		if (bundleContext == null) {
			return Optional.empty();
		}
		ServiceReference<org.eclipse.e4.ui.workbench.IWorkbench> reference = bundleContext
				.getServiceReference(org.eclipse.e4.ui.workbench.IWorkbench.class);
		if (reference == null) {
			return Optional.empty();
		}
		org.eclipse.e4.ui.workbench.IWorkbench service = bundleContext.getService(reference);
		if (service == null) {
			return Optional.empty();
		}
		bundleContext.ungetService(reference);
		return Optional.ofNullable(service.getApplication());
	}
}
