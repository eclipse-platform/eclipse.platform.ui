package org.eclipse.e4.emf.xpath.internal.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathEvaluationResult.XPathResultType;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class JavaXPathFactoryImpl<T> extends XPathContextFactory<T> {

	public static void main(String[] args) throws Exception {
		// parse the XML as a W3C Document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document;
//		FileInputStream inputStream = new FileInputStream(new File("widgets.xml"));
		ByteArrayInputStream inputStream = new ByteArrayInputStream("""
				<foo>
				    <bar/>
				    <bar/>
				    <bar/>
				</foo>
				""".getBytes(StandardCharsets.UTF_8));
		try (InputStream stream = inputStream) {
			document = builder.parse(new File("widgets.xml"));
		}
		document = builder.newDocument();
		Element root = document.createElement("foo");
		root.appendChild(document.createElement("bar"));
		root.appendChild(document.createElement("bar"));
		root.appendChild(document.createElement("bar"));
		document.appendChild(root);

		// Get an XPath object and evaluate the expression
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "/foo/bar";
		Node evaluateExpression = xpath.evaluateExpression(expression, document, Node.class);
		Node widgetNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);

		// or using the evaluateExpression method
		Node widgetNode2 = xpath.evaluateExpression(expression, document, Node.class);
	}

	@Override
	public XPathContext newContext(T contextBean) {
		return newContext(null, contextBean);
	}

	@Override
	public XPathContext newContext(XPathContext parentContext, T contextBean) {
		if (!(contextBean instanceof EObject contextObject)) {
			throw new IllegalArgumentException();
		}
		// TODO: consider parent-context (may be null)

		XPathFactory factory = XPathFactory.newInstance();
//		factory.setXPathFunctionResolver((functionName, arity) -> null);
//		factory.setXPathVariableResolver(e -> null);
		XPath xpath = factory.newXPath();

		DocumentBuilder documentBuilder;
		try {
			documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
		Document proxyDoc = documentBuilder.newDocument();

		Map<Element, EObject> proxy2object = new HashMap<>();
		Map<EObject, Element> object2proxy = new HashMap<>();

		Queue<EObject> queue = new ArrayDeque<>();
		queue.add(contextObject);
		Element rootElement = createElement(contextObject, proxyDoc, proxyDoc);
		object2proxy.put(contextObject, rootElement);
		proxy2object.put(rootElement, contextObject);

		while (!queue.isEmpty()) {
			EObject parent = queue.remove();
			Element parentNode = object2proxy.get(parent);
			List<EObject> eContents = parent.eContents();
			for (EObject childObject : eContents) {
				Element childElement = createElement(childObject, parentNode, proxyDoc);
				proxy2object.put(childElement, childObject);
				object2proxy.put(childObject, childElement);
				queue.add(childObject);
			}
		}
		DOMImplementation implementation = proxyDoc.getImplementation();
		final DOMImplementationLS domImplementation = (DOMImplementationLS) implementation;
		LSSerializer serial = domImplementation.createLSSerializer();
		serial.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		String writeToString = serial.writeToString(proxyDoc);
		System.out.println(writeToString);
//		try {
//			proxyDoc = documentBuilder.parse(new ByteArrayInputStream(writeToString.getBytes(StandardCharsets.UTF_16)));
//
//		} catch (SAXException | IOException e) {
//			throw new IllegalStateException(e);
//		}

		return new XPathContext() {
			@Override
			public Object getValue(String xpath) {
				Iterator<Object> iterate = iterate(xpath);
				return iterate.hasNext() ? iterate.next() : null;
			}

			@Override
			public Object getValue(String xpath, Class<?> requiredType) {
				return requiredType.cast(getValue(xpath));
			}

			@Override
			@SuppressWarnings("unchecked")
			public <O> Iterator<O> iterate(String xpath) {
				return (Iterator<O>) stream(xpath, Object.class).iterator();
			}

			public <R> Stream<R> stream(String path, Class<R> type) {
				try {
					EObjectNodePointer item = new EObjectNodePointer(contextObject);
					XPathEvaluationResult<?> evaluateExpression = xpath.evaluateExpression(path, item);
					XPathNodes value1 = (XPathNodes) evaluateExpression.value();
					if (value1.size() > 0) {
						Node node = value1.get(0);
						System.out.println(node);
					}

					path = "/Application/TrimmedWindow";
					XPathEvaluationResult<?> result1 = xpath.evaluateExpression(path, rootElement);
					XPathNodes nodes2 = (XPathNodes) result1.value();
					if (nodes2.size() > 0) {
						Node node = nodes2.get(0);

						System.out.println(node);
					}
					XPathExpression expr = xpath.compile(path);
					XPathEvaluationResult result = expr.evaluateExpression(rootElement, XPathEvaluationResult.class);
					XPathResultType type2 = result.type();
					Object value = result.value();

					XPathNodes nodes = expr.evaluateExpression(rootElement, XPathNodes.class);
					Stream<Node> stream = StreamSupport
							.stream(Spliterators.spliterator(nodes.iterator(), nodes.size(), 0), false);
					return stream.map(proxy2object::get).map(type::cast);
				} catch (Exception e1) {
					throw new IllegalStateException(e1);
				}
			}

		};
	}

	private Element createElement(EObject childObject, Node parentElement, Document doc) {
		Document document = doc; // parentElement.getOwnerDocument();
		Element child = document.createElement(childObject.eClass().getName());
		List<EAttribute> allAttributes = childObject.eClass().getEAllAttributes();
		for (EAttribute attribute : allAttributes) {
			if (childObject.eIsSet(attribute)) {
				Object value = childObject.eGet(attribute);
				String stringValue = EcoreUtil.convertToString(attribute.getEAttributeType(), value);
				child.setAttribute(attribute.getName(), stringValue);
			}
		}
		parentElement.appendChild(child);
		return child;
	}

	private static class EObjectNodePointer implements Element {

		private final String name;
		private final Document document;
		private final EObject eObject;
		private final EObjectNodePointer parent;
//		private final Map<EObject, EObjectNodePointer> obj2nodes;

		public EObjectNodePointer(EObject eObject) throws ParserConfigurationException {
			this.name = eObject.eClass().getName();
			this.eObject = eObject;
			this.parent = null;
			// TODO: check if document is necessary
			this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}

		public EObjectNodePointer(String name, EObject eObject,
//				Map<EObject, EObjectNodePointer> obj2nodes,
				EObjectNodePointer parent, Document document) {
			this.name = name;
			this.eObject = eObject;
//			this.obj2nodes = obj2nodes;
			this.parent = parent;
			this.document = document;
		}

		// TODO: hashCode/equals?

		@Override
		public String getNodeName() {
			return name;
		}

		@Override
		public String getTagName() {
			return name;
		}

		@Override
		public String getNodeValue() throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void setNodeValue(String nodeValue) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public short getNodeType() {
			return ELEMENT_NODE;
		}

		@Override
		public Node getParentNode() {
			return parent;
		}

		@Override
		public boolean hasChildNodes() {
			EClass eClass = eObject.eClass();
			return availableAttributes(eClass.getEAllAttributes()).findAny().isPresent()
					|| availableReferences(eClass).findAny().isPresent();
		}

		@Override
		public NodeList getChildNodes() {
			EClass eClass = eObject.eClass();
			var attributeNodes = availableAttributes(eClass.getEAllAttributes()).map(this::createAttributeNode);
			var referenceNodes = availableReferences(eClass).flatMap(this::createReferenceNodes);
			List<Node> childNodes = Stream.concat(attributeNodes, referenceNodes).toList();
			return new NodeList() {

				@Override
				public Node item(int index) {
					return childNodes.get(index);
				}

				@Override
				public int getLength() {
					return childNodes.size();
				}
			};
		}

		private Stream<EReference> availableReferences(EClass eClass) {
			return eClass.getEAllReferences().stream().filter(r -> r.isContainment()).filter(eObject::eIsSet);
		}

		private Stream<Element> createReferenceNodes(EReference ref) {
			Object value = this.eObject.eGet(ref);
			Stream<?> stream = value instanceof List<?> list ? list.stream() : Stream.of(value);
			return stream.map(EObject.class::cast)
					.map(e -> new EObjectNodePointer(ref.getName(), e, /* obj2nodes, */ this, document));
		}

		@Override
		public Node getFirstChild() {
			throw unsupportedOperation();
		}

		@Override
		public Node getLastChild() {
			throw unsupportedOperation();
		}

		@Override
		public Node getPreviousSibling() {
			throw unsupportedOperation();
		}

		@Override
		public Node getNextSibling() {
			throw unsupportedOperation();
		}

		@Override
		public boolean hasAttributes() {
			return availableAttributes(eObject.eClass().getEAllAttributes()).findAny().isPresent();
		}

		private Stream<EAttribute> availableAttributes(List<EAttribute> allAttributes) {
			return allAttributes.stream().filter(eObject::eIsSet);
		}

		private Node createAttributeNode(EAttribute attribute) {
			Object value = eObject.eIsSet(attribute) ? eObject.eGet(attribute) : null;
			Attr attributeNode = document.createAttribute(attribute.getName());
			String strValue = EcoreUtil.convertToString(attribute.getEAttributeType(), value);
			attributeNode.setValue(strValue);
			return attributeNode;
		}

		@Override
		public NamedNodeMap getAttributes() {
			List<EAttribute> allAttributes = eObject.eClass().getEAllAttributes();
			return new NamedNodeMap() {

				@Override
				public Node setNamedItemNS(Node arg) throws DOMException {
					throw unsupportedOperation();
				}

				@Override
				public Node setNamedItem(Node arg) throws DOMException {
					throw unsupportedOperation();
				}

				@Override
				public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
					throw unsupportedOperation();
				}

				@Override
				public Node removeNamedItem(String name) throws DOMException {
					throw unsupportedOperation();
				}

				@Override
				public Node item(int index) {
					var attribute = availableAttributes(allAttributes).skip(index).findFirst();
					return attribute.isPresent() ? createAttributeNode(attribute.get()) : null;
				}

				@Override
				public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
					throw unsupportedOperation();
				}

				@Override
				public Node getNamedItem(String name) {
					var attribute = allAttributes.stream().filter(a -> a.getName().equals(name)).findFirst();
					return attribute.isPresent() ? createAttributeNode(attribute.get()) : null;
				}

				@Override
				public int getLength() {
					return (int) availableAttributes(allAttributes).count();
				}
			};
		}

		@Override
		public String getAttribute(String name) {
			return getAttributes().getNamedItem(name).getNodeValue();
		}

		@Override
		public Document getOwnerDocument() {
			return document;
		}

		@Override
		public boolean isSameNode(Node other) {
			return other instanceof EObjectNodePointer node //
					&& node.name == name && node.eObject == eObject;
		}

		@Override
		public boolean isEqualNode(Node arg) {
			return arg instanceof EObjectNodePointer node && Objects.equals(node.eObject, eObject);
		}

		@Override
		public String toString() {
			return "[" + getTagName() + availableAttributes(eObject.eClass().getEAllAttributes())
					.map(a -> a.getName() + "=" + eObject.eGet(a)).collect(Collectors.joining(" ", " ", "]"));
		}

		private static UnsupportedOperationException unsupportedOperation() {
			return new UnsupportedOperationException();
		}

		@Override
		public Node insertBefore(Node newChild, Node refChild) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Node removeChild(Node oldChild) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Node appendChild(Node newChild) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Node cloneNode(boolean deep) {
			throw unsupportedOperation();
		}

		@Override
		public void normalize() {
			throw unsupportedOperation();
		}

		@Override
		public boolean isSupported(String feature, String version) {
			throw unsupportedOperation();
		}

		@Override
		public String getNamespaceURI() {
			return null;
		}

		@Override
		public String getPrefix() {
			throw unsupportedOperation();
		}

		@Override
		public void setPrefix(String prefix) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public String getBaseURI() {
			throw unsupportedOperation();
		}

		@Override
		public short compareDocumentPosition(Node other) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public String getTextContent() throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void setTextContent(String textContent) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public String lookupPrefix(String namespaceURI) {
			throw unsupportedOperation();
		}

		@Override
		public boolean isDefaultNamespace(String namespaceURI) {
			throw unsupportedOperation();
		}

		@Override
		public String lookupNamespaceURI(String prefix) {
			throw unsupportedOperation();
		}

		@Override
		public Object getFeature(String feature, String version) {
			throw unsupportedOperation();
		}

		@Override
		public Object setUserData(String key, Object data, UserDataHandler handler) {
			throw unsupportedOperation();
		}

		@Override
		public Object getUserData(String key) {
			throw unsupportedOperation();
		}

		@Override
		public void setAttribute(String name, String value) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void removeAttribute(String name) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Attr getAttributeNode(String name) {
			return (Attr) getAttributes().getNamedItem(name);
		}

		@Override
		public Attr setAttributeNode(Attr newAttr) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public NodeList getElementsByTagName(String name) {
			throw unsupportedOperation();
		}

		@Override
		public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public boolean hasAttribute(String name) {
			return getAttributes().getNamedItem(name) != null;
		}

		@Override
		public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public TypeInfo getSchemaTypeInfo() {
			throw unsupportedOperation();
		}

		@Override
		public void setIdAttribute(String name, boolean isId) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
			throw unsupportedOperation();
		}

		@Override
		public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
			throw unsupportedOperation();
		}

	}

}