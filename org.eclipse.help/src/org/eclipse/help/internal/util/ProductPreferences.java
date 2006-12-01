/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;

/**
 * Reads and processes product preferences by considering not only the active
 * product, but all installed products.
 * 
 * For example, you may want to order the table of contents in such a way that
 * satisfies the most products' preferred order, as opposed to just the active
 * product.
 */
public class ProductPreferences {

	private static final String TRUE = String.valueOf(true);
	private static Properties[] productPreferences;
	
	/**
	 * Returns the number of elements contained in both the given
	 * set and collection.
	 * 
	 * @param set the set
	 * @param collection the collection
	 * @return the number of common items
	 */
	public static int countCommonItems(Set set, Collection collection) {
		if (set != null && collection != null) {
			Set setCopy = new HashSet(set);
			setCopy.retainAll(collection);
			return setCopy.size();
		}
		return 0;
	}
	
	/**
	 * Finds the list that contains the most items from the set. If there is a tie,
	 * the first with the highest number of matches is chosen. If no lists are
	 * provided, returns null. If none of the lists have any matching items, returns
	 * null.
	 * 
	 * @param items the items to look for
	 * @param lists the lists to search through
	 * @return the list containing the most items from the set, or null
	 */
	public static List findBestMatch(Set items, List lists) {
		if (!lists.isEmpty()) {
			int bestMatchSoFar = 0;
			int bestCount = 0;
			Iterator iter = lists.iterator();
			int i = 0;
			while (iter.hasNext()) {
				List list = (List)iter.next();
				int count = countCommonItems(items, list);
				if (count > bestCount) {
					bestCount = count;
					bestMatchSoFar = i;
				}
				++i;
			}
			if (bestCount > 0) {
				return (List)lists.get(bestMatchSoFar);
			}
		}
		return null;
	}
	
	/**
	 * Returns the boolean preference for the given key by consulting every
	 * product's preferences. If any of the products want the preference to
	 * be true (or use the default and the default is true), returns true.
	 * Otherwise returns false (if no products want it true).
	 * 
	 * @param plugin the plugin that owns the preference
	 * @param key the preference key
	 * @return the boolean value, considering all products
	 */
	public static boolean getBoolean(Plugin plugin, String key) {
		Properties[] properties = getProductPreferences();
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
	
	/**
	 * Returns the given items in an order that satistfies the most products'
	 * preferences. Uses the given key for the preference stored in the given
	 * plugin to order the items. The active product is given precedence, then
	 * other products are considered.
	 * 
	 * @param plugin the plugin that contains the preference
	 * @param key the preference key (short form)
	 * @param items the items to order
	 * @return the ordered list of items
	 */
	public static List getOrderedList(Plugin plugin, String key, List items) {
		List primary = tokenize(plugin.getPluginPreferences().getString(key));
		Properties[] productPreferences = getProductPreferences();
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

	public static List getOrderedList(List items, List order) {
		return getOrderedList(items, order, null);
	}

	/**
	 * Returns the given items in an order that best satisfies the given orderings.
	 * The primary ordering is consulted first, then the secondary.
	 * 
	 * @param key the preference key (full form)
	 * @param items the items to order
	 * @param primary the primary ordering
	 * @param secondary the secondary orderings
	 * @return the ordered list of items
	 */
	public static List getOrderedList(List items, List primary, List[] secondary) {
		List orderedList = new ArrayList();
		Set itemsRemaining = new HashSet(items);
					
		// satisfy the primary ordering first, if there is one
		if (primary != null) {
			Iterator iter = primary.iterator();
			while (iter.hasNext()) {
				String item = (String)iter.next();
				if (itemsRemaining.contains(item)) {
					orderedList.add(item);
					itemsRemaining.remove(item);
				}
			}
		}
		
		// if there are any remaining items, order them
		if (!itemsRemaining.isEmpty()) {
			if (secondary != null && secondary.length > 0) {
				List secondaryOrderingsRemaining = new ArrayList(Arrays.asList(secondary));
				List bestMatch;
				while ((bestMatch = findBestMatch(itemsRemaining, secondaryOrderingsRemaining)) != null) {
					// satisfy this ordering
					Iterator iter = bestMatch.iterator();
					while (iter.hasNext()) {
						String item = (String)iter.next();
						if (itemsRemaining.contains(item)) {
							orderedList.add(item);
							itemsRemaining.remove(item);
							secondaryOrderingsRemaining.remove(bestMatch);
						}
					}
				}
			}
			// add the rest at the end, in the original order
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				Object item = iter.next();
				if (itemsRemaining.contains(item)) {
					orderedList.add(item);
				}
			}
		}
		return orderedList;
	}

	/**
	 * Returns the preferences for all products in the runtime environment (even if
	 * they are not active).
	 * 
	 * @return all product preferences
	 */
	public static synchronized Properties[] getProductPreferences() {
		if (productPreferences == null) {
			Collection collection = new ArrayList();
			IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.core.runtime.products"); //$NON-NLS-1$
			for (int i=0;i<elements.length;++i) {
				if (elements[i].getName().equals("product")) { //$NON-NLS-1$
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
							}
						}
					}
				}
			}
			productPreferences = (Properties[])collection.toArray(new Properties[collection.size()]);
		}
		return productPreferences;
	}
	
	/**
	 * Returns all the unique values for the given key in all the properties
	 * provided. For example, if two of them have "true" and one has "false", it
	 * will return "true" and "false".
	 * 
	 * @param key the property key
	 * @param properties the properties to search for values
	 * @return the unique values
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
	
	/**
	 * Returns the value for the given key by consulting the given properties, but giving
	 * precedence to the primary properties. If the primary properties has the key, it is
	 * returned. Otherwise, it will return the value of the first secondary properties that
	 * has the key, or null if none of them has it.
	 * 
	 * @param key the key whose value to get
	 * @param primary the primary properties to look through first, or null
	 * @param secondary the secondary properties
	 * @return the value for the given key
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

	/**
	 * Loads and returns the properties in the given properties file. The path is
	 * relative to the bundle with the given id.
	 * 
	 * @param bundleId the bundle id, e.g. "org.eclipse.help"
	 * @param path the bundle-relative path to the file, e.g. "myFolder/myFile.ext"
	 * @return the loaded properties
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
	
	/**
	 * Tokenizes the given list of items, allowing them to be separated by whitespace, commas,
	 * and/or semicolons.
	 * 
	 * e.g. "item1, item2, item3"
	 * would return a list of strings containing "item1", "item2", and "item3".
	 * 
	 * @param str the list of items to tokenize
	 * @return the tokenized list of items
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
