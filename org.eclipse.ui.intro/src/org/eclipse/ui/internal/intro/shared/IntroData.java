package org.eclipse.ui.internal.intro.shared;

import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ui.internal.intro.impl.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class IntroData {
	private String productId;
	private Hashtable pages=new Hashtable();
	private boolean active;
	
	public IntroData(String productId, String dataFile, boolean active) {
		this.productId = productId;
		this.active = active;
		initialize(dataFile);
	}
	
	public String getProductId() {
		return productId;
	}

	public boolean isActive() {
		return active;
	}

	private void initialize(String dataFile) {
		Document doc = parse(dataFile);
		if (doc == null)
			return;
		Element root = doc.getDocumentElement();
		NodeList pages = root.getChildNodes();
		for (int i = 0; i < pages.getLength(); i++) {
			Node node = pages.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("page")) { //$NON-NLS-1$
				loadPage((Element) node);
			}
		}
	}

	private void loadPage(Element page) {
		PageData pd = new PageData(page);
		pages.put(pd.getId(), pd);
	}

	private Document parse(String fileURI) {
		Document document = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setValidating(false);
			// if this is not set, Document.getElementsByTagNameNS() will fail.
			docFactory.setNamespaceAware(true);
			docFactory.setExpandEntityReferences(false);
			DocumentBuilder parser = docFactory.newDocumentBuilder();

			document = parser.parse(fileURI);
			return document;

		} catch (SAXParseException spe) {
			StringBuffer buffer = new StringBuffer("IntroData error in line "); //$NON-NLS-1$
			buffer.append(spe.getLineNumber());
			buffer.append(", uri "); //$NON-NLS-1$
			buffer.append(spe.getSystemId());
			buffer.append("\n"); //$NON-NLS-1$   
			buffer.append(spe.getMessage());

			// Use the contained exception.
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			Log.error(buffer.toString(), x);

		} catch (SAXException sxe) {
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			Log.error(x.getMessage(), x);

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			Log.error(pce.getMessage(), pce);

		} catch (IOException ioe) {
			Log.error(ioe.getMessage(), ioe);
		}
		return null;
	}
}
