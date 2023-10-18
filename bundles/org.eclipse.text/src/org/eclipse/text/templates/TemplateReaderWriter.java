/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.eclipse.osgi.util.NLS;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.templates.Template;

/**
 * Serializes templates as character or byte stream and reads the same format
 * back.
 * <p>
 * Clients may instantiate this class, it is not intended to be
 * subclassed.</p>
 *
 * @since 3.7
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TemplateReaderWriter {

	private static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
	private static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String CONTEXT_ATTRIBUTE= "context"; //$NON-NLS-1$
	private static final String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$
	private static final String DELETED_ATTRIBUTE= "deleted"; //$NON-NLS-1$
	/**
	 * @since 3.1
	 */
	private static final String AUTO_INSERTABLE_ATTRIBUTE= "autoinsert"; //$NON-NLS-1$

	/**
	 * Create a new instance.
	 */
	public TemplateReaderWriter() {
	}

	/**
	 * Reads templates from a reader and returns them. The reader must present
	 * a serialized form as produced by the <code>save</code> method.
	 *
	 * @param reader the reader to read templates from
	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
	 * @throws IOException if reading from the stream fails
	 */
	public TemplatePersistenceData[] read(Reader reader) throws IOException {
		return read(reader, null);
	}

	/**
	 * Reads the template with identifier <code>id</code> from a reader and
	 * returns it. The reader must present a serialized form as produced by the
	 * <code>save</code> method.
	 *
	 * @param reader the reader to read templates from
	 * @param id the id of the template to return
	 * @return the read template, encapsulated in an instances of
	 *         <code>TemplatePersistenceData</code>
	 * @throws IOException if reading from the stream fails
	 * @since 3.1
	 */
	public TemplatePersistenceData readSingle(Reader reader, String id) throws IOException {
		TemplatePersistenceData[] datas= read(new InputSource(reader), null, id);
		if (datas.length > 0)
			return datas[0];
		return null;
	}

	/**
	 * Reads templates from a stream and adds them to the templates.
	 *
	 * @param reader the reader to read templates from
	 * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
	 * @throws IOException if reading from the stream fails
	 */
	public TemplatePersistenceData[] read(Reader reader, ResourceBundle bundle) throws IOException {
		return read(new InputSource(reader), bundle, null);
	}

	/**
	 * Reads templates from a stream and adds them to the templates.
	 *
	 * @param stream the byte stream to read templates from
	 * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
	 * @throws IOException if reading from the stream fails
	 */
	public TemplatePersistenceData[] read(InputStream stream, ResourceBundle bundle) throws IOException {
		return read(new InputSource(stream), bundle, null);
	}

	/**
	 * Reads templates from an <code>InputSource</code> and adds them to the templates.
	 *
	 * @param source the input source
	 * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
	 * @param singleId the template id to extract, or <code>null</code> to read in all templates
	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
	 * @throws IOException if reading from the stream fails
	 */
	private TemplatePersistenceData[] read(InputSource source, ResourceBundle bundle, String singleId) throws IOException {
		try {
			Collection<TemplatePersistenceData> templates= new ArrayList<>();
			Set<String> ids= new HashSet<>();

			@SuppressWarnings("restriction")
			DocumentBuilder parser= org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE();
			parser.setErrorHandler(new DefaultHandler());
			Document document= parser.parse(source);

			NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);

			int count= elements.getLength();
			for (int i= 0; i != count; i++) {
				Node node= elements.item(i);
				NamedNodeMap attributes= node.getAttributes();

				if (attributes == null)
					continue;

				String id= getStringValue(attributes, ID_ATTRIBUTE, null);
				if (id != null && ids.contains(id)) {
					String PLUGIN_ID= "org.eclipse.jface.text"; //$NON-NLS-1$
					ILog log= ILog.of(Platform.getBundle(PLUGIN_ID));
					String message= NLS.bind(TextTemplateMessages.getString("TemplateReaderWriter.duplicate.id"), id); //$NON-NLS-1$
					log.log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null));
				} else {
					ids.add(id);
				}

				if (singleId != null && !singleId.equals(id))
					continue;

				boolean deleted = getBooleanValue(attributes, DELETED_ATTRIBUTE, false);

				String name= getStringValue(attributes, NAME_ATTRIBUTE);
				name= translateString(name, bundle);

				String description= getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$
				description= translateString(description, bundle);

				String context= getStringValue(attributes, CONTEXT_ATTRIBUTE);

				if (name == null || context == null)
					throw new IOException(TextTemplateMessages.getString("TemplateReaderWriter.error.missing_attribute")); //$NON-NLS-1$

				boolean enabled = getBooleanValue(attributes, ENABLED_ATTRIBUTE, true);
				boolean autoInsertable= getBooleanValue(attributes, AUTO_INSERTABLE_ATTRIBUTE, true);

				StringBuilder buffer= new StringBuilder();
				NodeList children= node.getChildNodes();
				for (int j= 0; j != children.getLength(); j++) {
					String value= children.item(j).getNodeValue();
					if (value != null)
						buffer.append(value);
				}
				String pattern= buffer.toString();
				pattern= translateString(pattern, bundle);

				Template template= new Template(name, description, context, pattern, autoInsertable);
				TemplatePersistenceData data= new TemplatePersistenceData(template, enabled, id);
				data.setDeleted(deleted);

				templates.add(data);

				if (singleId != null && singleId.equals(id))
					break;
			}

			return templates.toArray(new TemplatePersistenceData[templates.size()]);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (SAXException e) {
			throw (IOException)new IOException("Could not read template file").initCause(e); //$NON-NLS-1$
		}

		return null; // dummy
	}

	/**
	 * Saves the templates as XML, encoded as UTF-8 onto the given byte stream.
	 *
	 * @param templates the templates to save
	 * @param stream the byte output to write the templates to in XML
	 * @throws IOException if writing the templates fails
	 */
	public void save(TemplatePersistenceData[] templates, OutputStream stream) throws IOException {
		save(templates, new StreamResult(stream));
	}

	/**
	 * Saves the templates as XML.
	 *
	 * @param templates the templates to save
	 * @param writer the writer to write the templates to in XML
	 * @throws IOException if writing the templates fails
	 */
	public void save(TemplatePersistenceData[] templates, Writer writer) throws IOException {
		save(templates, new StreamResult(writer));
	}

	/**
	 * Saves the templates as XML.
	 *
	 * @param templates the templates to save
	 * @param result the stream result to write to
	 * @throws IOException if writing the templates fails
	 */
	private void save(TemplatePersistenceData[] templates, StreamResult result) throws IOException {
		try {
			@SuppressWarnings("restriction")
			Document document= org.eclipse.core.internal.runtime.XmlProcessorFactory.newDocumentWithErrorOnDOCTYPE();
			Node root= document.createElement(TEMPLATE_ROOT);
			document.appendChild(root);

			for (TemplatePersistenceData data : templates) {
				Template template= data.getTemplate();

				Node node= document.createElement(TEMPLATE_ELEMENT);
				root.appendChild(node);

				NamedNodeMap attributes= node.getAttributes();

				String id= data.getId();
				if (id != null) {
					Attr idAttr= document.createAttribute(ID_ATTRIBUTE);
					idAttr.setValue(id);
					attributes.setNamedItem(idAttr);
				}

				if (template != null) {
					Attr name= document.createAttribute(NAME_ATTRIBUTE);
					name.setValue(validateXML(template.getName()));
					attributes.setNamedItem(name);
				}

				if (template != null) {
					Attr description= document.createAttribute(DESCRIPTION_ATTRIBUTE);
					description.setValue(validateXML(template.getDescription()));
					attributes.setNamedItem(description);
				}

				if (template != null) {
					Attr context= document.createAttribute(CONTEXT_ATTRIBUTE);
					context.setValue(validateXML(template.getContextTypeId()));
					attributes.setNamedItem(context);
				}

				Attr enabled= document.createAttribute(ENABLED_ATTRIBUTE);
				enabled.setValue(data.isEnabled() ? Boolean.toString(true) : Boolean.toString(false));
				attributes.setNamedItem(enabled);

				Attr deleted= document.createAttribute(DELETED_ATTRIBUTE);
				deleted.setValue(data.isDeleted() ? Boolean.toString(true) : Boolean.toString(false));
				attributes.setNamedItem(deleted);

				if (template != null) {
					Attr autoInsertable= document.createAttribute(AUTO_INSERTABLE_ATTRIBUTE);
					autoInsertable.setValue(template.isAutoInsertable() ? Boolean.toString(true) : Boolean.toString(false));
					attributes.setNamedItem(autoInsertable);
				}

				if (template != null) {
					Text pattern= document.createTextNode(validateXML(template.getPattern()));
					node.appendChild(pattern);
				}
			}
			@SuppressWarnings("restriction")
			Transformer transformer= org.eclipse.core.internal.runtime.XmlProcessorFactory.createTransformerFactoryWithErrorOnDOCTYPE().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			DOMSource source = new DOMSource(document);

			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (TransformerException e) {
			if (e.getException() instanceof IOException)
				throw (IOException) e.getException();
			Assert.isTrue(false);
		}
	}

	/**
	 * Validates whether the given string only contains valid XML characters.
	 *
	 * @param string the string to validate
	 * @return the input string
	 * @throws IOException when the first invalid character is detected
	 * @since 3.6
	 */
	private static String validateXML(String string) throws IOException {
		for (int i= 0; i < string.length(); i++) {
			char ch= string.charAt(i);
			if (!(ch == 9 || ch == 10 || ch == 13 || ch >= 32))
				throw new IOException("Character reference \"&#" + Integer.toString(ch) + "\" is an invalid XML character."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return string;
	}

	private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException {
		Node enabledNode= attributes.getNamedItem(attribute);
		if (enabledNode == null)
			return defaultValue;
		else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
			return true;
		else if (enabledNode.getNodeValue().equals(Boolean.toString(false)))
			return false;
		else
			throw new SAXException(TextTemplateMessages.getString("TemplateReaderWriter.error.illegal_boolean_attribute")); //$NON-NLS-1$
	}

	private String getStringValue(NamedNodeMap attributes, String name) throws SAXException {
		String val= getStringValue(attributes, name, null);
		if (val == null)
			throw new SAXException(TextTemplateMessages.getString("TemplateReaderWriter.error.missing_attribute")); //$NON-NLS-1$
		return val;
	}

	private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) {
		Node node= attributes.getNamedItem(name);
		return node == null	? defaultValue : node.getNodeValue();
	}

	private String translateString(String str, ResourceBundle bundle) {
		if (bundle == null)
			return str;

		int idx= str.indexOf('%');
		if (idx == -1) {
			return str;
		}
		StringBuilder buf= new StringBuilder();
		int k= 0;
		while (idx != -1) {
			buf.append(str.substring(k, idx));
			for (k= idx + 1; k < str.length() && !Character.isWhitespace(str.charAt(k)); k++) {
				// loop
			}
			String key= str.substring(idx + 1, k);
			buf.append(getBundleString(key, bundle));
			idx= str.indexOf('%', k);
		}
		buf.append(str.substring(k));
		return buf.toString();
	}

	private String getBundleString(String key, ResourceBundle bundle) {
		if (bundle != null) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				return '!' + key + '!';
			}
		}
		return TextTemplateMessages.getString(key); // default messages
	}
}

