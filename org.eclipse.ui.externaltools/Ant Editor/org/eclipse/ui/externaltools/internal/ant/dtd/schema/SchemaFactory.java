/*====================================================================
Copyright (c) 2002, 2003 Object Factory Inc.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Object Factory Inc. - Initial implementation
====================================================================*/
package org.eclipse.ui.externaltools.internal.ant.dtd.schema;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.ui.externaltools.internal.ant.dtd.IModel;
import org.eclipse.ui.externaltools.internal.ant.dtd.ISchema;
import org.eclipse.ui.externaltools.internal.ant.dtd.util.Local;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;

/**
 * SchemaFactory is a SAX DeclHandler that converts DTD ELEMENT and ATTLIST
 * declarations to schema form on the fly. The only two methods available to
 * external users of SchemaFactory are its constructor and
 * <code>getSchema()</code>. The latter returns the schema built by this process
 * and should not be called until the XML parser to which this handler is
 * attached has finished parsing.
 * @author Bob Foster
 */
public class SchemaFactory implements DeclHandler {
	// used for parsing models
	private char[] fBuf;
	private int fLen;
	private int fPos;
	private Element fElement;
	
	private Schema fSchema;
	private static HashSet fTypes = new HashSet();
	private Exception fErrorException;
	static {
		fTypes.add("CDATA");
		fTypes.add("ID");
		fTypes.add("IDREF");
		fTypes.add("IDREFS");
		fTypes.add("NMTOKEN");
		fTypes.add("NMTOKENS");
		fTypes.add("ENTITY");
		fTypes.add("ENTITIES");
	}
	
	/**
	 * Constructor.
	 */
	public SchemaFactory() {
		fSchema = new Schema();
	}
	
	/**
	 * @return ISchema produced from the DeclHandler. The schema is always
	 * correct, though it may be incomplete if the parse was interrupted due to
	 * validation or well-formed errors.
	 */
	public ISchema getSchema() {
		fSchema.setErrorException(fErrorException);
		return fSchema;
	}
	
	/**
	 * @see org.xml.sax.ext.DeclHandler#attributeDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
		Element element = getElement(eName);
		Attribute attr = (Attribute) element.getAttributes().get(aName);
		if (attr == null) {
			attr = new Attribute(aName, element);
			element.addAttribute(attr);
			
			String[] enum = null;
			if (fTypes.contains(type))
				attr.setType(type);
			else if (type.startsWith("NOTATION"))
				enum = parseValues(type.substring("NOTATION".length()+1), ',');
			else
				enum = parseValues(type, '|');
			attr.setEnum(enum);
			
			attr.setRequired(valueDefault == null || !valueDefault.equals("#IMPLIED"));
			attr.setFixed(valueDefault != null && valueDefault.equals("#FIXED"));
			attr.setDefault(value);
		}
	}
	
	/**
	 * @param eName Element name
	 * @return Element from schema or new element. Either way
	 * the element is in the schema upon return.
	 */
	private Element getElement(String eName) {
		Element element = (Element) fSchema.getElement(eName);
		if (element == null) {
			element = new Element(eName);
			fSchema.addElement(element); 
		}
		return element;
	}

	private String[] parseValues(String type, char separator) {
		int start = 0, pos, len = type.length();
		LinkedList values = new LinkedList();
		while (start < len) {
			pos = type.indexOf(separator, start);
			if (pos < 0) pos = len;
			String term = type.substring(start, pos);
			start = pos + 1;
			values.add(term);
		}
		return (String[]) values.toArray(new String[values.size()]);
	}

	/**
	 * @see org.xml.sax.ext.DeclHandler#elementDecl(java.lang.String, java.lang.String)
	 */
	public void elementDecl(String name, String model) throws SAXException {
		Element element = getElement(name);
		if (!element.isUndefined()) {
			// if the element has already been defined, this is an error
			throw new SAXException(Local.format("Element {0} is doubly defined", name));
		}
		
		fElement = element;
		if (model.equals("ANY"))
			element.setAny(true);
		else if (model.equals("EMPTY"))
			element.setEmpty(true);
		else if (model.equals("(#PCDATA)"))
			element.setText(true);
		else if (model.startsWith("(#PCDATA"))
			element.setMixed(true);
		else
			element.setContentModel(parseModel(model));
	}

