/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.HelpData;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;

/*
 * Reads and processes product preferences by considering not only the active
 * product, but all installed products.
 * 
 * For example, help orders the books in the table of contents in such a way that
 * satisfies the currently running product's preferred order, and as many other product's
 * preferred orderings.
 */
public class ProductPreferences {

	private static final String TRUE = String.valueOf(true);
	private static Properties[] productPreferences;
	private static SequenceResolver orderResolver;
	private static Map preferencesToPluginIdMap;
	private static Map preferencesToProductIdMap;
	private static List primaryTocOrdering;
	private static List[] secondaryTocOrderings;
	
	/*
	 * Returns the recommended order to display the given toc entries in. Each
	 * toc entry is a String, either the id of the toc contribution or the
	 * id of the category of tocs.
	 */
	public static List getTocOrder(List itemsToOrder) {
		return getOrderedList(itemsToOrder, getPrimaryTocOrdering(), getSecondaryTocOrderings());
	}
	
	/*
	 * Returns the primary toc ordering. This is the preferred order for the active
	 * product (either specified via help data xml file or deprecated comma-separated
	 * list in plugin_customization.ini). Help data takes precedence.
	 */
	public static List getPrimaryTocOrdering() {
		if (primaryTocOrdering == null) {
			IProduct product = Platform.getProduct();
			if (product != null) {
				String pluginId = product.getDefiningBundle().getSymbolicName();
				Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
				String helpDataFile = prefs.getString(HelpPlugin.HELP_DATA_KEY);
				String baseTOCS = prefs.getString(HelpPlugin.BASE_TOCS_KEY);
				primaryTocOrdering = getTocOrdering(pluginId, helpDataFile, baseTOCS);
			}
			// active product has no preference for toc order
			if (primaryTocOrdering == null) {
				primaryTocOrdering = new ArrayList();
			}
		}
		return primaryTocOrdering;
	}
	
	/*
	 * Returns all secondary toc ordering. These are the preferred toc orders of all
	 * defined products except the active product.
	 */
	public static List[] getSecondaryTocOrderings() {
		if (secondaryTocOrderings == null) {
			List list = new ArrayList();
			Properties[] productPreferences = getProductPreferences(false);
			for (int i=0;i<productPreferences.length;++i) {
				String pluginId = (String)preferencesToPluginIdMap.get(productPreferences[i]);
				String helpDataFile = (String)productPreferences[i].get(HelpPlugin.PLUGIN_ID + '/' + HelpPlugin.HELP_DATA_KEY);
				String baseTOCS = (String)productPreferences[i].get(HelpPlugin.PLUGIN_ID + '/' + HelpPlugin.BASE_TOCS_KEY);
				List ordering = getTocOrdering(pluginId, helpDataFile, baseTOCS);
				if (ordering != null) {
					list.add(ordering);
				}
			}
			secondaryTocOrderings = (List[])list.toArray(new List[list.size()]);
		}
		return secondaryTocOrderings;
	}

	/*
	 * Returns the preferred toc ordering of the product defined by the given
	 * plug-in that has the given helpDataFile and baseTOCS specified (these last
	 * two may be null if not specified).
	 */
	public static List getTocOrdering(String pluginId, String helpDataFile, String baseTOCS) {
		if (helpDataFile != null && helpDataFile.length() > 0) {
			Bundle bundle = Platform.getBundle(pluginId);
			URL helpDataUrl = bundle.getEntry(helpDataFile);
			HelpData helpData = new HelpData(helpDataUrl);
			return helpData.getTocOrder();
		}
		else {
			if (baseTOCS != null) {
				return tokenize(baseTOCS);
			}
		}
		return null;
	}
	
