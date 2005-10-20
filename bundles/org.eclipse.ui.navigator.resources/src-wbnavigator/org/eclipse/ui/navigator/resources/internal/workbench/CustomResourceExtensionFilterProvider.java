/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.workbench;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.internal.filters.ExtensionFilterDescriptor;
import org.eclipse.ui.navigator.internal.filters.ExtensionFilterProvider;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author mdelder
 *  
 */
public class CustomResourceExtensionFilterProvider implements ExtensionFilterProvider {

	private static final String CUSTOM_FILTERS_PREFERENCE = "CUSTOM_FILTERS_PREFERENCE"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_PATTERN = "pattern"; //$NON-NLS-1$

	public class CustomFilter {
		public final String name;
		public final String pattern;

		public CustomFilter(String name, String pattern) {
			this.name = name;
			this.pattern = pattern;
		}
	}

	/*
	 * (non-Javadoc) @return a List of ExtensionFilterDescriptor (s)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.filters.ExtensionFilterProvider#getExtensionFilterDescriptors(java.lang.String)
	 */
	public List getExtensionFilterDescriptors(String extensionId, String viewerId) {
		List extFilterDescriptors = new ArrayList();
		ExtensionFilterDescriptor extFilterDescriptor = null;
		ViewerFilter vf = null;

		Preferences preferences = WorkbenchNavigatorPlugin.getDefault().getPluginPreferences();
		String knownCustomFilters = preferences.getString(viewerId + CUSTOM_FILTERS_PREFERENCE);
		CustomFilter[] customFilters = parseCustomFilters(knownCustomFilters);

		for (int i = 0; i < customFilters.length; i++) {
			vf = new ResourcePatternFilter();
			((ResourcePatternFilter) vf).setPatterns(new String[]{customFilters[i].pattern});

			boolean enabledByDefault = true;

			extFilterDescriptor = new ExtensionFilterDescriptor(extensionId + "." + customFilters[i].pattern, extensionId, //$NON-NLS-1$
						customFilters[i].name, customFilters[i].pattern, viewerId, enabledByDefault, vf);

			if (!extFilterDescriptors.contains(extFilterDescriptor))
				extFilterDescriptors.add(extFilterDescriptor);
		}
		return extFilterDescriptors;
	}

	/**
	 * @param knownCustomFilters
	 * @return
	 */
	private CustomFilter[] parseCustomFilters(String knownCustomFilters) {

		List resultList = new ArrayList();
		try {

			Source source = new StreamSource(new StringReader(knownCustomFilters));
			DOMResult result = new DOMResult();
			transform(source, result);

			Node node = result.getNode();
			NodeList children = node.getChildNodes();

			CustomFilter filter = null;
			String name = null;
			String pattern = null;
			Node child = null;
			NamedNodeMap attributes = null;
			Node temp = null;

			for (int i = 0; i < children.getLength(); i++) {
				child = children.item(i);
				attributes = child.getAttributes();
				name = ((temp = attributes.getNamedItem(ATT_NAME)) != null) ? temp.getNodeValue() : ""; //$NON-NLS-1$
				pattern = ((temp = attributes.getNamedItem(ATT_PATTERN)) != null) ? temp.getNodeValue() : ""; //$NON-NLS-1$
				filter = new CustomFilter((name != null) ? name : "", (pattern != null) ? pattern : ""); //$NON-NLS-1$ //$NON-NLS-2$
				resultList.add(filter);
			}

			resultList.toArray(new CustomFilter[resultList.size()]);

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return new CustomFilter[0];
	}


	private void transform(Source s, Result r) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.transform(s, r);
	}
}