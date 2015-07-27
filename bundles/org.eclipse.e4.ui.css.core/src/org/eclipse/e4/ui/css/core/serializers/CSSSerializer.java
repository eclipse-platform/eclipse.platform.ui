/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * CSS Serializer to retrieve default CSSStyleDeclaration of the SWT Widgets.
 */

public class CSSSerializer {

	public CSSSerializer() {

	}

	/**
	 * Build CSS Style Sheet content of the <code>element</code> Object by
	 * using {@link CSSEngine} engine configuration. The serialization result is
	 * stored into the <code>writer</code>. If
	 * <code>serializeChildNodes</code> is true, the method will serialize too
	 * the child nodes of the <code>element</code>.
	 *
	 * @param writer
	 * @param engine
	 * @param element
	 * @param serializeChildNodes
	 * @throws IOException
	 */
	public void serialize(Writer writer, CSSEngine engine, Object element, boolean serializeChildNodes) throws IOException {
		serialize(writer, engine, element, serializeChildNodes, null);
	}

	/**
	 * Build CSS Style Sheet content of the <code>element</code> Object by
	 * using {@link CSSEngine} engine configuration. The serialization result is
	 * stored into the <code>writer</code>. If
	 * <code>serializeChildNodes</code> is true, the method will serialize too
	 * the child nodes of the <code>element</code>. The
	 * {@link CSSSerializerConfiguration} <code>configuration</code> is used
	 * to generate selector with condition like Text[style='SWT.MULTI'].
	 *
	 * @param writer
	 * @param engine
	 * @param element
	 * @param serializeChildNodes
	 * @param configuration
	 * @throws IOException
	 */
	public void serialize(Writer writer, CSSEngine engine, Object element, boolean serializeChildNodes, CSSSerializerConfiguration configuration) throws IOException {
		Map<String, CSSStyleDeclaration> selectors = new HashMap<String, CSSStyleDeclaration>();
		serialize(writer, engine, element, serializeChildNodes, selectors, configuration);
		boolean firstSelector = true;
		for (Map.Entry<String, CSSStyleDeclaration> entry : selectors.entrySet()) {
			String selectorName = entry.getKey();
			CSSStyleDeclaration styleDeclaration = entry.getValue();
			if (styleDeclaration != null) {
				int length = styleDeclaration.getLength();
				// Start selector
				startSelector(writer, selectorName, firstSelector);
				firstSelector = false;
				for (int i = 0; i < length; i++) {
					String propertyName = styleDeclaration.item(i);
					String propertyValue = styleDeclaration.getPropertyValue(propertyName);
					property(writer, propertyName, propertyValue);
				}
				// End selector
				endSelector(writer, selectorName);
			}
		}
	}

	/**
	 * Build CSS Style Sheet content of the <code>element</code> Object by
	 * using {@link CSSEngine} engine configuration. The serialization result is
	 * stored into the <code>writer</code>. If
	 * <code>serializeChildNodes</code> is true, the method will serialize too
	 * the child nodes of the <code>element</code>. The
	 * {@link CSSSerializerConfiguration} <code>configuration</code> is used
	 * to generate selector with condition like Text[style='SWT.MULTI'].
	 *
	 * Map of <code>selectors</code> contains the selector already built.
	 *
	 * @param writer
	 * @param engine
	 * @param element
	 * @param serializeChildNodes
	 * @param selectors
	 * @param configuration
	 * @throws IOException
	 */
	protected void serialize(Writer writer, CSSEngine engine, Object element, boolean serializeChildNodes, Map<String, CSSStyleDeclaration> selectors,
			CSSSerializerConfiguration configuration) throws IOException {
		Element elt = engine.getElement(element);
		if (elt != null) {
			StringBuilder selectorName = new StringBuilder(elt.getLocalName());
			CSSStyleDeclaration styleDeclaration = engine.getDefaultStyleDeclaration(element, null);

			if (configuration != null) {
				String[] attributesFilter = configuration.getAttributesFilter();
				for (int i = 0; i < attributesFilter.length; i++) {
					String attributeFilter = attributesFilter[i];
					String value = elt.getAttribute(attributeFilter);
					if (value != null && value.length() > 0) {
						selectorName.append("[");
						selectorName.append(attributeFilter);
						selectorName.append("=");
						if (value.indexOf(".") != -1) {
							value = "'" + value + "'";
						}
						selectorName.append(value);
						selectorName.append("]");
						break;
					}
				}
			}

			selectors.put(selectorName.toString(), styleDeclaration);
			if (serializeChildNodes) {
				NodeList nodes = elt.getChildNodes();
				if (nodes != null) {
					for (int k = 0; k < nodes.getLength(); k++) {
						serialize(writer, engine, nodes.item(k),
								serializeChildNodes, selectors, configuration);
					}
				}
			}
		}
	}

	/**
	 * Generate start selector.
	 *
	 * @param writer
	 * @param selectorName
	 * @param firstSelector
	 * @throws IOException
	 */
	protected void startSelector(Writer writer, String selectorName, boolean firstSelector) throws IOException {
		if (firstSelector == false) {
			writer.write("\n\n");
		}
		writer.write(selectorName + " {");
	}

	/**
	 * Generate end selector.
	 *
	 * @param writer
	 * @param selectorName
	 * @throws IOException
	 */
	protected void endSelector(Writer writer, String selectorName) throws IOException {
		writer.write("\n}");
	}

	/**
	 * Generate CSS Property.
	 *
	 * @param writer
	 * @param propertyName
	 * @param propertyValue
	 * @throws IOException
	 */
	private void property(Writer writer, String propertyName, String propertyValue) throws IOException {
		writer.write("\n\t" + propertyName + ":" + propertyValue + ";");
	}
}
