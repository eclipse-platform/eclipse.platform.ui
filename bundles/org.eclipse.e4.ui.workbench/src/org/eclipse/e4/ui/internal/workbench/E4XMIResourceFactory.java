/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLParserPool;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;

/**
 * Resource factory for {@link E4XMIResource}.
 */
public class E4XMIResourceFactory extends XMIResourceFactoryImpl {

	/**
	 * List used for EMF {@link XMLResource#OPTION_USE_CACHED_LOOKUP_TABLE} option value. Packaged
	 * in a ThreadLocal per EMF recommendation for thread safety.
	 */
	private final ThreadLocal<List<Object>> lookupTable = new ThreadLocal<>();
	/**
	 * Parser pool object for {@link XMLResource#OPTION_USE_PARSER_POOL} option. Also needed for
	 * setting {@link XMLResource#OPTION_USE_DEPRECATED_METHODS} for false.
	 */
	private final XMLParserPool parserPool = new XMLParserPoolImpl();
	/**
	 * Map used for {@link XMLResource#OPTION_USE_XML_NAME_TO_FEATURE_MAP}. Per EMF documentation,
	 * the map is hosted within a ThreadLocale for thread safety.
	 */
	private final ThreadLocal<Map<Object, Object>> nameToFeatureMap = new ThreadLocal<>();

	@Override
	public Resource createResource(URI uri) {
		final E4XMIResource resource = new E4XMIResource(uri);

		// configure default save/load options, as suggested by
		// EMF: Eclipse Modeling Framework, Second Edition
		// Section 15.5.1
		final Map<Object, Object> saveOptions = resource.getDefaultSaveOptions();
		saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
		saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, lookupTable.get());

		final Map<Object, Object> loadOptions = resource.getDefaultLoadOptions();
		loadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, parserPool);
		loadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, nameToFeatureMap.get());
		loadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
		return resource;
	}
}
