/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.preferences.PreferenceForwarder;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The abstract superclass of all plug-in runtime class
 * implementations. A plug-in subclasses this class and overrides
 * the appropriate life cycle methods in order to react to the life cycle 
 * requests automatically issued by the platform.
 * For compatibility reasons, the methods called for those life cycle events 
 * vary, please see the "Constructors and life cycle methods" section below. 
 *  
 * <p>
 * Conceptually, the plug-in runtime class represents the entire plug-in
 * rather than an implementation of any one particular extension the
 * plug-in declares. A plug-in is not required to explicitly
 * specify a plug-in runtime class; if none is specified, the plug-in
 * will be given a default plug-in runtime object that ignores all life 
 * cycle requests (it still provides access to the corresponding
 * plug-in descriptor).
 * </p>
 * <p>
 * In the case of more complex plug-ins, it may be desirable
 * to define a concrete subclass of <code>Plugin</code>.
 * However, just subclassing <code>Plugin</code> is not
 * sufficient. The name of the class must be explicitly configured
 * in the plug-in's manifest (<code>plugin.xml</code>) file
 * with the class attribute of the <code>&ltplugin&gt</code> element markup.
 * </p>
 * <p>
 * Instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation. For compatibility reasons, 
 * the constructor used to create plug-in instances varies, please see the "Constructors 
 * and life cycle methods" section below.
 * </p><p>
 * The concept of bundles underlies plug-ins. However it is safe to regard plug-ins 
 * and bundles as synonyms. 
 * </p>
 * <p>
 * <b>Clients must never explicitly instantiate a plug-in runtime class</b>.
 * </p>
 * <p>
 * A typical implementation pattern for plug-in runtime classes is to
 * provide a static convenience method to gain access to a plug-in's
 * runtime object. This way, code in other parts of the plug-in
 * implementation without direct access to the plug-in runtime object
 * can easily obtain a reference to it, and thence to any plug-in-wide
 * resources recorded on it. An example for Eclipse 3.0 follows:
 * <pre>
 *     package myplugin;
 *     public class MyPluginClass extends Plugin {
 *         private static MyPluginClass instance;
 *
 *         public static MyPluginClass getInstance() { return instance; }
 *
 *         public void MyPluginClass() {
 *             super();
 *             instance = this;
 *             // ... other initialization
 *         }
 *         // ... other methods
 *     }
 * </pre>
 * In the above example, a call to <code>MyPluginClass.getInstance()</code>
 * will always return an initialized instance of <code>MyPluginClass</code>.
 * </p>
 * <p>
 * <b>Constructors and life cycle methods</b> 
 * </p><p>
 * If the plugin.xml of a plug-in indicates &lt;?eclipse version="3.0"?&gt; and its prerequisite
 * list includes <code>org.eclipse.core.runtime</code>, the default constructor of the plug-in 
 * class is used and {@link #start(BundleContext)} and {@link #stop(BundleContext)} are
 * called as life cycle methods.    
 * </p><p>
 * If the plugin.xml of a plug-in indicates &lt;?eclipse version="3.0"?&gt; and its prerequisite list includes
 * <code>org.eclipse.core.runtime.compatibility</code>, the {@link #Plugin(IPluginDescriptor)}
 * constructor is used and {@link #startup()} and {@link #shutdown()} are called as life cycle methods.
 * Note that in this situation, start() is called before startup() and stop() is called
 * after shutdown. 
 * </p><p>
 * If the plugin.xml of your plug-in does <b>not</b> indicate &lt;?eclipse version="3.0"?&gt; it is therefore
 * not a 3.0 plug-in. Consequently the {@link #Plugin(IPluginDescriptor)} is used and {@link #startup()} and 
 * {@link #shutdown()} are called as life cycle methods.
 * </p>
 */
public abstract class Plugin implements BundleActivator {

	/**
	 * String constant used for the default scope name for legacy 
	 * Eclipse plug-in preferences. 
	 * 
	 * @since 3.0
	 */
	public static final String PLUGIN_PREFERENCE_SCOPE = InstanceScope.SCOPE;

	/**
	 * The bundle associated this plug-in
	 */
	private Bundle bundle;

	/**
	 * The debug flag for this plug-in.  The flag is false by default.
	 * It can be set to true either by the plug-in itself or in the platform 
	 * debug options.
	 */
	private boolean debug = false;

	/** The plug-in descriptor.
	 * @deprecated Marked as deprecated to suppress deprecation warnings.
	 */
	private IPluginDescriptor descriptor;

	/**
	 * The name of the file (value <code>"preferences.ini"</code>) in a
	 * plug-in's (read-only) directory that, when present, contains values that
	 * override the normal default values for this plug-in's preferences.
	 * <p>
	 * The format of the file is as per <code>java.io.Properties</code> where
	 * the keys are property names and values are strings.
	 * </p>
	 * 
	 * @since 2.0
	 */
	public static final String PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME = "preferences"; //$NON-NLS-1$
	public static final String PREFERENCES_DEFAULT_OVERRIDE_FILE_NAME = PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME + ".ini"; //$NON-NLS-1$

	/**
	 * The preference object for this plug-in; initially <code>null</code>
	 * meaning not yet created and initialized.
	 * 
	 * @since 2.0
	 */
	private PreferenceForwarder preferences = null;

	/**
	 * Creates a new plug-in runtime object.  This method is called by the platform
	 * if this class is used as a <code>BundleActivator</code>.  This method is not 
	 * needed/used if this plug-in requires the org.eclipse.core.runtime.compatibility plug-in.  
	 * Subclasses of <code>Plugin</code> 
	 * must call this method first in their constructors.  
	 * 
	 * The resultant instance is not managed by the runtime and
	 * so should be remembered by the client (typically using a Singleton pattern).
	 * <b>Clients must never explicitly call this method.</b>
	 * </p> 
	 * <p>
	 * Note: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public Plugin() {
		super();
	}

	/**
	 * Creates a new plug-in runtime object for the given plug-in descriptor.
	 * <p>
	 * Instances of plug-in runtime classes are automatically created 
	 * by the platform in the course of plug-in activation.
	 * <b>Clients must never explicitly call this method.</b>
	 * </p>
	 * <p>
	 * Note: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 *
	 * @param descriptor the plug-in descriptor
	 * @see #getDescriptor()
	 * @deprecated
	 * In Eclipse 3.0 this constructor has been replaced by {@link #Plugin()}.
	 * Implementations of <code>MyPlugin(IPluginDescriptor descriptor)</code> should be changed to 
	 * <code>MyPlugin()</code> and call <code>super()</code> instead of <code>super(descriptor)</code>.
	 * The <code>MyPlugin(IPluginDescriptor descriptor)</code> constructor is called only for plug-ins 
	 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
	 */
	public Plugin(IPluginDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		Assert.isTrue(!CompatibilityHelper.hasPluginObject(descriptor), NLS.bind(Messages.plugin_deactivatedLoad, this.getClass().getName(), descriptor.getUniqueIdentifier() + " is not activated")); //$NON-NLS-1$
		this.descriptor = descriptor;
		
		// on plugin start, find and start the corresponding bundle.
		bundle = InternalPlatform.getDefault().getBundle(descriptor.getUniqueIdentifier());
		try {
			if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) == 0)
				bundle.start();
		} catch (BundleException e) {
			String message = NLS.bind(Messages.plugin_startupProblems, descriptor.getUniqueIdentifier());
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e);
			InternalPlatform.getDefault().log(status);
		}
	}

	/**
	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * @param path path relative to plug-in installation location 
	 * @return a URL for the given path or <code>null</code>
	 */
	public final URL find(IPath path) {
		return FindSupport.find(bundle, path, null);
	}

	/**
	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * @param path file path relative to plug-in installation location
	 * @param override map of override substitution arguments to be used for
	 * any $arg$ path elements. The map keys correspond to the substitution
	 * arguments (eg. "$nl$" or "$os$"). The resulting
	 * values must be of type java.lang.String. If the map is <code>null</code>,
	 * or does not contain the required substitution argument, the default
	 * is used.
	 * @return a URL for the given path or <code>null</code>
	 */
	public final URL find(IPath path, Map override) {
		return FindSupport.find(bundle, path, override);
	}

	/**
	 * Returns the plug-in descriptor for this plug-in runtime object.
	 *
	 * @return the plug-in descriptor for this plug-in runtime object
	 * @deprecated 
	 * <code>IPluginDescriptor</code> was refactored in Eclipse 3.0.
	 * The <code>getDescriptor()</code> method may only be called by plug-ins 
	 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
	 * See the comments on {@link IPluginDescriptor} and its methods for details.
	 */
	public final IPluginDescriptor getDescriptor() {
		if (descriptor != null)
			return descriptor;
		
		return initializeDescriptor(bundle.getSymbolicName());
	}

	/**
	 * Returns the log for this plug-in.  If no such log exists, one is created.
	 *
	 * @return the log for this plug-in
	 */
	public final ILog getLog() {
		return InternalPlatform.getDefault().getLog(bundle);
	}

	/**
	 * Returns the location in the local file system of the 
	 * plug-in state area for this plug-in.
	 * If the plug-in state area did not exist prior to this call,
	 * it is created.
	 * <p>
	 * The plug-in state area is a file directory within the
	 * platform's metadata area where a plug-in is free to create files.
	 * The content and structure of this area is defined by the plug-in,
	 * and the particular plug-in is solely responsible for any files
	 * it puts there. It is recommended for plug-in preference settings and 
	 * other configuration parameters.
	 * </p>
	 * @throws IllegalStateException, when the system is running with no data area (-data @none),
	 * or when a data area has not been set yet.
	 * @return a local file system path
	 */
	public final IPath getStateLocation() throws IllegalStateException {
		return InternalPlatform.getDefault().getStateLocation(bundle, true);
	}

	/**
	 * Returns the preference store for this plug-in.
	 * <p>
	 * Note that if an error occurs reading the preference store from disk, an empty 
	 * preference store is quietly created, initialized with defaults, and returned.
	 * </p>
	 * <p>
	 * Calling this method may cause the preference store to be created and
	 * initialized. Subclasses which reimplement the 
	 * <code>initializeDefaultPluginPreferences</code> method have this opportunity
	 * to initialize preference default values, just prior to processing override
	 * default values imposed externally to this plug-in (specified for the product,
	 * or at platform start up).
	 * </p>
	 * <p>
	 * After settings in the preference store are changed (for example, with 
	 * <code>Preferences.setValue</code> or <code>setToDefault</code>),
	 * <code>savePluginPreferences</code> should be called to store the changed
	 * values back to disk. Otherwise the changes will be lost on plug-in
	 * shutdown.
	 * </p>
	 *
	 * @return the preference store
	 * @see #savePluginPreferences()
	 * @see Preferences#setValue(String, String)
	 * @see Preferences#setToDefault(String)
	 * @since 2.0
	 */
	public final Preferences getPluginPreferences() {
		if (preferences != null) {
			if (InternalPlatform.DEBUG_PREFERENCE_GENERAL)
				Policy.debug("Plugin preferences already loaded for: " + bundle.getSymbolicName()); //$NON-NLS-1$
			return preferences;
		}

		if (InternalPlatform.DEBUG_PREFERENCE_GENERAL)
			Policy.debug("Loading preferences for plugin: " + bundle.getSymbolicName()); //$NON-NLS-1$
		preferences = new PreferenceForwarder(this, bundle.getSymbolicName());
		return preferences;
	}

	/**
	 * Saves preferences settings for this plug-in. Does nothing if the preference
	 * store does not need saving.
	 * <p>
	 * Plug-in preferences are <b>not</b> saved automatically on plug-in shutdown.
	 * </p>
	 * 
	 * @see Preferences#store(OutputStream, String)
	 * @see Preferences#needsSaving()
	 * @since 2.0
	 */
	public final void savePluginPreferences() {
		// populate the "preferences" instvar. We still might
		// need to save them because someone else might have
		// made changes via the OSGi APIs.
		getPluginPreferences();
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, Messages.preferences_saveProblems, e);
			InternalPlatform.getDefault().log(status);
		}
	}

	/**
	 * Initializes the default preferences settings for this plug-in.
	 * <p>
	 * This method is called sometime after the preference store for this
	 * plug-in is created. Default values are never stored in preference
	 * stores; they must be filled in each time. This method provides the
	 * opportunity to initialize the default values.
	 * </p>
	 * <p>
	 * The default implementation of this method does nothing. A subclass that needs
	 * to set default values for its preferences must reimplement this method.
	 * Default values set at a later point will override any default override
	 * settings supplied from outside the plug-in (product configuration or
	 * platform start up).
	 * </p>
	 * @since 2.0
	 * @deprecated
	 * This method has been refactored in the new preference mechanism 
	 * to handle the case where the runtime compatibility layer does not exist. The 
	 * contents of this method should be moved to the method named 
	 * <code>initializeDefaultPreferences</code> in a separate subclass of 
	 * {@link org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer}. 
	 * This class should be contributed via the
	 * <code>org.eclipse.core.runtime.preferences</code> extension point.
	 * <pre>
	 * 	&lt;extension point=&quo;org.eclipse.core.runtime.preferences&quo;&gt;
	 *			&lt;initializer class=&quo;com.example.MyPreferenceInitializer&quo;/&gt;
	 *		&lt;/extension&gt;
	 *		...
	 *		package com.example;
	 *		public class MyPreferenceInitializer extends AbstractPreferenceInitializer {
	 *			public MyPreferenceInitializer() {
	 *				super();
	 *			}
	 *			public void initializeDefaultPreferences() {
	 *				Preferences node = new DefaultScope().getNode("my.plugin.id");
	 *				node.put(key, value);
	 *			}
	 *		}
	 * </pre>
	 */
	protected void initializeDefaultPluginPreferences() {
		// default implementation of this method - spec'd to do nothing
	}

	/**
	 * Internal method. This method is a hook for
	 * initialization of default preference values. 
	 * It should not be called by clients.
	 * 
	 * @since 3.0
	 */
	public final void internalInitializeDefaultPluginPreferences() {
		initializeDefaultPluginPreferences();
	}

	/**
	 * Returns whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set an execution option to do so.
	 * <p>
	 * Note that the plug-in's debug flag is initialized when the 
	 * plug-in is started. The result of calling this method before the plug-in
	 * has started is unspecified.
	 * </p>
	 *
	 * @return whether this plug-in is in debug mode
	 */
	public boolean isDebugging() {
		return debug;
	}

	/**
	 * Returns an input stream for the specified file. The file path
	 * must be specified relative this the plug-in's installation location.
	 *
	 * @param file path relative to plug-in installation location
	 * @return an input stream
	 * @exception IOException if the given path cannot be found in this plug-in
	 * 
	 * @see #openStream(IPath,boolean)
	 */
	public final InputStream openStream(IPath file) throws IOException {
		return FindSupport.openStream(bundle, file, false);
	}

	/**
	 * Returns an input stream for the specified file. The file path
	 * must be specified relative to this plug-in's installation location.
	 * Optionally, the platform searches for the correct localized version
	 * of the specified file using the users current locale, and Java
	 * naming convention for localized resource files (locale suffix appended 
	 * to the specified file extension).
	 * <p>
	 * The caller must close the returned stream when done.
	 * </p>
	 *
	 * @param file path relative to plug-in installation location
	 * @param localized <code>true</code> for the localized version
	 *   of the file, and <code>false</code> for the file exactly
	 *   as specified
	 * @return an input stream
	 * @exception IOException if the given path cannot be found in this plug-in
	 */
	public final InputStream openStream(IPath file, boolean localized) throws IOException {
		return FindSupport.openStream(bundle, file, localized);
	}

	/**
	 * Sets whether this plug-in is in debug mode.
	 * By default plug-ins are not in debug mode.  A plug-in can put itself
	 * into debug mode or the user can set a debug option to do so.
	 * <p>
	 * Note that the plug-in's debug flag is initialized when the 
	 * plug-in is started. The result of calling this method before the plug-in
	 * has started is unspecified.
	 * </p>
	 *
	 * @param value whether or not this plug-in is in debug mode
	 */
	public void setDebugging(boolean value) {
		debug = value;
	}

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the plug-in is shut down.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * Plug-in shutdown code should be robust. In particular, this method
	 * should always make an effort to shut down the plug-in. Furthermore,
	 * the code should not assume that the plug-in was started successfully,
	 * as this method will be invoked in the event of a failure during startup.
	 * </p>
	 * <p>
	 * Note 1: If a plug-in has been started, this method will be automatically
	 * invoked by the platform when the platform is shut down.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple termination
	 * of the plug-in environment. The platform may terminate invocations
	 * that do not complete in a timely fashion.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 * <p>
	 *
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in
	 * @deprecated 
	 * In Eclipse 3.0 this method has been replaced by {@link Plugin#stop(BundleContext context)}.
	 * Implementations of <code>shutdown()</code> should be changed to override 
	 * <code>stop(BundleContext context)</code> and call <code>super.stop(context)</code> 
	 * instead of <code>super.shutdown()</code>.
	 * The <code>shutdown()</code> method is called only for plug-ins which explicitly require the 
	 * org.eclipse.core.runtime.compatibility plug-in.
	 */
	public void shutdown() throws CoreException {
		if (CompatibilityHelper.initializeCompatibility() == null)
			return;
		Throwable exception = null;
		Method m;
		try {
			m = descriptor.getClass().getMethod("doPluginDeactivation", new Class[0]); //$NON-NLS-1$
			m.invoke(descriptor, null);
		} catch (SecurityException e) {
			exception = e;
		} catch (NoSuchMethodException e) {
			exception = e;
		} catch (IllegalArgumentException e) {
			exception = e;
		} catch (IllegalAccessException e) {
			exception = e;
		} catch (InvocationTargetException e) {
			exception = e;
		}
		if (exception == null)
			return;
		String message = NLS.bind(Messages.plugin_shutdownProblems, descriptor.getUniqueIdentifier());
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, exception);
		InternalPlatform.getDefault().log(status);
	}

	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method should be overridden in subclasses that need to do something
	 * when this plug-in is started.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * If this method throws an exception, it is taken as an indication that
	 * plug-in initialization has failed; as a result, the plug-in will not
	 * be activated; moreover, the plug-in will be marked as disabled and 
	 * ineligible for activation for the duration.
	 * </p>
	 * <p>
	 * Plug-in startup code should be robust. In the event of a startup failure,
	 * the plug-in's <code>shutdown</code> method will be invoked automatically,
	 * in an attempt to close open files, etc.
	 * </p>
	 * <p>
	 * Note 1: This method is automatically invoked by the platform 
	 * the first time any code in the plug-in is executed.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple initialization 
	 * of the plug-in environment. The platform may terminate initializers 
	 * that do not complete in a timely fashion.
	 * </p>
	 * <p>
	 * Note 3: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 * <p>
	 *
	 * @exception CoreException if this plug-in did not start up properly
	 * @deprecated 
	 * In Eclipse 3.0 this method has been replaced by {@link Plugin#start(BundleContext context)}.
	 * Implementations of <code>startup()</code> should be changed to extend
	 * <code>start(BundleContext context)</code> and call <code>super.start(context)</code>
	 * instead of <code>super.startup()</code>.
	 * The <code>startup()</code> method is called only for plug-ins which explicitly require the 
	 * org.eclipse.core.runtime.compatibility plug-in.
	 */
	public void startup() throws CoreException {
	}

	/**
	 * Returns a string representation of the plug-in, suitable 
	 * for debugging purposes only.
	 */
	public String toString() {
		String name = bundle.getSymbolicName(); 
		return name==null ? new Long(bundle.getBundleId()).toString() : name;
	}

	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method should be overridden in subclasses that need to do something
	 * when this plug-in is started.  Implementors should call the inherited method
	 * at the first possible point to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * If this method throws an exception, it is taken as an indication that
	 * plug-in initialization has failed; as a result, the plug-in will not
	 * be activated; moreover, the plug-in will be marked as disabled and 
	 * ineligible for activation for the duration.
	 * </p>
	 * <p>
	 * Plug-in startup code should be robust. In the event of a startup failure,
	 * the plug-in's <code>shutdown</code> method will be invoked automatically,
	 * in an attempt to close open files, etc.
	 * </p>
	 * <p>
	 * Note 1: This method is automatically invoked by the platform 
	 * the first time any code in the plug-in is executed.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple initialization 
	 * of the plug-in environment. The platform may terminate initializers 
	 * that do not complete in a timely fashion.
	 * </p>
	 * <p>
	 * Note 3: The class loader typically has monitors acquired during invocation of this method.  It is 
	 * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
	 * as this would lead to deadlock vulnerability.
	 * </p>
	 * <p>
	 * Note 4: The supplied bundle context represents the plug-in to the OSGi framework.
	 * For security reasons, it is strongly recommended that this object should not be divulged.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 *
	 * @param context the bundle context for this plug-in
	 * @exception Exception if this plug-in did not start up properly
	 * @since 3.0
	 */
	public void start(BundleContext context) throws Exception {
		bundle = context.getBundle();

		String symbolicName = bundle.getSymbolicName();
		if (symbolicName != null) {
			String key = symbolicName + "/debug"; //$NON-NLS-1$
			String value = InternalPlatform.getDefault().getOption(key);
			this.debug = value == null ? false : value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		
		initializeDescriptor(symbolicName);
	}

	/**
	 * @deprecated Marked as deprecated to suppress deprecation warnings.
	 */
	private IPluginDescriptor initializeDescriptor(String symbolicName) {
		if (CompatibilityHelper.initializeCompatibility() == null)
			return null;
		
		//This associate a descriptor to any real bundle that uses this to start
		if (symbolicName == null)
			return null;
		
		IPluginDescriptor tmp = CompatibilityHelper.getPluginDescriptor(symbolicName);
		
		//Runtime descriptor is never set to support dynamic re-installation of compatibility 
		if (!symbolicName.equals(Platform.PI_RUNTIME))
			descriptor =  tmp;
		
		CompatibilityHelper.setPlugin(tmp, this);
		CompatibilityHelper.setActive(tmp);
		return tmp;
	}

	/**
	 * Stops this plug-in.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the plug-in is shut down.  Implementors should call the inherited method
	 * as late as possible to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * Plug-in shutdown code should be robust. In particular, this method
	 * should always make an effort to shut down the plug-in. Furthermore,
	 * the code should not assume that the plug-in was started successfully,
	 * as this method will be invoked in the event of a failure during startup.
	 * </p>
	 * <p>
	 * Note 1: If a plug-in has been automatically started, this method will be automatically
	 * invoked by the platform when the platform is shut down.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple termination
	 * of the plug-in environment. The platform may terminate invocations
	 * that do not complete in a timely fashion.
	 * </p>
	 * <p>
	 * Note 3: The supplied bundle context represents the plug-in to the OSGi framework.
	 * For security reasons, it is strongly recommended that this object should not be divulged.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 * 
	 * @param context the bundle context for this plug-in
	 * @exception Exception if this method fails to shut down this plug-in
	 * @since 3.0
	 */
	public void stop(BundleContext context) throws Exception {
		// sub-classes to override
	}

	/**
	 * Returns the bundle associated with this plug-in.
	 * 
	 * @return the associated bundle
	 * @since 3.0
	 */
	public final Bundle getBundle() {
		return bundle;
	}
}
