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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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

public class SharedIntroConfigurer extends IntroConfigurer implements ISharedIntroConstants {

	private ArrayList introData=new ArrayList();

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
			if (value.startsWith("bundle:")) { //$NON-NLS-1$
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
		if (pageId.equals(ID_ROOT)) {
			if (groupId.equals(DIV_PAGE_LINKS))
				return getRootPageLinks(false);
		} else if (pageId.equals(ID_STANDBY)) {
			if (groupId.equals(DIV_PAGE_LINKS))
				return getRootPageLinks(true);
		}
		else {
			// other pages
			if (groupId.equals(DIV_PAGE_LINKS))
				return getNavLinks(pageId);
			if (groupId.equals(DIV_LAYOUT_LEFT) || groupId.equals(DIV_LAYOUT_RIGHT) || groupId.equals(DIV_LAYOUT_BOTTOM))
				return getContent(pageId, groupId);
		}
		return new IntroElement[0];
	}

	private IntroElement[] getRootPageLinks(boolean standby) {
		ArrayList links = new ArrayList();
		String ids = getVariable(VAR_INTRO_ROOT_PAGES);
		if (ids != null) {
			StringTokenizer stok = new StringTokenizer(ids, ","); //$NON-NLS-1$
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

	private IntroElement createRootPageLink(String id) {
		if (id.equals(ID_OVERVIEW))
			return createRootLink(Messages.SharedIntroConfigurer_overview_name, "http://org.eclipse.ui.intro/showPage?id=overview", id,  //$NON-NLS-1$
					"overview_img", "css/graphics/root/overview.png", Messages.SharedIntroConfigurer_overview_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_overview_tooltip);
		if (id.equals(ID_FIRSTSTEPS))
			return createRootLink(Messages.SharedIntroConfigurer_firststeps_name, "http://org.eclipse.ui.intro/showPage?id=firststeps",  //$NON-NLS-1$
					id, "firststeps_img", "css/graphics/root/firststeps.png", Messages.SharedIntroConfigurer_firststeps_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_firststeps_tooltip);
		if (id.equals(ID_TUTORIALS))
			return createRootLink(Messages.SharedIntroConfigurer_tutorials_name, "http://org.eclipse.ui.intro/showPage?id=tutorials",  //$NON-NLS-1$
					id, "tutorials_img", "css/graphics/root/tutorials.png", Messages.SharedIntroConfigurer_tutorials_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_tutorials_tooltip);
		if (id.equals(ID_SAMPLES))
			return createRootLink(Messages.SharedIntroConfigurer_samples_name, "http://org.eclipse.ui.intro/showPage?id=samples", id,  //$NON-NLS-1$
					"samples_img", "css/graphics/root/samples.png", Messages.SharedIntroConfigurer_samples_alt, Messages.SharedIntroConfigurer_samples_tooltip); //$NON-NLS-1$ //$NON-NLS-2$
		if (id.equals(ID_WHATSNEW))
			return createRootLink(Messages.SharedIntroConfigurer_whatsnew_name, "http://org.eclipse.ui.intro/showPage?id=whatsnew",  //$NON-NLS-1$
					id, "whatsnew_img", "css/graphics/root/whatsnew.png", Messages.SharedIntroConfigurer_whatsnew_alt, //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_whatsnew_tooltip);
		if (id.equals(ID_MIGRATE))
			return createRootLink(Messages.SharedIntroConfigurer_migrate_name, "http://org.eclipse.ui.intro/showPage?id=migrate", id,  //$NON-NLS-1$
					"migrate_img", "css/graphics/root/migrate.png", Messages.SharedIntroConfigurer_migrate_alt, Messages.SharedIntroConfigurer_migrate_tooltip); //$NON-NLS-1$ //$NON-NLS-2$
		if (id.equals(ID_WEBRESOURCES))
			return createRootLink(Messages.SharedIntroConfigurer_webresources_name, "http://org.eclipse.ui.intro/showPage?id=webresources",  //$NON-NLS-1$
					id, "webresources_img", "css/graphics/root/webresources.png", //$NON-NLS-1$ //$NON-NLS-2$
					Messages.SharedIntroConfigurer_webresources_alt, Messages.SharedIntroConfigurer_webresources_tooltip);
		return null;
	}

	private IntroElement createNavLink(String id, String pageId) {
		if (id.equals(ID_OVERVIEW))
			return createNavLink(Messages.SharedIntroConfigurer_overview_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_FIRSTSTEPS))
			return createNavLink(Messages.SharedIntroConfigurer_firststeps_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_TUTORIALS))
			return createNavLink(Messages.SharedIntroConfigurer_tutorials_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_SAMPLES))
			return createNavLink(Messages.SharedIntroConfigurer_samples_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_WHATSNEW))
			return createNavLink(Messages.SharedIntroConfigurer_whatsnew_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_MIGRATE))
			return createNavLink(Messages.SharedIntroConfigurer_migrate_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
		if (id.equals(ID_WEBRESOURCES))
			return createNavLink(Messages.SharedIntroConfigurer_webresources_nav, "http://org.eclipse.ui.intro/showPage?id=" + id, id, "left");  //$NON-NLS-1$//$NON-NLS-2$ 
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

	private IntroElement [] getContent(String pageId, String groupId) {
		ArrayList result = new ArrayList();
		if (introData.size()>0) {
			//TODO getting the active product one only
			//Eventually we should consult the data from all the products
			IntroData idata = (IntroData)introData.get(0);
			PageData pdata = idata.getPage(pageId);
			if (pdata!=null) {
				pdata.addAnchors(result, groupId);
			}
		}
		// Add the fallback anchor
		IntroElement fallback = new IntroElement("anchor"); //$NON-NLS-1$
		fallback.setAttribute("id", ID_FALLBACK_ANCHOR); //$NON-NLS-1$
		result.add(fallback);
		return (IntroElement[]) result.toArray(new IntroElement[result.size()]);		
	}

	public String resolvePath(String extensionId, String path) {
		IPath ipath = new Path(path);
		String pageId = ipath.segment(0);
		if (introData.size()>0) {
			//TODO getting the active product one only
			//Eventually we should consult the data from all the products
			IntroData idata = (IntroData)introData.get(0);
			PageData pdata = idata.getPage(pageId);
			if (pdata!=null) {
				return pdata.resolvePath(extensionId);
			}
		}
		return null;
	}
}