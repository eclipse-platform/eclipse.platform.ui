package org.eclipse.e4.emf.xpath.internal.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class EObjectContext implements XPathContext {
	private final String PROXY_KEY = "EObjectContext.Proxy";
	private final Map<EObject, Node> proxy2node;
	private final Document root;
	private XMLHelper xml;

	public EObjectContext() throws ParserConfigurationException {
		proxy2node = new HashMap<>();
		root = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
	}

	@Override
	public Object getValue(String xpath) {
		Iterator<Object> iterate = iterate(xpath);
		return iterate.hasNext() ? iterate.next() : null;
	}

	@Override
	public <T> T getValue(String xpath, Class<T> requiredType) {
		return requiredType.cast(getValue(xpath));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Iterator<T> iterate(String xpath) {
		return (Iterator<T>) stream(xpath, Object.class).iterator();
	}

	@Override
	public <T> Stream<T> stream(String path, Class<T> type) {
		try {
			XPathFactory factory = XPathFactory.newInstance();
//			factory.setXPathFunctionResolver((functionName, arity) -> null);
//			factory.setXPathVariableResolver(e -> null);
			XPath xpath = factory.newXPath();
			/*
			EObjectNodePointer item = new EObjectNodePointer(contextObject);
			XPathEvaluationResult<?> evaluateExpression = xpath.evaluateExpression(path, item);
			XPathNodes value1 = (XPathNodes) evaluateExpression.value();
			if (value1.size() > 0) {
				Node node = value1.get(0);
				System.out.println(node);
			}*/

//			path = "/Application/TrimmedWindow";
			XPathEvaluationResult<?> result1 = xpath.evaluateExpression(path, root);
			XPathNodes nodes2 = (XPathNodes) result1.value();
			if (nodes2.size() > 0) {
				Node node = nodes2.get(0);

				System.out.println(node);
			}

			prettyPrint();

			XPathExpression expr = xpath.compile(path);
			XPathEvaluationResult result = expr.evaluateExpression(root, XPathEvaluationResult.class);
			XPathResultType type2 = result.type();
			Object value = result.value();

			NodeList nodes1 = (NodeList) expr.evaluate(root, XPathConstants.NODESET);
			XPathNodes nodes = expr.evaluateExpression(root, XPathNodes.class);

			System.out.println(nodes1.getLength());
			System.out.println(nodes.size());

			Stream<Node> stream = StreamSupport.stream(Spliterators.spliterator(nodes.iterator(), nodes.size(), 0),
					false);
			return stream.map(this::getProxy).map(type::cast);
		} catch (Exception e1) {
			throw new IllegalStateException(e1);
		}
	}

	private void prettyPrint() {
		final DOMImplementation implementation = root.getImplementation();
		final DOMImplementationLS domImplementation = (DOMImplementationLS) implementation;
		final LSSerializer serial = domImplementation.createLSSerializer();
		serial.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);

		final String writeToString = serial.writeToString(root);
		System.out.println(writeToString);
	}

	protected Element getNode(EObject proxy) {
		return (Element) proxy2node.get(proxy);
	}

	protected EObject getProxy(Node element) {
		return (EObject) element.getUserData(PROXY_KEY);
	}

	protected Element addRoot(EObject eObject) {
		// TODO What about other types of resources?
		xml = new XMLHelperImpl((XMLResource) eObject.eResource());
		Element object = createElement(eObject);
		root.appendChild(object);
		return object;
	}

	protected Element add(EObject eChildObject, EObject eParentObject) {
		Element childObject = createElement(eChildObject);
		Node parentObject = proxy2node.get(eParentObject);
		parentObject.appendChild(childObject);
		return childObject;
	}

	private Element createElement(EObject eObject) {
		String qName = "application";
		if (eObject.eContainingFeature() != null) {
			qName = xml.getQName(eObject.eContainingFeature());
		}
		Element object = root.createElement(qName);
		object.setUserData(PROXY_KEY, eObject, null);
		proxy2node.put(eObject, object);
		List<EAttribute> eAttributes = eObject.eClass().getEAllAttributes();
		for (EAttribute eAttribute : eAttributes) {
			if (eObject.eIsSet(eAttribute)) {
				Object value = eObject.eGet(eAttribute);
				String stringValue = EcoreUtil.convertToString(eAttribute.getEAttributeType(), value);
				object.setAttribute(eAttribute.getName(), stringValue);
			}
		}
		return object;
	}
}
