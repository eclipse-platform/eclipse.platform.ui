/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.ui.internal.intro.shared;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.intro.config.IntroConfigurer;
import org.eclipse.ui.intro.config.IntroElement;
import org.osgi.framework.Bundle;

/**
 * This class provides for dynamic configuration of the shared intro implementation based on the
 * data file associated with the product.
 * 
 * @since 3.2
 */

public class SharedIntroConfigurer extends IntroConfigurer {

	private ArrayList introData;

	public SharedIntroConfigurer() {
		initialize();
	}

	public String getVariable(String variableName) {
		IProduct product = Platform.getProduct();
		if (product != null) {
			String value = product.getProperty(variableName);
			if (value != null) {
				value = resolveVariable(product.getDefiningBundle(), value);
			}
			return value;
		}
		return null;
	}

	private String resolveVariable(Bundle bundle, String value) {
		if (value != null) {
			if (value.startsWith("bundle:")) {
				try {
					String path = value.substring(7);
					URL url = bundle.getEntry(path);
					URL localURL = Platform.asLocalURL(url);
					return localURL.toString();
				} catch (IOException e) {
					// just use the value as-is
					return value;
				}
			}
		}
		return value;
	}

	public IntroElement[] getGroupChildren(String pageId, String groupId) {
		if (pageId.equals("root")) {
			if (groupId.equals("page-links"))
				return getRootPageLinks(false);
		} else if (pageId.equals("standby")) {
			if (groupId.equals("page-links"))
				return getRootPageLinks(true);
		}
		if (groupId.equals("page-links"))
			return getNavLinks(pageId);
		return new IntroElement[0];
	}

	private IntroElement[] getRootPageLinks(boolean standby) {
		ArrayList links = new ArrayList();
		String ids = getVariable("introRootPages");
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ",");
			while (stok.hasMoreTokens()) {
				String id = stok.nextToken().trim();
				IntroElement page = createRootPageLink(id);
				if (page != null)
					links.add(page);
			}
		}
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}

	private IntroElement[] getNavLinks(String pageId) {
		ArrayList links = new ArrayList();
		String ids = getVariable("introRootPages");
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ",");
			while (stok.hasMoreTokens()) {
				String id = stok.nextToken().trim();
				IntroElement page = createNavLink(id, pageId);
				if (page != null)
					links.add(page);
			}
		}
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}

	private IntroElement createRootPageLink(String id) {
		if (id.equals("overview"))
			return createRootLink("Overview", "http://org.eclipse.ui.intro/showPage?id=overview", "overview",
					"overview_img", "css/graphics/root/overview.png", "Overview",
					"Find out what Eclipse is all about");
		if (id.equals("firststeps"))
			return createRootLink("First Steps", "http://org.eclipse.ui.intro/showPage?id=firststeps",
					"firststeps", "firststeps_img", "css/graphics/root/firststeps.png", "First Steps",
					"Make first steps");
		if (id.equals("tutorials"))
			return createRootLink("Tutorials", "http://org.eclipse.ui.intro/showPage?id=tutorials",
					"tutorials", "tutorials_img", "css/graphics/root/tutorials.png", "Tutorials",
					"Go through tutorials");
		if (id.equals("samples"))
			return createRootLink("Samples", "http://org.eclipse.ui.intro/showPage?id=samples", "samples",
					"samples_img", "css/graphics/root/samples.png", "Samples", "Try out the samples");
		if (id.equals("whatsnew"))
			return createRootLink("What's New", "http://org.eclipse.ui.intro/showPage?id=whatsnew",
					"whatsnew", "whatsnew_img", "css/graphics/root/whatsnew.png", "What's New",
					"Find out what is new");
		if (id.equals("migrate"))
			return createRootLink("Migrate", "http://org.eclipse.ui.intro/showPage?id=migrate", "migrate",
					"migrate_img", "css/graphics/root/migrate.png", "Migrate", "Migrate to the new release");
		if (id.equals("webresources"))
			return createRootLink("Web Resources", "http://org.eclipse.ui.intro/showPage?id=webresources",
					"webresources", "webresources_img", "css/graphics/root/webresources.png",
					"Web Resources", "Read more on the Web");
		return null;
	}

	private IntroElement createNavLink(String id, String pageId) {
		if (id.equals("overview"))
			return createNavLink("Overview", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("firststeps"))
			return createNavLink("First Steps", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("tutorials"))
			return createNavLink("Tutorials", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("samples"))
			return createNavLink("Samples", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("whatsnew"))
			return createNavLink("What's New", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("migrate"))
			return createNavLink("Migrate", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		if (id.equals("webresources"))
			return createNavLink("Web Resources", "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");
		return null;
	}

	private IntroElement createRootLink(String name, String url, String id, String imgId, String imgSrc,
			String imgAlt, String imgText) {
		IntroElement element = new IntroElement("link");
		element.setAttribute("label", name);
		element.setAttribute("url", url);
		element.setAttribute("id", id);
		IntroElement img = new IntroElement("img");
		img.setAttribute("id", imgId);
		img.setAttribute("style-id", "content-img");
		img.setAttribute("src", imgSrc);
		img.setAttribute("alt", imgAlt);
		IntroElement text = new IntroElement("text");
		text.setValue(imgText);
		element.addChild(img);
		element.addChild(text);
		return element;
	}

	private IntroElement createNavLink(String label, String url, String id, String styleId) {
		IntroElement element = new IntroElement("link");
		element.setAttribute("label", label);
		element.setAttribute("url", url);
		element.setAttribute("id", "id");
		element.setAttribute("style-id", styleId);
		return element;
	}

	private void initialize() {
		// add intro data for this product first
		String dataFile = getVariable("introData");
		String pid = Platform.getProduct().getId();
		if (dataFile != null)
			introData.add(new IntroData(pid, dataFile, true));
		IConfigurationElement[] products = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.core.runtime.products");
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
		IConfigurationElement[] children = product.getChildren("property");
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			String name = child.getAttribute("name");
			if (name != null && name.equals("introData")) {
				String value = child.getAttribute("value");
				String bid = child.getDeclaringExtension().getNamespace();
				Bundle bundle = Platform.getBundle(bid);
				if (bundle != null) {
					String dataFile = resolveVariable(bundle, value);
					introData.add(new IntroData(pid, dataFile, false));
				}
			}
		}
	}
}