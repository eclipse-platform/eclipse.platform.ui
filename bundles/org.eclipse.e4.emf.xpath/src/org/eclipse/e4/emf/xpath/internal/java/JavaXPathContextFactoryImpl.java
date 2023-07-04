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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JavaXPathContextFactoryImpl<T> extends XPathContextFactory<T> {

	@Override
	public XPathContext newContext(T contextBean) {
		return newContext(null, contextBean);
	}

	@Override
	public XPathContext newContext(XPathContext parentContext, T contextBean) {
		if (!(contextBean instanceof EObject rootObject)) {
			throw new IllegalArgumentException();
		}
		// TODO: consider parent-context (may be null). Require the context-bean to be
		// from the same resource? Then we can also reuse all maps and the XPath object

		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
		Document proxyDoc = documentBuilder.newDocument();

		Map<Node, EObject> domProxy2object = new HashMap<>();
		Map<EObject, Element> object2domProxy = new HashMap<>();
		// The xpath '/' actually referes to the document but it's also expected to
		// match the root application
		domProxy2object.put(proxyDoc, rootObject);

		Element rootElement = createElement(rootObject, proxyDoc);
		object2domProxy.put(rootObject, rootElement);
		domProxy2object.put(rootElement, rootObject);

		rootObject.eAllContents().forEachRemaining(eObject -> {
			Element parent = object2domProxy.get(eObject.eContainer());
			Element proxy = object2domProxy.computeIfAbsent(eObject, o -> createElement(o, parent));
			domProxy2object.put(proxy, eObject);
		});

		return new EObjectContext(rootElement, domProxy2object);
	}

	private static class EObjectContext implements XPathContext {

		private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

		private final XPath xpath;
		private final Element rootElement;
		private final Map<Node, EObject> domProxy2object;

		private EObjectContext(Element rootElement, Map<Node, EObject> domProxy2object) {
			this.rootElement = rootElement;
			this.domProxy2object = Map.copyOf(domProxy2object);
			this.xpath = XPATH_FACTORY.newXPath();
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
			if (pathNodes.size() == 0) {
				throw new XPathNotFoundException("No value for xpath: " + xpath);
			}
			return StreamSupport.stream(pathNodes.spliterator(), false).map(node -> {
				if (node instanceof Element || node instanceof Document) {
					return domProxy2object.get(node);
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
			if (arity == 1 && "ecore".equals(functionName.getNamespaceURI())
					&& "eClassName".equals(functionName.getLocalPart())) {
				return args -> {
					Node item = getSingleNodeArgument(args);
					return EObjectContext.this.domProxy2object.get(item).eClass().getName();
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

	private static Element createElement(EObject eObject, Node parent) {
		EStructuralFeature containingFeature = eObject.eContainingFeature();
		EStructuralFeature containmentFeature = eObject.eContainmentFeature();
		String className = eObject.eClass().getName();
		String qName = containingFeature != null ? containingFeature.getName() : className;

		Document document = parent instanceof Document documentParent ? documentParent : parent.getOwnerDocument();
		Element element = document.createElement(qName);
		EClass eClass = eObject.eClass();
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			// TODO: or check how lists could be serialized as attributes? CSV? With
			// leading/trailing square-bracket?
			if (!attribute.isMany() && attribute.getEAttributeType().isSerializable() && eObject.eIsSet(attribute)) {
				Object value = eObject.eGet(attribute);
				try {
					String stringValue = EcoreUtil.convertToString(attribute.getEAttributeType(), value);
					element.setAttribute(attribute.getName(), stringValue);
				} catch (Exception e) {
					// TODO: avoid the occurrence of exceptions
				}
			}
		}
		// TODO: set the xsi:type? This requires to declare the EPackage nsURI as xml
		// namespace initially, but it's unsure to me if that's even necessary.
//		String name = eClass.getName();
//		childElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", name);

		parent.appendChild(element);
		return element;
	}
}
