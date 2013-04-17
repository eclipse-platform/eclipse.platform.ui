/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.osgi.service.localization.BundleLocalization;

/**
 * The lookup of the translation the same than the one in {@link BundleLocalization} which is based
 * upon the value found in equinox.root.locale which defaults to "en":
 * <ul>
 * <li>If set to empty string then the search order for:
 * <ul>
 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
 * <li>bn + Ls + "_" + Cs</li>
 * <li>bn + Ls</li>
 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
 * <li>bn + Ld + "_" + Cd</li>
 * <li>bn + Ld</li>
 * </ul>
 * </li>
 * <li>If Ls equals the value of equinox.root.locale then the following search order is used:
 * <ul>
 * <li>bn + Ls + "_" + Cs + "_" + Vs</li>
 * <li>bn + Ls + "_" + Cs</li>
 * <li>bn + Ls</li>
 * <li>bn + Ld + "_" + Cd + "_" + Vd</li>
 * <li>bn + Ld + "_" + Cd</li>
 * <li>bn + Ld</li>
 * </ul>
 * </li>
 * </ul>
 * Where bn is this bundle's localization basename, Ls, Cs and Vs are the specified locale
 * (language, country, variant) and Ld, Cd and Vd are the default locale (language, country,
 * variant).
 */
public abstract class AbstractTranslationProvider {
	final static String DEFAULT_ROOT = getEquinoxRootLocale();

	private final Hashtable<String, BundleResourceBundle> cache = new Hashtable<String, BundleResourceBundle>(
			5);

	private static String getEquinoxRootLocale() {
		// Logic from FrameworkProperties.getProperty("equinox.root.locale", "en")
		String root = System.getProperties().getProperty("equinox.root.locale");
		if (root == null) {
			root = "en";
		}
		return root;
	}
	
	protected abstract InputStream getResourceAsStream(String name);
	protected abstract String getBasename();

	public String translate(String locale, String key) {
		String defaultLocale = Locale.getDefault().toString();
		String localeString = locale;
		ResourceBundle bundle = getResourceBundle(locale.toString(),
				defaultLocale.equals(localeString));
		try {
			if( bundle == null ) {
				return key;
			}
			return bundle.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
			return key;
		}

	}

	private ResourceBundle getResourceBundle(String localeString, boolean isDefaultLocale) {
		BundleResourceBundle resourceBundle = lookupResourceBundle(localeString);
		if (isDefaultLocale)
			return (ResourceBundle) resourceBundle;
		// need to determine if this is resource bundle is an empty stem
		// if it is then the default locale should be used
		if (resourceBundle == null || resourceBundle.isStemEmpty())
			return (ResourceBundle) lookupResourceBundle(Locale.getDefault().toString());
		return (ResourceBundle) resourceBundle;
	}

	private interface BundleResourceBundle {
		void setParent(ResourceBundle parent);

		boolean isEmpty();

		boolean isStemEmpty();
	}

	private BundleResourceBundle lookupResourceBundle(String localeString) {
		// get the localization header as late as possible to avoid accessing the raw headers
		// getting the first value from the raw headers forces the manifest to be parsed (bug
		// 332039)
		String localizationHeader = getBasename();
		synchronized (cache) {
			BundleResourceBundle result = cache.get(localeString);
			if (result != null)
				return result.isEmpty() ? null : result;
			String[] nlVarients = buildNLVariants(localeString);
			BundleResourceBundle parent = null;

			for (int i = nlVarients.length - 1; i >= 0; i--) {
				BundleResourceBundle varientBundle = null;
				InputStream resourceStream = getResourceAsStream(localizationHeader
						+ (nlVarients[i].equals("") ? nlVarients[i] : '_' + nlVarients[i])
						+ ".properties");

				if (resourceStream == null) {
					varientBundle = cache.get(nlVarients[i]);
				} else {
					try {
						varientBundle = new LocalizationResourceBundle(resourceStream);
					} catch (IOException e) {
						// ignore and continue
					} finally {
						if (resourceStream != null) {
							try {
								resourceStream.close();
							} catch (IOException e3) {
								// Ignore exception
							}
						}
					}
				}

				if (varientBundle == null) {
					varientBundle = new EmptyResouceBundle(nlVarients[i]);
				}
				if (parent != null)
					varientBundle.setParent((ResourceBundle) parent);
				cache.put(nlVarients[i], varientBundle);
				parent = varientBundle;
			}
			result = cache.get(localeString);
			return result.isEmpty() ? null : result;
		}
	}

	private String[] buildNLVariants(String nl) {
		List<String> result = new ArrayList<String>();
		while (nl.length() > 0) {
			result.add(nl);
			int i = nl.lastIndexOf('_');
			nl = (i < 0) ? "" : nl.substring(0, i); //$NON-NLS-1$
		}
		result.add(""); //$NON-NLS-1$
		return result.toArray(new String[result.size()]);
	}

	private class LocalizationResourceBundle extends PropertyResourceBundle implements
			BundleResourceBundle {
		public LocalizationResourceBundle(InputStream in) throws IOException {
			super(in);
		}

		public void setParent(ResourceBundle parent) {
			super.setParent(parent);
		}

		public boolean isEmpty() {
			return false;
		}

		public boolean isStemEmpty() {
			return parent == null;
		}
	}

	class EmptyResouceBundle extends ResourceBundle implements BundleResourceBundle {
		private final String localeString;

		public EmptyResouceBundle(String locale) {
			super();
			this.localeString = locale;
		}

		public Enumeration<String> getKeys() {
			return null;
		}

		protected Object handleGetObject(String arg0) throws MissingResourceException {
			return null;
		}

		public void setParent(ResourceBundle parent) {
			super.setParent(parent);
		}

		public boolean isEmpty() {
			if (parent == null)
				return true;
			return ((BundleResourceBundle) parent).isEmpty();
		}

		public boolean isStemEmpty() {
			if (DEFAULT_ROOT.equals(localeString))
				return false;
			if (parent == null)
				return true;
			return ((BundleResourceBundle) parent).isStemEmpty();
		}
	}
	
	protected void clearCache() {
		cache.clear();
	}
}
