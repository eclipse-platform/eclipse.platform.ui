package org.eclipse.e4.emf.xpath.internal.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

		Element rootElement = createElement(contextObject, proxyDoc, proxyDoc);
		object2proxy.put(contextObject, rootElement);
		proxy2object.put(rootElement, contextObject);

		contextObject.eAllContents().forEachRemaining(eObject -> {
			EObject eParent = eObject.eContainer();
			Element eParentNode = object2proxy.get(eParent);
			Element eObjectNode = object2proxy.computeIfAbsent(eObject,
					(x) -> createElement(eObject, eParentNode, proxyDoc));
			proxy2object.put(eObjectNode, eObject);
		});
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

}