	/**
	 * Convert model string to IModel. The <code>fElement</code>
	 * variable is an implicit argument to this method, and it
	 * sets <code>fBuf</code>, <code>fPos</code> and <code>fLen</code> for use
	 * by other parser methods.
	 * @param model String from DTD, with parameter entities replaced.
	 * @return IModel
	 * @throws SAXException if syntax error detected in model. This is a
	 * validation error. Since the DTD is usually not read unless the parser is
	 * validating, we may not ever be handed a bad content model, but we need to
	 * check them, just the same.
	 */
	private IModel parseModel(String model) throws SAXException {
		fBuf = model.toCharArray();
		fLen = fBuf.length;
		if (fBuf[0] != '(')
			throw new SAXException(
				Local.format("Element {0} model does not start with left parenthesis", fElement.getName()));

		IModel emodel;
		boolean ortext = model.startsWith("(#PCDATA|");
		if (ortext) {
			fPos = "(#PCDATA".length() + 1;
			emodel = scanExpr();
		}
		else {
			fPos = 0;
			emodel = scanExpr();
		}
		return emodel;
	}

	/**
	 * Scan a parenthesized expression starting
	 * from the left parenthesis or leftmost operator.
	 * @return IModel
	 */
	private IModel scanExpr() throws SAXException {
		// skip opening ( or |
		fPos++;
		return scanExpr(scanElement());
	}
	
	/**
	 * Scan a parenthesized expression with the
	 * first term in hand.
	 * @param term The first operand in the expression, pre-scanned.
	 * @return IModel
	 * @throws SAXException if errors are detected in the model.
	 */
	private IModel scanExpr(IModel term) throws SAXException {
		checkLen();
		if (fBuf[fPos] != ')') {
			char op = fBuf[fPos];
			if (op != '|' && op != ',')
				throw new SAXException(
					Local.format("Expecting operator or right parenthesis in element {0} model {1}", 
						fElement.getName(),
						String.valueOf(fBuf)));
			Model model = new Model(op == '|' ? IModel.CHOICE : IModel.SEQUENCE);
			model.addModel(term);
			term = model;
			
			while (fBuf[fPos] == op) {
				fPos++;
				IModel next = scanElement();
				model.addModel(next);
			}
			if (fBuf[fPos] != ')')
				throw new SAXException(
					Local.format("Expecting operator or right parenthesis in element {0} model {1}", 
						fElement.getName(),
						String.valueOf(fBuf)));
			fPos++;
		}
		return term;
	}

	/**
	 * Scan an element name or a parenthesized sub-expression.
	 * @return IModel
	 * @throws SAXException
	 */
	private IModel scanElement() throws SAXException {
		checkLen();
		if (fBuf[fPos] == '(')
			return scanExpr();
		StringBuffer sb = new StringBuffer();
		while (fBuf[fPos] != '|' && fBuf[fPos] != ',' && fBuf[fPos] != ')'
		&& fBuf[fPos] != '*' && fBuf[fPos] != '+' && fBuf[fPos] != '?' ) {
			sb.append(fBuf[fPos++]);
			checkLen();
		}
		String name = sb.toString();
		Element element = getElement(name);
		Model model = new Model(IModel.LEAF);
		model.setLeaf(element);
		return model;
	}

	private void checkLen() throws SAXException {
		if (fPos == fLen)
			throw new SAXException(
				Local.format("Unexpected end of content model for element {0}: {1}", 
					fElement.getName(),
					String.valueOf(fBuf)));
	}

	/**
	 * @see org.xml.sax.ext.DeclHandler#externalEntityDecl(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
	}

	/**
	 * @see org.xml.sax.ext.DeclHandler#internalEntityDecl(java.lang.String, java.lang.String)
	 */
	public void internalEntityDecl(String name, String value) throws SAXException {
	}
	/**
	 * Method setErrorException.
	 * @param e
	 */
	public void setErrorException(Exception e) {
		fErrorException = e;
	}

}
