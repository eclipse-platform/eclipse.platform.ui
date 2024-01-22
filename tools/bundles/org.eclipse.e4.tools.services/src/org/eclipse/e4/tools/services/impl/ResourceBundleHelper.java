/*******************************************************************************
 * Copyright (c) 2012, 2017 Dirk Fauth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.io.IOException;
import java.io.InputStream;
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

import org.osgi.framework.Bundle;

/**
 * Helper class for retrieving {@link ResourceBundle}s out of OSGi {@link Bundle}s.
 *
 * @author Dirk Fauth
 */
public class ResourceBundleHelper {

	/**
	 * This method searches for the {@link ResourceBundle} in a modified way by inspecting the configuration option
	 * <code>equinox.root.locale</code>.
	 * <p>
	 * If the value for this system property is set to an empty String the default search order for ResourceBundles is
	 * used:
	 * </p>
	 * <ul>
	 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
	 * <li>bn + Ls + "_" + Cs</li>
	 * <li>bn + Ls</li>
	 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
	 * <li>bn + Ld + "_" + Cd</li>
	 * <li>bn + Ld</li>
	 * <li>bn</li>
	 * </ul>
	 * <p>
	 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale (language, country,
	 * variant) and Ld, Cd and Vd are the default locale (language, country, variant).
	 * </p>
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * </p>
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
	 * </p>
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
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * </p>
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
	 * </p>
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
	 * <p>
	 * If Ls equals the value of <code>equinox.root.locale</code> then the following search order is used:
	 * </p>
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
				PrivilegedExceptionAction<InputStream> action = (PrivilegedExceptionAction<InputStream>) () -> {
					URL url = osgiBundle.getEntry(resourceName);
					if (url != null) {
						URLConnection connection = url.openConnection();
						if (connection != null) {
							// Disable caches to get fresh data for
							// reloading.
							connection.setUseCaches(false);
							return connection.getInputStream();
						}
					}
					return null;
				};
				try (InputStream stream = AccessController.doPrivileged(action)) {
					if (stream != null) {
						bundle = new PropertyResourceBundle(stream);
					}
				} catch (final PrivilegedActionException e) {
					throw (IOException) e.getException();
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
