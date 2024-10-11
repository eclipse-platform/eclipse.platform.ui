package org.eclipse.e4.emf.xpath.internal.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.emf.ecore.EObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

		EObjectContext context;

		try {
			context = new EObjectContext();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}

		context.addRoot(contextObject);
		contextObject.eContents();
		contextObject.eAllContents().forEachRemaining(eObject -> {
			EObject eParent = eObject.eContainer();
			if (context.getNode(eObject) == null) {
				context.add(eObject, eParent);
			}
		});
		/*
		DOMImplementation implementation = proxyDoc.getImplementation();
		final DOMImplementationLS domImplementation = (DOMImplementationLS) implementation;
		LSSerializer serial = domImplementation.createLSSerializer();
		serial.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		String writeToString = serial.writeToString(proxyDoc);
		System.out.println(writeToString);
		*/
//		try {
//			proxyDoc = documentBuilder.parse(new ByteArrayInputStream(writeToString.getBytes(StandardCharsets.UTF_16)));
//
//		} catch (SAXException | IOException e) {
//			throw new IllegalStateException(e);
//		}

		return context;
	}

}