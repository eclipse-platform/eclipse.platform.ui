package org.eclipse.e4.emf.xpath.internal.java;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

// TODO Is this still needed?
public class EObjectNodePointer implements Element {

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