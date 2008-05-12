/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.net.URL;
import java.util.*;

/**
 * A plug-in descriptor contains information about a plug-in
 * obtained from the plug-in's manifest (<code>plugin.xml</code>) file.
 * <p>
 * Plug-in descriptors are platform-defined objects that exist
 * in the plug-in registry independent of whether a plug-in has
 * been started. In contrast, a plug-in's runtime object 
 * (<code>getPlugin</code>) generally runs plug-in-defined code.
 * </p>
 * 
 * @deprecated 
 * <code>IPluginDescriptor</code> was refactored in Eclipse 3.0.
 * Most of the functionality has moved to {@link Platform} and the 
 * plug-in descriptor has been replaced with the OSGi <code>Bundle</code> 
 * object.
 * <p>
 * This interface must only be used by plug-ins 
 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
 * </p>
 * <p>
 * For most uses the bundle object can be treated as an opaque token
 * representing your plug-in to the system -- It must be supplied to various 
 * <code>Platform</code> methods but need not be interrogated itself.
 * There are a small number of plug-in descriptor method equivalents 
 * supplied by <code>Bundle</code> itself.  The details are spelled out
 * in the comments on each <code>IPluginDescriptor</code> method.
 * </p>
 * <p>
 * Clients of this interface have a reference to an <code>IPluginDescriptor</code> 
 * corresponding to a plug-in.  To adapt to the deprecation, the bundle corresponding
 * to the plug-in generally needs to be acquired. There are several cases:
 * <ul>
 * <li>the descriptor was discovered using methods such as 
 * <code>IPluginRegistry.getPluginDescriptor()</code>.  The code should be 
 * updated to use one of the bundle discovery mechanisms such as 
 * <code>Platform.getBundle()</code>.
 * </li>
 * <li>the descriptor is from the plug-in itself.  The code should be updated to
 * use the plug-in's bundle instead.
 * </li>
 * <li>the descriptor is supplied by a third party.  The plug-in writer must 
 * cooperate with that party to obtain a bundle rather than a descriptor.
 * </li>
 * </ul>
 * </p>
 * <p>
 * The resulting bundle object can be used
 * to carry out the adaptation steps outlined for each of the 
 * <code>IPluginDescriptor</code> methods.
 * </p>
 * 
 * @see #getPlugin()
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPluginDescriptor {
	/**
	 * Returns the extension with the given simple identifier declared in
	 * this plug-in, or <code>null</code> if there is no such extension.
	 * Since an extension might not have an identifier, some extensions
	 * can only be found via the <code>getExtensions</code> method.
	 *
	 * @param extensionName the simple identifier of the extension (e.g. <code>"main"</code>).
	 * @return the extension, or <code>null</code>
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getExtensionRegistry().getExtension(id + "." + extensionName) 
	 * </pre>
	 * where <code>id</code> is the plug-in id.
	 */
	public IExtension getExtension(String extensionName);

	/**
	 * Returns the extension point with the given simple identifier
	 * declared in this plug-in, or <code>null</code> if there is no such extension point.
	 *
	 * @param extensionPointId the simple identifier of the extension point (e.g. <code>"wizard"</code>).
	 * @return the extension point, or <code>null</code>
	 * @deprecated  
	 * Use 
	 * <pre>
	 *     Platform.getExtensionRegistry().getExtensionPoint(id, extensionPointId) 
	 * </pre>
	 * where <code>id</code> is the plug-in id.
	 */
	public IExtensionPoint getExtensionPoint(String extensionPointId);

	/**
	 * Returns all extension points declared by this plug-in.
	 * Returns an empty array if this plug-in does not declare any extension points.
	 *
	 * @return the extension points declared by this plug-in
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getExtensionRegistry().getExtensionPoints(id) 
	 * </pre>
	 * where <code>id</code> is the plug-in id.
	 */
	public IExtensionPoint[] getExtensionPoints();

	/**
	 * Returns all extensions declared by this plug-in.
	 * Returns an empty array if this plug-in does not declare any extensions.
	 *
	 * @return the extensions declared by this plug-in
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getExtensionRegistry().getExtensions(id) 
	 * </pre>
	 * where <code>id</code> is the plug-in id.
	 */
	public IExtension[] getExtensions();

	/**
	 * Returns the URL of this plug-in's install directory. 
	 * This is the directory containing
	 * the plug-in manifest file, resource bundle, runtime libraries,
	 * and any other files supplied with this plug-in. This directory is usually
	 * read-only. Plug-in relative information should be written to the location 
	 * provided by <code>Plugin.getStateLocation</code>.
	 *
	 * @return the URL of this plug-in's install directory
	 * @see #getPlugin()
	 * @see Plugin#getStateLocation()
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     bundle.getEntry("/");
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in or simply use the <code>getEntry</code> API
	 * to construct the desired URL.
	 */
	public URL getInstallURL();

	/**
	 * Returns a displayable label for this plug-in.
	 * Returns the empty string if no label for this plug-in
	 * is specified in the plug-in manifest file.
	 * <p> Note that any translation specified in the plug-in manifest
	 * file is automatically applied.
	 * </p>
	 *
	 * @return a displayable string label for this plug-in,
	 *    possibly the empty string
	 * @see #getResourceString(String)
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME) 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public String getLabel();

	/**
	 * Returns the plug-in runtime object corresponding to this
	 * plug-in descriptor. Unlike other methods on this object,
	 * invoking this method may activate the plug-in.
	 * The returned object is an instance of the plug-in runtime class
	 * specified in the plug-in's manifest file;
	 * if a class is not specified there, the returned object
	 * is an internally-supplied one that does not react to life cycle requests.
	 *
	 * @return the plug-in runtime object
	 * @exception CoreException 
	 *   if this plug-in's runtime object could not be created.
	 * @see #isPluginActivated()
	 * @deprecated 
	 * In Eclipse 3.0 plug-in runtime objects are not globally managed and
	 * so are not generically accessible.  Rather, each plug-in is free to declare
	 * API which exposes the plug-in runtime object (e.g., <code>MyPlugin.getInstance()</code>).
	 * 
	 * If this method is being used to activate a plug-in then the following code is
	 * equivalent:
	 * <pre>
	 *     bundle.start()
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public Plugin getPlugin() throws CoreException;

	/**
	 * Returns the plug-in class loader used to load classes and resources
	 * for this plug-in. The class loader can be used to directly access
	 * plug-in resources and classes. Note that accessing a resource will
	 * <b>not activate</b> the corresponding plug-in. Successfully loading 
	 * a class will <b>always activate</b> the corresponding plug-in.
	 * <p> 
	 * The following examples illustrate the direct use of the plug-in class
	 * loader and its effect on plug-in activation (example ignores error
	 * handling).
	 *
	 * <pre>
	 *     ClassLoader loader = descriptor.getPluginClassLoader();
	 *
	 *     // Load resource by name. Will not activate the plug-in.
	 *     URL res = loader.getResource("com/example/Foo/button.gif");
	 *     InputStream is = loader.getResourceAsStream("splash.jpg");
	 *
	 *     // Load resource for class. Will activate the plug-in because
	 *     // the referenced class is loaded first and triggers activation.
	 *     URL u = com.example.Foo.class.getResource("button.gif");
	 *
	 *     // Load class by name. Will activate the plug-in.
	 *     Class c = loader.loadClass("com.example.Bar");
	 *
	 *     // Load a resource bundle. May, or may not activate the plug-in, depending
	 *     // on the bundle implementation. If implemented as a class, the plug-in
	 *     // will be activated. If implemented as a properties file, the plug-in will
	 *     // not be activated.
	 *     ResourceBundle b = 
	 *         ResourceBundle.getBundle("bundle", Locale.getDefault(), loader);
	 * </pre>
	 *
	 * @return the plug-in class loader
	 * @see IConfigurationElement#createExecutableExtension(String)
	 * @see #isPluginActivated()
	 * @see #getResourceBundle()
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     bundle.loadClass(className)
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public ClassLoader getPluginClassLoader();

	/**
	 * Returns a list of plug-in prerequisites required
	 * for correct execution of this plug-in.
	 *
	 * @return an array of plug-in prerequisites, or an empty array
	 * if no prerequisites were specified
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     import org.eclipse.osgi.util.ManifestElement;
	 *     import org.osgi.framework.Constants;
	 *     ...
	 *     String requires = (String)bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
	 *     ManifestElement[] elements = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, requires);
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.  The resultant elements array contains one
	 * entry for each required plug-in.
	 */
	public IPluginPrerequisite[] getPluginPrerequisites();

	/**
	 * Returns the name of the provider of this plug-in.
	 * Returns the empty string if no provider name is specified in 
	 * the plug-in manifest file.
	 * <p> Note that any translation specified in the plug-in manifest
	 * file is automatically applied.
	 * </p>
	 * @return the name of the provider, possibly the empty string
	 * @see #getResourceString(String)
	 * @deprecated
	 * Use 
	 * <pre>
	 *     bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VENDOR) 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public String getProviderName();

	/**
	 * Returns this plug-in's resource bundle for the current locale. 
	 * <p>
	 * The bundle is stored as the <code>plugin.properties</code> file 
	 * in the plug-in install directory, and contains any translatable
	 * strings used in the plug-in manifest file (<code>plugin.xml</code>)
	 * along with other resource strings used by the plug-in implementation.
	 * </p>
	 *
	 * @return the resource bundle
	 * @exception MissingResourceException if the resource bundle was not found
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getResourceBundle(bundle)
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public ResourceBundle getResourceBundle() throws MissingResourceException;

	/**
	 * Returns a resource string corresponding to the given argument value.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the default resource bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed in the
	 * plugin.properties resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string beginning with the "%" character.
	 * Note, that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * Equivalent to <code>getResourceString(value, getResourceBundle())</code>
	 * </p>
	 *
	 * @param value the value
	 * @return the resource string
	 * @see #getResourceBundle()
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getResourceString(bundle, value)
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public String getResourceString(String value);

	/**
	 * Returns a resource string corresponding to the given argument 
	 * value and bundle.
	 * If the argument value specifies a resource key, the string
	 * is looked up in the given resource bundle. If the argument does not
	 * specify a valid key, the argument itself is returned as the
	 * resource string. The key lookup is performed against the
	 * specified resource bundle. If a resource string 
	 * corresponding to the key is not found in the resource bundle
	 * the key value, or any default text following the key in the
	 * argument value is returned as the resource string.
	 * A key is identified as a string beginning with the "%" character.
	 * Note that the "%" character is stripped off prior to lookup
	 * in the resource bundle.
	 * <p>
	 * For example, assume resource bundle plugin.properties contains
	 * name = Project Name
	 * <pre>
	 *     getResourceString("Hello World") returns "Hello World"</li>
	 *     getResourceString("%name") returns "Project Name"</li>
	 *     getResourceString("%name Hello World") returns "Project Name"</li>
	 *     getResourceString("%abcd Hello World") returns "Hello World"</li>
	 *     getResourceString("%abcd") returns "%abcd"</li>
	 *     getResourceString("%%name") returns "%name"</li>
	 * </pre>
	 * </p>
	 *
	 * @param value the value
	 * @param resourceBundle the resource bundle
	 * @return the resource string
	 * @see #getResourceBundle()
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.getResourceString(bundle, value, resourceBundle) 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public String getResourceString(String value, ResourceBundle resourceBundle);

	/**
	 * Returns all runtime libraries declared by this plug-in.
	 * Returns an empty array if this plug-in has no runtime libraries.
	 *
	 * @return the runtime libraries declared by this plug-in
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     import org.eclipse.osgi.util.ManifestElement;
	 *     import org.osgi.framework.Constants;
	 *     ...
	 *     String requires = (String)bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
	 *     ManifestElement[] elements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, requires);
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.  The resultant elements array contains one
	 * entry for each entry on the bundle's classpath..
	 */
	public ILibrary[] getRuntimeLibraries();

	/**
	 * Returns the unique identifier of this plug-in.
	 * This identifier is a non-empty string and is unique 
	 * within the plug-in registry.
	 *
	 * @return the unique identifier of the plug-in (e.g. <code>"org.eclipse.core.runtime"</code>)
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     bundle.getSymbolicName() 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public String getUniqueIdentifier();

	/**
	 * Returns the version identifier of this plug-in.
	 *
	 * @return the plug-in version identifier
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     String version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
	 *     new PluginVersionIdentifier(version); 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public PluginVersionIdentifier getVersionIdentifier();

	/**
	 * Returns whether the plug-in described by this descriptor
	 * has been activated. Invoking this method will not cause the
	 * plug-in to be activated.
	 *
	 * @return <code>true</code> if this plug-in is activated, and
	 *   <code>false</code> otherwise
	 * @see #getPlugin()
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     bundle.getState == org.osgi.framework.Bundle.ACTIVE 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public boolean isPluginActivated();

	/**
	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * @param path path relative to plug-in installation location 
	 * @return a URL for the given path or <code>null</code>  It is not
	 * necessary to perform a 'resolve' on this URL.
	 * 
	 * @since 2.0
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.find(bundle, file) 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public URL find(IPath path);

	/**
	 * Returns a URL for the given path.  Returns <code>null</code> if the URL
	 * could not be computed or created.
	 * 
	 * find will look for this path under the directory structure for this plugin
	 * and any of its fragments.  If this path will yield a result outside the
	 * scope of this plugin, <code>null</code> will be returned.  Note that
	 * there is no specific order to the fragments.
	 * 
	 * The following arguments may also be used
	 * 
	 *  $nl$ - for language specific information
	 *  $os$ - for operating system specific information
	 *  $ws$ - for windowing system specific information
	 * 
	 * A path of $nl$/about.properties in an environment with a default 
	 * locale of en_CA will return a URL corresponding to the first place
	 * about.properties is found according to the following order:
	 *   plugin root/nl/en/CA/about.properties
	 *   fragment1 root/nl/en/CA/about.properties
	 *   fragment2 root/nl/en/CA/about.properties
	 *   ...
	 *   plugin root/nl/en/about.properties
	 *   fragment1 root/nl/en/about.properties
	 *   fragment2 root/nl/en/about.properties
	 *   ...
	 *   plugin root/about.properties
	 *   fragment1 root/about.properties
	 *   fragment2 root/about.properties
	 *   ...
	 * 
	 * If a locale other than the default locale is desired, use an
	 * override map.
	 * 
	 * @param path file path relative to plug-in installation location
	 * @param override map of override substitution arguments to be used for
	 * any $arg$ path elements. The map keys correspond to the substitution
	 * arguments (eg. "$nl$" or "$os$"). The resulting
	 * values must be of type java.lang.String. If the map is <code>null</code>,
	 * or does not contain the required substitution argument, the default
	 * is used.
	 * @return a URL for the given path or <code>null</code>.  It is not
	 * necessary to perform a 'resolve' on this URL.
	 * 
	 * @since 2.0
	 * @deprecated 
	 * Use 
	 * <pre>
	 *     Platform.find(bundle, path, override) 
	 * </pre>
	 * where <code>bundle</code> is the bundle associated with 
	 * the relevant plug-in.
	 */
	public URL find(IPath path, Map override);
}
