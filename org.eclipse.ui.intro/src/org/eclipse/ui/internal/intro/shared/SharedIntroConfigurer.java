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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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

	public SharedIntroConfigurer() {
		initialize();
	}

	public String getVariable(String variableName) {
		IProduct product = Platform.getProduct();
		if (product != null) {
			String value = product.getProperty(variableName);
			if (value != null) {
				if (value.startsWith("bundle:")) {
					try {
						Bundle bundle = product.getDefiningBundle();
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
		return null;
	}

	public IntroElement[] getGroupChildren(String pageId, String groupId) {
		if (pageId.equals("root"))
			if (groupId.equals("page-links"))
				return getRootPageLinks();
		return null;
	}

	private IntroElement[] getRootPageLinks() {
		ArrayList links = new ArrayList();
		links.add(createRootLink("Overview", "http://org.eclipse.ui.intro/showPage?id=overview", "overview"));
		links.add(createRootLink("Tutorials", "http://org.eclipse.ui.intro/showPage?id=tutorials",
				"tutorials"));
		links.add(createRootLink("Samples", "http://org.eclipse.ui.intro/showPage?id=samples", "samples"));
		links.add(createRootLink("What's New", "http://org.eclipse.ui.intro/showPage?id=news", "news"));
		return (IntroElement[]) links.toArray(new IntroElement[links.size()]);
	}

	private IntroElement createRootLink(String name, String url, String id) {
		IntroElement element = new IntroElement("link");
		element.setAttribute("label", name);
		element.setAttribute("url", url);
		element.setAttribute("id", id);
		return element;
	}

	private void initialize() {
		// load the data
	}
}