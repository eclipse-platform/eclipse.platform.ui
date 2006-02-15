/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.ui.internal.intro.universal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.intro.config.IntroConfigurer;
import org.eclipse.ui.intro.config.IntroElement;
import org.osgi.framework.Bundle;

/**
 * This class provides for dynamic configuration of the shared intro implementation based on the
 * data file associated with the product.
 * 
 * @since 3.2
 */

public class UniversalIntroConfigurer extends IntroConfigurer implements ISharedIntroConstants {

	private ArrayList introData = new ArrayList();

	public UniversalIntroConfigurer() {
		initialize();
	}

	public String getVariable(String variableName) {
		IProduct product = Platform.getProduct();
		if (product != null) {
			// try product property first
			String value = getProductProperty(product, variableName);
			if (value != null) {
				value = resolveVariable(product.getDefiningBundle(), value);
				return value;
			}
			// nothing - try preferences
			Preferences prefs = IntroPlugin.getDefault().getPluginPreferences();
			// try to prefix with a preduct id first
			String key = product.getId() + "_" + variableName; //$NON-NLS-1$
			value = prefs.getString(key);
			if (value.length() == 0) {
				// try direct variable name
				key = variableName;
				value = prefs.getString(key);
			}
			if (value.length() > 0)
				value = resolveVariable(product.getDefiningBundle(), value);
			else
				value = null;
			return value;
		}
		return null;
	}

	private String resolveVariable(Bundle bundle, String value) {
		if (value != null) {
			String path = null;
			if (value.startsWith("intro:")) { //$NON-NLS-1$
				bundle = IntroPlugin.getDefault().getBundle();
				path = value.substring(6);
			} else if (value.startsWith("product:")) { //$NON-NLS-1$
				path = value.substring(8);
			} else
				return value;
			try {
				URL url = bundle.getEntry(path);
				if (url != null) {
					URL localURL = Platform.asLocalURL(url);
					return localURL.toString();
				}
			} catch (IOException e) {
				// just use the value as-is
				return value;
			}
		}
		return null;
	}

	private String getProductProperty(IProduct product, String variableName) {
		String value = product.getProperty(variableName);
		if (value == null) {
			// return default values
			if (variableName.equals(VAR_INTRO_BACKGROUND_IMAGE))
				return "css/graphics/root/welcomebckgrd.jpg"; //$NON-NLS-1$
		}
		return value;
	}

	public IntroElement[] getGroupChildren(String pageId, String groupId) {
		if (pageId.equals(ID_ROOT)) {
			if (groupId.equals(DIV_PAGE_LINKS))
				return getRootPageLinks(false);
		} else if (pageId.equals(ID_STANDBY)) {
			if (groupId.equals(DIV_PAGE_LINKS))
				return getRootPageLinks(true);
		} else {
			// other pages
			if (groupId.equals(DIV_PAGE_LINKS))
				return getNavLinks(pageId);
			if (groupId.equals(DIV_CUSTOMIZE))
				return getCustomizeContent(pageId);
			if (groupId.equals(DIV_LAYOUT_LEFT) || groupId.equals(DIV_LAYOUT_RIGHT)
					|| groupId.equals(DIV_LAYOUT_BOTTOM))
				return getContent(pageId, groupId);
		}
		return new IntroElement[0];
	}

