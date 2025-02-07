/*******************************************************************************
 * Copyright (c) 2023, 2025 Hannes Wellmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hannes Wellmann - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.emf.xpath.internal.java;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathNodes;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathNotFoundException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.DefaultDOMHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JavaXPathContextFactoryImpl<T> extends XPathContextFactory<T> {

	private static final boolean DEBUG = false;

	@Override
	public XPathContext newContext(T contextBean) {
		return newContext(null, contextBean);
	}

	@Override
	public XPathContext newContext(XPathContext parentContext, T contextBean) {
		if (!(contextBean instanceof EObject rootObject)) {
			throw new IllegalArgumentException();
		}

		if (parentContext != null) {
			EObjectContext parent = ((EObjectContext) parentContext);
			Element rootElement = parent.domMapper.getElement(contextBean);
			if (rootElement == null) {
				throw new IllegalArgumentException("Context bean is not from the same tree its parent context");
			}
			return new EObjectContext(rootElement, parent.xpath.getNamespaceContext(), parent.domMapper);
		}

		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}

		Document document = documentBuilder.newDocument();
		DOMMapping domHandler = new DOMMapping();
		Element rootElement = createElement(rootObject, document, domHandler);
		if (DEBUG) {
			dump(document);
		}
		return new EObjectContext(rootElement, createNamespaceContext(rootElement), domHandler);
	}

	private static class EObjectContext implements XPathContext {

		private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

		private final XPath xpath;
		private final Element rootElement;
		private final DOMMapping domMapper;

		private EObjectContext(Element rootElement, NamespaceContext namespaceContext, DOMMapping domMapper) {
			this.rootElement = rootElement;
			this.domMapper = domMapper;
			this.xpath = XPATH_FACTORY.newXPath();
			this.xpath.setNamespaceContext(namespaceContext);
			this.xpath.setXPathFunctionResolver(this::resolveEMFFunctions);
		}

		@Override
		public <R> Stream<R> stream(String path, Class<R> resultType) {
			// See XPathResultType for generally supported result types
			Class<?> type = XPathNodes.class;
			if (resultType == Boolean.class || resultType == String.class
					|| Number.class.isAssignableFrom(resultType)) {
				type = resultType;
			}

			String pathEnhanced = path;
			if (path.equals("/")) {
				pathEnhanced = "/" + rootElement.getTagName();
			} else if (path.startsWith("/") && !path.startsWith("//")) {
				// The xpath '/' actually refers to the document but it's also expected to
				// match the root object
				pathEnhanced = "/" + rootElement.getTagName() + path;
			}

			// Fix the different root and allow .[predicate] and ..[predicate] which is
			// actually not permitted in XPath-1
			pathEnhanced = pathEnhanced.replace("..[", "parent::node()[").replace(".[", "self::node()[");

			Object result;
			try {
				result = xpath.evaluateExpression(pathEnhanced, rootElement, type);
			} catch (XPathExpressionException e) {
				throw new IllegalArgumentException("Illegal xpath: " + path, e);
			}
			if (!(result instanceof XPathNodes pathNodes)) {
				return Stream.of(resultType.cast(result));
			}
			if (pathNodes.size() == 0) {
				throw new XPathNotFoundException("No value for xpath: " + xpath);
			}
			return StreamSupport.stream(pathNodes.spliterator(), false).map(node -> {
				if (node instanceof Element || node instanceof Document) {
					return (EObject) domMapper.getValue(node);
				} else if (node instanceof Attr attribute) {
					return attribute.getValue();
				}
				return node;
			}).peek(Objects::requireNonNull).filter(resultType::isInstance).map(resultType::cast);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <R> Iterator<R> iterate(String xpath) {
			return (Iterator<R>) stream(xpath, Object.class).iterator();
		}

		@Override
		public <R> R getValue(String xpath, Class<R> requiredType) {
			return requiredType.cast(getValue(xpath));
		}

		@Override
		public Object getValue(String xpath) {
			Iterator<Object> iterate = iterate(xpath);
			return iterate.hasNext() ? iterate.next() : null;
		}

		private XPathFunction resolveEMFFunctions(QName functionName, int arity) {
			if (arity == 1 && EcorePackage.eNS_URI.equals(functionName.getNamespaceURI())
					&& "eClassName".equals(functionName.getLocalPart())) {
				return args -> {
					Node item = getSingleNodeArgument(args);
					EObject eObject = (EObject) EObjectContext.this.domMapper.getValue(item);
					return eObject == null ? null : eObject.eClass().getName();
				};
			}
			return null;
		}

		private static Node getSingleNodeArgument(List<?> args) throws XPathFunctionException {
			if (args != null && args.size() == 1) {
				Object argument = args.get(0);
				if (argument instanceof NodeList nodeList && nodeList.getLength() == 1) {
					return nodeList.item(0);
				} else if (argument instanceof Node node) {
					return node;
				}
			}
			throw new XPathFunctionException("Not a single node list: " + args);
		}
	}

	private static Element createElement(EObject eObject, Document document, DOMMapping domMapper) {
		new XMLSaveImpl(Map.of(), new XMIHelperImpl(), "UTF-8").save(null, document,
				Map.of(XMLResource.OPTION_ROOT_OBJECTS, List.of(eObject)), domMapper);
		return document.getDocumentElement();
	}

	private static NamespaceContext createNamespaceContext(Element element) {
		element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ecore", EcorePackage.eNS_URI);
		return new NamespaceContext() {
			final Map<String, String> xmlnsPrefixMap = new HashMap<>();
			final Map<String, String> xmlnsPrefixMapInverse = new HashMap<>();

			{
				NamedNodeMap attributes = element.getAttributes();
				for (int i = 0, length = attributes.getLength(); i < length; ++i) {
					Attr attribute = (Attr) attributes.item(i);
					if ("xmlns".equals(attribute.getPrefix())) {
						String prefix = attribute.getLocalName();
						String namespace = attribute.getValue();
						xmlnsPrefixMap.put(prefix, namespace);
						xmlnsPrefixMapInverse.put(namespace, prefix);
					}
				}
			}

			@Override
			public String getNamespaceURI(String prefix) {
				return xmlnsPrefixMap.get(prefix);
			}

			@Override
			public String getPrefix(String namespaceURI) {
				return xmlnsPrefixMapInverse.get(namespaceURI);
			}

			@Override
			public Iterator<String> getPrefixes(String namespaceURI) {
				String prefix = getPrefix(namespaceURI);
				List<String> list = prefix == null ? List.of() : List.of(prefix);
				return list.iterator();
			}
		};
	}

	private static class DOMMapping extends DefaultDOMHandlerImpl {
		public Element getElement(Object object) {
			for (Entry<Node, Object> entry : nodeToObject.entrySet()) {
				if (Objects.equals(entry.getValue(), object)) {
					return (Element) entry.getKey();
				}
			}
			return null;
		}
	}

	private static void dump(Document document) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter out = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(out));
			out.close();
			System.out.print(out);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
