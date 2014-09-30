/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.eclipse.e4.tools.services.ToolsServicesActivator;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Helper class for retrieving {@link ResourceBundle}s out of OSGi {@link Bundle}s.
 *
 * @author Dirk Fauth
 */
// There is no replacement for PackageAdmin#getBundles()
@SuppressWarnings("deprecation")
public class ResourceBundleHelper {

	/**
	 * The schema identifier used for Eclipse platform references
	 */
	private static final String PLATFORM_SCHEMA = "platform"; //$NON-NLS-1$
	/**
	 * The schema identifier used for Eclipse bundle class references
	 */
	private static final String BUNDLECLASS_SCHEMA = "bundleclass"; //$NON-NLS-1$
	/**
	 * Identifier part of the Eclipse platform schema to point to a plugin
	 */
	private static final String PLUGIN_SEGMENT = "/plugin/"; //$NON-NLS-1$
	/**
	 * Identifier part of the Eclipse platform schema to point to a fragment
	 */
	private static final String FRAGMENT_SEGMENT = "/fragment/"; //$NON-NLS-1$
	/**
	 * The separator character for paths in the platform schema
	 */
	private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

	/**
	 * Parses the specified contributor URI and loads the {@link ResourceBundle} for the specified {@link Locale} out of
	 * an OSGi {@link Bundle}.
	 * <p>
	 * Following URIs are supported:
	 * <ul>
	 * <li>platform:/[plugin|fragment]/[Bundle-SymbolicName]<br>
	 * Load the OSGi resource bundle out of the bundle/fragment named [Bundle-SymbolicName]</li>
	 * <li>platform:/[plugin|fragment]/[Bundle-SymbolicName]/[Path]/[Basename]<br>
	 * Load the resource bundle specified by [Path] and [Basename] out of the bundle/fragment named
	 * [Bundle-SymbolicName].</li>
	 * <li>bundleclass://[plugin|fragment]/[Full-Qualified-Classname]<br>
	 * Instantiate the class specified by [Full-Qualified-Classname] out of the bundle/fragment named
	 * [Bundle-SymbolicName]. Note that the class needs to be a subtype of {@link ResourceBundle}.</li>
	 * </ul>
	 * </p>
	 *
	 * @param contributorURI The URI that points to a {@link ResourceBundle}
	 * @param locale The {@link Locale} to use for loading the {@link ResourceBundle}
	 * @param localization The service for retrieving a {@link ResourceBundle} for a given {@link Locale} out of
	 *            the given {@link Bundle} which is specified by URI.
	 * @return the resource bundle
	 */
	public static ResourceBundle getResourceBundleForUri(String contributorURI, Locale locale,
		BundleLocalization localization) {
		if (contributorURI == null) {
			return null;
		}

		final LogService logService = ToolsServicesActivator.getDefault().getLogService();

		URI uri;
		try {
			uri = new URI(contributorURI);
		} catch (final URISyntaxException e) {
			if (logService != null)
			{
				logService.log(LogService.LOG_ERROR, "Invalid contributor URI: " + contributorURI); //$NON-NLS-1$
			}
			return null;
		}

		String bundleName = null;
		Bundle bundle = null;
		String resourcePath = null;
		String classPath = null;

		// the uri follows the platform schema, so we search for .properties files in the bundle
		if (PLATFORM_SCHEMA.equals(uri.getScheme())) {
			bundleName = uri.getPath();
			if (bundleName.startsWith(PLUGIN_SEGMENT)) {
				bundleName = bundleName.substring(PLUGIN_SEGMENT.length());
			} else if (bundleName.startsWith(FRAGMENT_SEGMENT)) {
				bundleName = bundleName.substring(FRAGMENT_SEGMENT.length());
			}

			resourcePath = ""; //$NON-NLS-1$
			if (bundleName.contains(PATH_SEPARATOR)) {
				resourcePath = bundleName.substring(bundleName.indexOf(PATH_SEPARATOR) + 1);
				bundleName = bundleName.substring(0, bundleName.indexOf(PATH_SEPARATOR));
			}
		} else if (BUNDLECLASS_SCHEMA.equals(uri.getScheme())) {
			if (uri.getAuthority() == null) {
				if (logService != null)
				{
					logService.log(LogService.LOG_ERROR, "Failed to get bundle for: " + contributorURI); //$NON-NLS-1$
				}
			}
			bundleName = uri.getAuthority();
			// remove the leading /
			classPath = uri.getPath().substring(1);
		}

		ResourceBundle result = null;

		if (bundleName != null) {
			bundle = getBundleForName(bundleName);

			if (bundle != null) {
				if (resourcePath == null && classPath != null) {
					// the URI points to a class within the bundle classpath
					// therefore we are trying to instantiate the class
					try {
						final Class<?> resourceBundleClass = bundle.loadClass(classPath);
						result = getEquinoxResourceBundle(classPath, locale, resourceBundleClass.getClassLoader());
					} catch (final Exception e) {
						if (logService != null)
						{
							logService.log(LogService.LOG_ERROR,
								"Failed to load specified ResourceBundle: " + contributorURI, e); //$NON-NLS-1$
						}
					}
				}
				else if (resourcePath.length() > 0) {
					// the specified URI points to a resource
					// therefore we try to load the .properties files into a ResourceBundle
					result = getEquinoxResourceBundle(resourcePath.replace('.', '/'), locale, bundle);
				}
				else {
					// there is no class and no special resource specified within the URI
					// therefore we load the OSGi resource bundle out of the specified Bundle
					// for the current Locale
					result = localization.getLocalization(bundle, locale.toString());
				}
			}
		}

		return result;
	}