	public IntroElement[] getLaunchBarShortcuts() {
		ArrayList links = new ArrayList();
		String ids = getVariable(VAR_INTRO_ROOT_PAGES);
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ","); //$NON-NLS-1$
			while (stok.hasMoreTokens()) {
				String id = stok.nextToken().trim();
				IntroElement page = createLaunchBarShortcut(id);
				if (page != null)
					links.add(page);
			}
		}
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}

	private IntroElement[] getRootPageLinks(boolean standby) {
		ArrayList links = new ArrayList();
		String ids = getVariable(VAR_INTRO_ROOT_PAGES);
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ","); //$NON-NLS-1$
			while (stok.hasMoreTokens()) {
				String id = stok.nextToken().trim();
				IntroElement page = createRootPageLink(id, standby);
				if (page != null)
					links.add(page);
			}
		}
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}

	private IntroElement[] getNavLinks(String pageId) {
		ArrayList links = new ArrayList();
		String ids = getVariable(VAR_INTRO_ROOT_PAGES);
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ","); //$NON-NLS-1$
			while (stok.hasMoreTokens()) {
				String id = stok.nextToken().trim();
				IntroElement page = createNavLink(id, pageId);
				if (page != null)
					links.add(page);
			}
		}
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}



	private IntroElement[] getCustomizeContent(String pageId) {
		IntroElement clink = new IntroElement("link"); //$NON-NLS-1$
		clink
				.setAttribute(
						"url", "http://org.eclipse.ui.intro/runAction?pluginId=org.eclipse.ui.intro&class=org.eclipse.ui.internal.intro.shared.CustomizeCommand&pageId=" + pageId); //$NON-NLS-1$ //$NON-NLS-2$
		clink.setAttribute("label", Messages.SharedIntroConfigurer_customize_label); //$NON-NLS-1$
		clink.setAttribute("id", "customize"); //$NON-NLS-1$ //$NON-NLS-2$
		IntroElement text = new IntroElement("text"); //$NON-NLS-1$
		text.setValue(Messages.SharedIntroConfigurer_customize_text);
		clink.addChild(text);
		return new IntroElement[] { clink };
	}

	private IntroElement createRootPageLink(String id, boolean standby) {
		
		if (id.equals(ID_OVERVIEW))
			return createRootLink(
					Messages.SharedIntroConfigurer_overview_name,
					createPageURL(id, standby), id, 
					"overview_img", "css/graphics/root/overview.png", Messages.SharedIntroConfigurer_overview_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_overview_tooltip);
		if (id.equals(ID_FIRSTSTEPS))
			return createRootLink(
					Messages.SharedIntroConfigurer_firststeps_name,
					createPageURL(id, standby), 
					id,
					"firststeps_img", "css/graphics/root/firststeps.png", Messages.SharedIntroConfigurer_firststeps_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_firststeps_tooltip);
		if (id.equals(ID_TUTORIALS))
			return createRootLink(
					Messages.SharedIntroConfigurer_tutorials_name,
					createPageURL(id, standby), 
					id,
					"tutorials_img", "css/graphics/root/tutorials.png", Messages.SharedIntroConfigurer_tutorials_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_tutorials_tooltip);
		if (id.equals(ID_SAMPLES))
			return createRootLink(
					Messages.SharedIntroConfigurer_samples_name,
					createPageURL(id, standby), id, 
					"samples_img", "css/graphics/root/samples.png", Messages.SharedIntroConfigurer_samples_alt, Messages.SharedIntroConfigurer_samples_tooltip); //$NON-NLS-1$ //$NON-NLS-2$
		if (id.equals(ID_WHATSNEW))
			return createRootLink(
					Messages.SharedIntroConfigurer_whatsnew_name,
					createPageURL(id, standby), 
					id,
					"whatsnew_img", "css/graphics/root/whatsnew.png", Messages.SharedIntroConfigurer_whatsnew_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_whatsnew_tooltip);
		if (id.equals(ID_MIGRATE))
			return createRootLink(
					Messages.SharedIntroConfigurer_migrate_name,
					createPageURL(id, standby), id, 
					"migrate_img", "css/graphics/root/migrate.png", Messages.SharedIntroConfigurer_migrate_alt, Messages.SharedIntroConfigurer_migrate_tooltip); //$NON-NLS-1$ //$NON-NLS-2$
		if (id.equals(ID_WEBRESOURCES))
			return createRootLink(
					Messages.SharedIntroConfigurer_webresources_name,
					createPageURL(id, standby), 
					id, "webresources_img", "css/graphics/root/webresources.png", //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_webresources_alt,
					Messages.SharedIntroConfigurer_webresources_tooltip);
		return null;
	}

	private IntroElement createNavLink(String id, String pageId) {
		if (id.equals(ID_OVERVIEW))
			return createNavLink(Messages.SharedIntroConfigurer_overview_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$ 
		if (id.equals(ID_FIRSTSTEPS))
			return createNavLink(Messages.SharedIntroConfigurer_firststeps_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		if (id.equals(ID_TUTORIALS))
			return createNavLink(Messages.SharedIntroConfigurer_tutorials_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		if (id.equals(ID_SAMPLES))
			return createNavLink(Messages.SharedIntroConfigurer_samples_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		if (id.equals(ID_WHATSNEW))
			return createNavLink(Messages.SharedIntroConfigurer_whatsnew_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		if (id.equals(ID_MIGRATE))
			return createNavLink(Messages.SharedIntroConfigurer_migrate_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		if (id.equals(ID_WEBRESOURCES))
			return createNavLink(Messages.SharedIntroConfigurer_webresources_nav,
					createPageURL(id, false), id, "left"); //$NON-NLS-1$
		return null;
	}
	
	private String createPageURL(String id, boolean standby) {
		String url = "http://org.eclipse.ui.intro/showPage?id=" + id; //$NON-NLS-1$
		if (standby)
			url+= "&standby=false"; //$NON-NLS-1$
		return url;
	}

	private IntroElement createLaunchBarShortcut(String id) {
		if (id.equals(ID_OVERVIEW))
			return createShortcutLink(
					"icons/full/obj16/overview16.png", Messages.SharedIntroConfigurer_overview_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_FIRSTSTEPS))
			return createShortcutLink(
					"icons/full/obj16/firststeps16.png", Messages.SharedIntroConfigurer_firststeps_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_TUTORIALS))
			return createShortcutLink(
					"icons/full/obj16/tutorials16.png", Messages.SharedIntroConfigurer_tutorials_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_SAMPLES))
			return createShortcutLink(
					"icons/full/obj16/samples16.png", Messages.SharedIntroConfigurer_samples_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_WHATSNEW))
			return createShortcutLink(
					"icons/full/obj16/whatsnew16.png", Messages.SharedIntroConfigurer_whatsnew_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_MIGRATE))
			return createShortcutLink(
					"icons/full/obj16/migrate16.png", Messages.SharedIntroConfigurer_migrate_nav, //$NON-NLS-1$
					id);
		if (id.equals(ID_WEBRESOURCES))
			return createShortcutLink(
					"icons/full/obj16/webresources16.png", Messages.SharedIntroConfigurer_webresources_nav, //$NON-NLS-1$
					id);
		return null;
	}

	private IntroElement createRootLink(String name, String url, String id, String imgId, String imgSrc,
			String imgAlt, String imgText) {
		IntroElement element = new IntroElement("link"); //$NON-NLS-1$
		element.setAttribute("label", name); //$NON-NLS-1$
		element.setAttribute("url", url); //$NON-NLS-1$
		element.setAttribute("id", id); //$NON-NLS-1$
		IntroElement img = new IntroElement("img"); //$NON-NLS-1$
		img.setAttribute("id", imgId); //$NON-NLS-1$
		img.setAttribute("style-id", "content-img"); //$NON-NLS-1$ //$NON-NLS-2$
		img.setAttribute("src", imgSrc); //$NON-NLS-1$
		img.setAttribute("alt", imgAlt); //$NON-NLS-1$
		IntroElement text = new IntroElement("text"); //$NON-NLS-1$
		text.setValue(imgText);
		element.addChild(img);
		element.addChild(text);
		return element;
	}

	private IntroElement createNavLink(String label, String url, String id, String styleId) {
		IntroElement element = new IntroElement("link"); //$NON-NLS-1$
		element.setAttribute("label", label); //$NON-NLS-1$
		element.setAttribute("url", url); //$NON-NLS-1$
		element.setAttribute("id", "id"); //$NON-NLS-1$ //$NON-NLS-2$
		element.setAttribute("style-id", styleId); //$NON-NLS-1$
		return element;
	}

	private IntroElement createShortcutLink(String icon, String tooltip, String id) {
		IntroElement element = new IntroElement("shortcut"); //$NON-NLS-1$
		element.setAttribute("icon", icon); //$NON-NLS-1$
		element.setAttribute("tooltip", tooltip); //$NON-NLS-1$
		element.setAttribute("url", createPageURL(id, false)); //$NON-NLS-1$
		return element;
	}

	private void initialize() {
		// add intro data for this product first
		String dataFile = getVariable(VAR_INTRO_DATA);
		String pid = Platform.getProduct().getId();
		if (dataFile != null)
			introData.add(new IntroData(pid, dataFile, true));
		IConfigurationElement[] products = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.core.runtime.products"); //$NON-NLS-1$
		for (int i = 0; i < products.length; i++) {
			IConfigurationElement product = products[i];
			IExtension extension = product.getDeclaringExtension();
			String uid = extension.getUniqueIdentifier();
			// skip this product
			if (pid.equals(uid))
				continue;
			addIntroDataFor(uid, product);
		}
	}

	private void addIntroDataFor(String pid, IConfigurationElement product) {
		IConfigurationElement[] children = product.getChildren("property"); //$NON-NLS-1$
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			String name = child.getAttribute("name"); //$NON-NLS-1$
			if (name != null && name.equals(VAR_INTRO_DATA)) {
				String value = child.getAttribute("value"); //$NON-NLS-1$
				String bid = child.getDeclaringExtension().getNamespace();
				Bundle bundle = Platform.getBundle(bid);
				if (bundle != null) {
					String dataFile = resolveVariable(bundle, value);
					introData.add(new IntroData(pid, dataFile, false));
				}
			}
		}
	}

	private IntroElement[] getContent(String pageId, String groupId) {
		ArrayList result = new ArrayList();
		if (introData.size() > 0) {
			// TODO getting the active product one only
			// Eventually we should consult the data from all the products
			IntroData idata = (IntroData) introData.get(0);
			PageData pdata = idata.getPage(pageId);
			if (pdata != null) {
				pdata.addAnchors(result, groupId);
			}
		}
		// Add the fallback anchor
		IntroElement fallback = new IntroElement("anchor"); //$NON-NLS-1$
		fallback.setAttribute("id", DEFAULT_ANCHOR); //$NON-NLS-1$
		result.add(fallback);
		return (IntroElement[]) result.toArray(new IntroElement[result.size()]);
	}

	public String resolvePath(String extensionId, String path) {
		boolean extensionRelativePath=false;
		IPath ipath = new Path(path);
		String pageId = ipath.segment(0);
		String s2 = ipath.segment(1);
		if (!s2.equals("@")) //$NON-NLS-1$
			extensionRelativePath=true;
		if (introData.size() > 0) {
			// TODO getting the active product one only
			// Eventually we should consult the data from all the products
			IntroData idata = (IntroData) introData.get(0);
			PageData pdata = idata.getPage(pageId);
			if (pdata != null) {
				String resolvedPath=pdata.resolvePath(extensionId);
				if (extensionRelativePath) {
					// not done - use the resolved extension path
					// to complete the source path
					IPath p2 = new Path(resolvedPath);
					IPath p1 = ipath.removeFirstSegments(2);
					// remove the last anchor and append the
					// relative path from the extension
					resolvedPath = p2.removeLastSegments(1).append(p1).toString(); 
				}
				return resolvedPath;
			}
		}
		else {
			// use fallback anchor
			return pageId+DEFAULT_CONTENT_PATH;
		}
		return null;
	}
}