	/*
	 * Returns the boolean preference for the given key by consulting every
	 * product's preferences. If any of the products want the preference to
	 * be true (or use the default and the default is true), returns true.
	 * Otherwise returns false (if no products want it true).
	 */
	public static boolean getBoolean(Plugin plugin, String key) {
		Properties[] properties = getProductPreferences(true);
		String defaultValue = plugin.getPluginPreferences().getDefaultString(key);
		String currentValue = plugin.getPluginPreferences().getString(key);
		String pluginId = plugin.getBundle().getSymbolicName();
		if (currentValue != null && currentValue.equalsIgnoreCase(TRUE)) {
			return true;
		}
		for (int i=0;i<properties.length;++i) {
			String value = (String)properties[i].get(pluginId + '/' + key);
			if (value == null) {
				value = defaultValue;
			}
			if (value != null && value.equalsIgnoreCase(TRUE)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Returns the given items in an order that satistfies the most products'
	 * preferences. Uses the given key for the preference stored in the given
	 * plugin to order the items. The active product is given precedence, then
	 * other products are considered.
	 */
	public static List getOrderedList(Plugin plugin, String key, List items) {
		List primary = tokenize(plugin.getPluginPreferences().getString(key));
		Properties[] productPreferences = getProductPreferences(false);
		List secondaryLists = new ArrayList();
		for (int i=0;i<productPreferences.length;++i) {
			String value = productPreferences[i].getProperty(plugin.getBundle().getSymbolicName() + '/' + key);
			if (value != null) {
				secondaryLists.add(tokenize(value));
			}
		}
		List[] secondary = (List[])secondaryLists.toArray(new List[secondaryLists.size()]);
		return getOrderedList(items, primary, secondary);
	}

	/*
	 * Returns the given items in the order specified. Items listed in the order
	 * but not present are skipped, and items present but not ordered are added
	 * at the end.
	 */
	public static List getOrderedList(List items, List order) {
		return getOrderedList(items, order, null);
	}

	/*
	 * Returns the given items in an order that best satisfies the given orderings.
	 * The primary ordering must be satisfied in all cases. As many secondary orderings
	 * as reasonably possible will be satisfied.
	 */
	public static List getOrderedList(List items, List primary, List[] secondary) {
		List result = new ArrayList();
		LinkedHashSet set = new LinkedHashSet(items);
		if (orderResolver == null) {
			orderResolver = new SequenceResolver();
		}
		List order = orderResolver.getSequence(primary, secondary);
		Iterator iter = order.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (set.contains(obj)) {
				result.add(obj);
				set.remove(obj);
			}
		}
		result.addAll(set);
		return result;
	}

	public static synchronized String getPluginId(Properties prefs) {
		return (String)preferencesToPluginIdMap.get(prefs);
	}

	public static synchronized String getProductId(Properties prefs) {
		return (String)preferencesToProductIdMap.get(prefs);
	}

	/*
	 * Returns the preferences for all products in the runtime environment (even if
	 * they are not active).
	 */
	public static synchronized Properties[] getProductPreferences(boolean includeActiveProduct) {
		if (productPreferences == null) {
			String activeProductId = null;
			IProduct activeProduct = Platform.getProduct();
			if (activeProduct != null) {
				activeProductId = activeProduct.getId();
			}
			Collection collection = new ArrayList();
			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.core.runtime.products"); //$NON-NLS-1$
			for (int i=0;i<elements.length;++i) {
				if (elements[i].getName().equals("product")) { //$NON-NLS-1$
					String productId = elements[i].getDeclaringExtension().getUniqueIdentifier();
					if (includeActiveProduct || activeProductId == null || !activeProductId.equals(productId)) {
						String contributor = elements[i].getContributor().getName();
						IConfigurationElement[] propertyElements = elements[i].getChildren("property"); //$NON-NLS-1$
						for (int j=0;j<propertyElements.length;++j) {
							String name = propertyElements[j].getAttribute("name"); //$NON-NLS-1$
							if (name != null && name.equals("preferenceCustomization")) { //$NON-NLS-1$
								String value = propertyElements[j].getAttribute("value"); //$NON-NLS-1$
								if (value != null) {
									Properties properties = loadPropertiesFile(contributor, value);
									if (properties != null) {
										collection.add(properties);
									}
									if (preferencesToPluginIdMap == null) {
										preferencesToPluginIdMap = new HashMap();
									}
									preferencesToPluginIdMap.put(properties, contributor);
									if (preferencesToProductIdMap == null) {
										preferencesToProductIdMap = new HashMap();
									}
									preferencesToProductIdMap.put(properties, productId);
								}
							}
						}
					}
				}
			}
			productPreferences = (Properties[])collection.toArray(new Properties[collection.size()]);
		}
		return productPreferences;
	}
	
	/*
	 * Returns all the unique values for the given key in all the properties
	 * provided. For example, if two of them have "true" and one has "false", it
	 * will return "true" and "false".
	 */
	public static Set getUniqueValues(Plugin plugin, String key, Properties[] properties) {
		Set set = new HashSet();
		String defaultValue = plugin.getPluginPreferences().getDefaultString(key);
		String currentValue = plugin.getPluginPreferences().getString(key);
		String pluginId = plugin.getBundle().getSymbolicName();
		for (int i=0;i<properties.length;++i) {
			String value = (String)properties[i].get(pluginId + '/' + key);
			set.add(value != null ? value : defaultValue);
		}
		set.add(currentValue != null ? currentValue : defaultValue);
		return set;
	}
	
	/*
	 * Returns the value for the given key by consulting the given properties, but giving
	 * precedence to the primary properties. If the primary properties has the key, it is
	 * returned. Otherwise, it will return the value of the first secondary properties that
	 * has the key, or null if none of them has it.
	 */
	public static String getValue(String key, Properties primary, Properties[] secondary) {
		String value = null;
		if (primary != null) {
			value = primary.getProperty(key);
		}
		if (value == null) {
			for (int i=0;i<secondary.length;++i) {
				if (secondary[i] != primary) {
					value = secondary[i].getProperty(key);
					if (value != null) {
						break;
					}
				}
			}
		}
		return value;
	}

	/*
	 * Loads and returns the properties in the given properties file. The path is
	 * relative to the bundle with the given id.
	 */
	public static Properties loadPropertiesFile(String bundleId, String path) {
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle != null) {
			URL url = bundle.getEntry(path);
			if (url != null) {
				InputStream in = null;
				try {
					in = url.openStream();
					Properties properties = new Properties();
					properties.load(in);
					return properties;
				}
				catch (IOException e) {
					// log the fact that it couldn't load it
					HelpPlugin.logError("Error opening product's plugin customization file: " + bundleId + "/" + path, e); //$NON-NLS-1$ //$NON-NLS-2$
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (IOException e) {
							// nothing we can do here
						}
					}
				}
			}
		}
		return null;
	}
	
	/*
	 * Tokenizes the given list of items, allowing them to be separated by whitespace, commas,
	 * and/or semicolons.
	 * 
	 * e.g. "item1, item2, item3"
	 * would return a list of strings containing "item1", "item2", and "item3".
	 */
	public static List tokenize(String str) {
		if (str != null) {
			StringTokenizer tok = new StringTokenizer(str, " \n\r\t;,"); //$NON-NLS-1$
			List list = new ArrayList();
			while (tok.hasMoreElements()) {
				list.add(tok.nextToken());
			}
			return list;
		}
		return new ArrayList();
	}
}
