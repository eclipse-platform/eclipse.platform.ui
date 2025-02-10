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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.eclipse.emf.ecore.EReference;
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

@SuppressWarnings({ "deprecation", "removal" })
public class JavaXPathContextFactoryImpl<T> extends XPathContextFactory<T> {

	private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

	@Override
	public XPathContext newContext(T contextBean) {
		return newContext(null, contextBean);
	}

	@Override
	public XPathContext newContext(XPathContext parentContext, T contextBean) {
		if (!(contextBean instanceof EObject rootObject)) {
			throw new IllegalArgumentException();
		}
		XPath xpath;
		DOMMapping domMapping;
		Element rootElement = null;

		if (parentContext != null) {
			EObjectContext parent = (EObjectContext) parentContext;
			xpath = parent.xpath;
			rootElement = parent.domMapping.getElement(contextBean);
		} else {
			xpath = XPATH_FACTORY.newXPath();
		}

		if (rootElement != null) {
			domMapping = ((EObjectContext) parentContext).domMapping;
		} else {
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new IllegalStateException(e);
			}
			Document document = documentBuilder.newDocument();

			domMapping = new DOMMapping();
			rootElement = createElement(rootObject, document, domMapping);
			xpath.setNamespaceContext(createNamespaceContext(rootElement));
		}
		return new EObjectContext(rootElement, domMapping, xpath);
	}

	private static class EObjectContext implements XPathContext {

		private final XPath xpath;
		private final Element rootElement;
		private final DOMMapping domMapping;

		private EObjectContext(Element rootElement, DOMMapping domMapping, XPath xpath) {
			this.rootElement = rootElement;
			this.domMapping = domMapping;
			this.xpath = xpath;
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

			// Fix the different root and allow .[predicate] and ..[predicate] which is
			// actually not permitted in XPath-1
			String pathEnhanced = path;
			if (path.equals("/")) {
				pathEnhanced = "/" + rootElement.getTagName();
			} else if (path.startsWith("/") && !path.startsWith("//")) {
				// The xpath '/' actually refers to the document but it's also expected to
				// match the root object
				pathEnhanced = "/" + rootElement.getTagName() + path;
			}
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
			return StreamSupport.stream(pathNodes.spliterator(), false).map(node -> {
				if (node instanceof Element || node instanceof Document) {
					return (EObject) domMapping.getValue(node);
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
			Iterator<Object> iterator = iterate(xpath);
			if (!iterator.hasNext()) {
				throw new XPathNotFoundException("No value for xpath: " + xpath);
			}
			Object first = iterator.next();
			if (first instanceof EObject firstEObject) {
				return reconstructReferenceList(firstEObject, xpath, iterator).orElse(first);
			}
			return first;
		}

		private XPathFunction resolveEMFFunctions(QName functionName, int arity) {
			if (arity == 1 && EcorePackage.eNS_URI.equals(functionName.getNamespaceURI())
					&& "eClassName".equals(functionName.getLocalPart())) {
				return args -> {
					Node item = getSingleNodeArgument(args);
					EObject eObject = (EObject) EObjectContext.this.domMapping.getValue(item);
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
		NamedNodeMap attributes = element.getAttributes();
		Map<String, String> xmlnsPrefixMap = IntStream.range(0, attributes.getLength()).mapToObj(attributes::item)
				.map(Attr.class::cast).filter(a -> "xmlns".equals(a.getPrefix()))
				.collect(Collectors.toMap(Attr::getLocalName, Attr::getValue));
		Map<String, String> xmlnsPrefixMapInverse = new HashMap<>();
		xmlnsPrefixMap.forEach((prefix, namespace) -> xmlnsPrefixMapInverse.put(namespace, prefix));

		return new NamespaceContext() {
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
				return prefix == null ? Collections.emptyIterator() : List.of(prefix).iterator();
			}
		};
	}

	private static class DOMMapping extends DefaultDOMHandlerImpl {

		public Element getElement(Object object) {
			for (Map.Entry<Node, Object> entry : nodeToObject.entrySet()) {
				if (Objects.equals(entry.getValue(), object)) {
					return (Element) entry.getKey();
				}
			}
			return null;
		}
	}

	private static Optional<Object> reconstructReferenceList(EObject first, String xpath, Iterator<Object> iterator) {
		EReference containment = first.eContainmentFeature();
		if (containment != null && containment.isMany() && xpath.endsWith("/" + containment.getName())) {
			EObject container = first.eContainer();
			List<EObject> featureList = new ArrayList<>();
			featureList.add(first);
			while (iterator.hasNext()) {
				EObject next = (EObject) iterator.next();
				if (next.eContainer() != container || next.eContainmentFeature() != containment) {
					break;
				}
				featureList.add(next);
			}
			return Optional.of(featureList);
		}
		return Optional.empty();
	}

}