	/**
	 * This method searches for the {@link ResourceBundle} in a modified way by inspecting the configuration option
	 * <code>equinox.root.locale</code>.
	 * <p>
	 * If the value for this system property is set to an empty String the default search order for ResourceBundles is
	 * used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale (language, country,
	 * variant) and Ld, Cd and Vd are the default locale (language, country, variant).
	 * </p>
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * </p>
	 * If <code>equinox.root.locale=en</code> and en_XX or en is asked for then this allows the root file to be used
	 * instead of
	 * falling back to the default locale.
	 *
	 * @param baseName the base name of the resource bundle, a fully qualified class name
	 * @param locale the locale for which a resource bundle is desired
	 * @param loader the class loader from which to load the resource bundle
	 * @return a resource bundle for the given base name and locale
	 *
	 * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
	 */
	public static ResourceBundle getEquinoxResourceBundle(String baseName, Locale locale, ClassLoader loader) {
		ResourceBundle resourceBundle = null;

		final String equinoxLocale = getEquinoxRootLocale();
		// if the equinox.root.locale is not empty and the specified locale equals the equinox.root.locale
		// -> use the special search order
		if (equinoxLocale.length() > 0 && locale.toString().startsWith(equinoxLocale)) {
			// there is a equinox.root.locale configured that matches the specified locale
			// so the special search order is used
			// to achieve this we first search without a fallback to the default locale
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale, loader,
					ResourceBundle.Control.getNoFallbackControl(Control.FORMAT_DEFAULT));
			} catch (final MissingResourceException e) {
				// do nothing
			}
			// if there is no ResourceBundle found for that path, we will now search for the default locale
			// ResourceBundle
			if (resourceBundle == null) {
				try {
					resourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), loader,
						ResourceBundle.Control.getNoFallbackControl(Control.FORMAT_DEFAULT));
				} catch (final MissingResourceException e) {
					// do nothing
				}
			}
		}
		else {
			// there is either no equinox.root.locale configured or it does not match the specified locale
			// -> use the default search order
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale, loader);
			} catch (final MissingResourceException e) {
				// do nothing
			}
		}

		return resourceBundle;
	}

	/**
	 * This method searches for the {@link ResourceBundle} in a modified way by inspecting the configuration option
	 * <code>equinox.root.locale</code>. It uses the {@link BundleResourceBundleControl} to load the resources out
	 * of a {@link Bundle}.
	 * <p>
	 * <b>Note: This method will only search for ResourceBundles based on properties files.</b>
	 * </p>
	 * <p>
	 * If the value for this system property is set to an empty String the default search order for ResourceBundles is
	 * used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale (language, country,
	 * variant) and Ld, Cd and Vd are the default locale (language, country, variant).
	 * </p>
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * </p>
	 * If <code>equinox.root.locale=en</code> and en_XX or en is asked for then this allows the root file to be used
	 * instead of
	 * falling back to the default locale.
	 *
	 * @param baseName the base name of the resource bundle, a fully qualified class name
	 * @param locale the locale for which a resource bundle is desired
	 * @param bundle The OSGi {@link Bundle} to lookup the {@link ResourceBundle}
	 * @return a resource bundle for the given base name and locale
	 *
	 * @see ResourceBundle#getBundle(String, Locale, Control)
	 */
	public static ResourceBundle getEquinoxResourceBundle(String baseName, Locale locale, Bundle bundle) {
		return getEquinoxResourceBundle(baseName, locale,
			new BundleResourceBundleControl(bundle, true), new BundleResourceBundleControl(bundle, false));
	}

	/**
	 * This method searches for the {@link ResourceBundle} in a modified way by inspecting the configuration option
	 * <code>equinox.root.locale</code>.
	 * <p>
	 * <b>Note: This method will only search for ResourceBundles based on properties files.</b>
	 * </p>
	 * <p>
	 * If the value for this system property is set to an empty String the default search order for ResourceBundles is
	 * used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale (language, country,
	 * variant) and Ld, Cd and Vd are the default locale (language, country, variant).
	 * </p>
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * </p>
	 * If <code>equinox.root.locale=en</code> and en_XX or en is asked for then this allows the root file to be used
	 * instead of
	 * falling back to the default locale.
	 *
	 * @param baseName the base name of the resource bundle, a fully qualified class name
	 * @param locale the locale for which a resource bundle is desired
	 * @param withFallback The {@link Control} that uses the default locale fallback on searching for resource bundles.
	 * @param withoutFallback The {@link Control} that doesn't use the default locale fallback on searching for resource
	 *            bundles.
	 * @return a resource bundle for the given base name and locale
	 *
	 * @see ResourceBundle#getBundle(String, Locale, Control)
	 */
	public static ResourceBundle getEquinoxResourceBundle(String baseName, Locale locale, Control withFallback,
		Control withoutFallback) {
		ResourceBundle resourceBundle = null;

		final String equinoxLocale = getEquinoxRootLocale();
		// if the equinox.root.locale is not empty and the specified locale equals the equinox.root.locale
		// -> use the special search order
		if (equinoxLocale.length() > 0 && locale.toString().startsWith(equinoxLocale)) {
			// there is a equinox.root.locale configured that matches the specified locale
			// so the special search order is used
			// to achieve this we first search without a fallback to the default locale
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale, withoutFallback);
			} catch (final MissingResourceException e) {
				// do nothing
			}
			// if there is no ResourceBundle found for that path, we will now search for the default locale
			// ResourceBundle
			if (resourceBundle == null) {
				try {
					resourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), withoutFallback);
				} catch (final MissingResourceException e) {
					// do nothing
				}
			}
		}
		else {
			// there is either no equinox.root.locale configured or it does not match the specified locale
			// -> use the default search order
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale, withFallback);
			} catch (final MissingResourceException e) {
				// do nothing
			}
		}

		return resourceBundle;
	}

	/**
	 * @return The value for the system property for key <code>equinox.root.locale</code>.
	 *         If none is specified than <b>en</b> will be returned as default.
	 */
	private static String getEquinoxRootLocale() {
		// Logic from FrameworkProperties.getProperty("equinox.root.locale", "en")
		String root = System.getProperties().getProperty("equinox.root.locale"); //$NON-NLS-1$
		if (root == null) {
			root = "en"; //$NON-NLS-1$
		}
		return root;
	}

	/**
	 * This method is copied out of org.eclipse.e4.ui.internal.workbench.Activator
	 * because as it is a internal resource, it is not accessible for us.
	 *
	 * @param bundleName
	 *            the bundle id
	 * @return A bundle if found, or <code>null</code>
	 */
	public static Bundle getBundleForName(String bundleName) {
		final PackageAdmin packageAdmin = ToolsServicesActivator.getDefault().getPackageAdmin();
		final Bundle[] bundles = packageAdmin.getBundles(bundleName, null);
		if (bundles == null) {
			return null;
		}
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Converts a String to a Locale.
	 * </p>
	 *
	 * <p>
	 * This method takes the string format of a locale and creates the locale object from it.
	 * </p>
	 *
	 * <pre>
	 *   MessageFactoryServiceImpl.toLocale("en")         = new Locale("en", "")
	 *   MessageFactoryServiceImpl.toLocale("en_GB")      = new Locale("en", "GB")
	 *   MessageFactoryServiceImpl.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")
	 * </pre>
	 *
	 * <p>
	 * This method validates the input strictly. The language code must be lowercase. The country code must be
	 * uppercase. The separator must be an underscore. The length must be correct.
	 * </p>
	 *
	 * <p>
	 * This method is inspired by <code>org.apache.commons.lang.LocaleUtils.toLocale(String)</code> by fixing the
	 * parsing error for uncommon Locales like having a language and a variant code but no country code, or a Locale
	 * that only consists of a country code.
	 * </p>
	 *
	 * @param str the locale String to convert
	 * @return a Locale that matches the specified locale String or <code>null</code> if the specified String is
	 *         <code>null</code>
	 * @throws IllegalArgumentException if the String is an invalid format
	 */
	public static Locale toLocale(String str) {
		if (str == null) {
			return null;
		}

		String language = ""; //$NON-NLS-1$
		String country = ""; //$NON-NLS-1$
		String variant = ""; //$NON-NLS-1$

		final String[] localeParts = str.split("_"); //$NON-NLS-1$
		if (localeParts.length == 0 || localeParts.length > 3
			|| localeParts.length == 1 && localeParts[0].length() == 0) {
			throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
		}
		if (localeParts[0].length() == 1 || localeParts[0].length() > 2) {
			throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
		}
		else if (localeParts[0].length() == 2) {
			final char ch0 = localeParts[0].charAt(0);
			final char ch1 = localeParts[0].charAt(1);
			if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
				throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
			}
		}

		language = localeParts[0];

		if (localeParts.length > 1) {
			if (localeParts[1].length() == 1 || localeParts[1].length() > 2) {
				throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
			}
			else if (localeParts[1].length() == 2) {
				final char ch3 = localeParts[1].charAt(0);
				final char ch4 = localeParts[1].charAt(1);
				if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
					throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
				}
			}

			country = localeParts[1];
		}

		if (localeParts.length == 3) {
			if (localeParts[0].length() == 0 && localeParts[1].length() == 0) {
				throw new IllegalArgumentException("Invalid locale format: " + str); //$NON-NLS-1$
			}
			variant = localeParts[2];
		}

		return new Locale(language, country, variant);
	}

	/**
	 * Specialization of {@link Control} which loads the {@link ResourceBundle} out of an
	 * OSGi {@link Bundle} instead of using a classloader.
	 *
	 * <p>
	 * It only supports properties based {@link ResourceBundle}s. If you want to use source based {@link ResourceBundle}
	 * s you have to use the bundleclass URI with the Message annotation.
	 *
	 * @author Dirk Fauth
	 *
	 */
	static class BundleResourceBundleControl extends ResourceBundle.Control {

		/**
		 * Flag to determine whether the default locale should be used as fallback locale
		 * in case there is no {@link ResourceBundle} found for the specified locale.
		 */
		private final boolean useFallback;

		/**
		 * The OSGi {@link Bundle} to lookup the {@link ResourceBundle}
		 */
		private final Bundle osgiBundle;

		/**
		 *
		 * @param osgiBundle The OSGi {@link Bundle} to lookup the {@link ResourceBundle}
		 * @param useFallback <code>true</code> if the default locale should be used as fallback
		 *            locale in the search path or <code>false</code> if there should be no fallback.
		 */
		public BundleResourceBundleControl(Bundle osgiBundle, boolean useFallback) {
			this.osgiBundle = osgiBundle;
			this.useFallback = useFallback;
		}

		@Override
		public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {

			final String bundleName = toBundleName(baseName, locale);
			ResourceBundle bundle = null;
			if (format.equals("java.properties")) { //$NON-NLS-1$
				final String resourceName = toResourceName(bundleName, "properties"); //$NON-NLS-1$
				InputStream stream = null;
				try {
					stream = AccessController.doPrivileged(
						new PrivilegedExceptionAction<InputStream>() {
							@Override
							public InputStream run() throws IOException {
								InputStream is = null;
								final URL url = osgiBundle.getEntry(resourceName);
								if (url != null) {
									final URLConnection connection = url.openConnection();
									if (connection != null) {
										// Disable caches to get fresh data for
										// reloading.
										connection.setUseCaches(false);
										is = connection.getInputStream();
									}
								}
								return is;
							}
						});
				} catch (final PrivilegedActionException e) {
					throw (IOException) e.getException();
				}
				if (stream != null) {
					try {
						bundle = new PropertyResourceBundle(stream);
					} finally {
						stream.close();
					}
				}
			}
			else {
				throw new IllegalArgumentException("unknown format: " + format); //$NON-NLS-1$
			}
			return bundle;
		}

		@Override
		public List<String> getFormats(String baseName) {
			return FORMAT_PROPERTIES;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return useFallback ? super.getFallbackLocale(baseName, locale) : null;
		}
	}
}
