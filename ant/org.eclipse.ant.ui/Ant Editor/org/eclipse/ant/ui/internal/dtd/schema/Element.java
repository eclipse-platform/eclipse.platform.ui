/*******************************************************************************
 * Copyright (c) 2002, 2003 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.dtd.schema;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.ui.internal.dtd.IAttribute;
import org.eclipse.ant.ui.internal.dtd.IDfm;
import org.eclipse.ant.ui.internal.dtd.IElement;
import org.eclipse.ant.ui.internal.dtd.IModel;
import org.eclipse.ant.ui.internal.dtd.ParseError;

public class Element extends Atom implements IElement {
	private boolean fUndefined = true;
	private boolean fText;
	private boolean fMixed;
	private IModel fModel;
	private HashMap fMap = new HashMap(4); 
	private Dfm fElementDfm;

	/**
	 * Constructor
	 * @param name QName of element.
	 */
	public Element(String name) {
		super(ELEMENT, name);
	}

	/**
	 * Set undefined property.
	 * @param undefined False if defined; otherwise true (default).
	 */
	public void setUndefined(boolean undefined) {
		fUndefined = undefined;
	}
	
	/**
	 * Set text property.
	 * @param text True if text only; otherwise false (default).
	 */
	public void setText(boolean text) {
		fText = text;
	}
	
	/**
	 * Set mixed property.
	 * @param mixed True if text plus element content allowed; otherwise false
	 * (default).
	 */
	public void setMixed(boolean mixed) {
		fMixed = mixed;
	}
	
	/**
	 * Set model property.
	 * @param model Dfm describing content model.
	 */
	public void setContentModel(IModel model) {
		fModel = model;
	}
	
	/**
	 * Add an attribute to the attribute map.
	 * @param attribute Attribute to add.
	 */
	public void addAttribute(IAttribute attribute) {
		fMap.put(attribute.getName(), attribute);
	}
	
	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#getAttributes()
	 */
	public Map getAttributes() {
		return fMap;
	}

	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#getContorg.eclipse.ui.externaltools.internal.ant.dtd*/
	public IModel getContentModel() {
		return fModel;
	}

	/**
	 * @see org.eclipse.ui.eorg.eclipse.ui.externaltools.internal.ant.dtdnternal.ant.dtd.IElement#isMixed()
	 */
	public boolean isMixed() {
		return fMixed;
	}
	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#isText()
	 */
	public boolean isText() {
		return fText;
	}

	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#isUndefined()
	 */
	public boolean isUndefined() {
		return fUndefined;
	}

	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#getDfm()
	 */
	public IDfm getDfm() {
		Dfm dfm = fElementDfm;
		if (dfm == null) {
			dfm = parseElementDfm();
			fElementDfm = dfm;
		}
		return dfm;
	}
	
	private Dfm parseElementDfm() {
		Dfm dfm;
		if (fAny) {
			dfm = Dfm.dfm(true);
			dfm.any = true;
		}
		else if (fEmpty || fText) {
			dfm = Dfm.dfm(true);
			dfm.empty = true;
		}
		else {
			dfm = parseModel(fModel);
		}
		return dfm;
	}
	
	private Dfm parseModel(IModel model) {
		Dfm dfm;
		Nfm nfm = model.toNfm();
		if (nfm != null) {
			try {
				dfm = fNfmParser.parse(nfm, true);
			} catch (ParseError e) {
				//??? this would be the place to log the error
				dfm = Dfm.dfm(false);
			}
		}
		else {
			dfm = Dfm.dfm(false);
		}
		return dfm;
	}
	
	private static final NfmParser fNfmParser = new NfmParser();
	private boolean fAny;
	private boolean fEmpty;

	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#isAny()
	 */
	public boolean isAny() {
		return fAny;
	}

	/**
	 * @see org.eclipse.ant.ui.internal.dtd.IElement#isEmpty()
	 */
	public boolean isEmpty() {
		return fEmpty;
	}

	/**
	 * Sets the any.
	 * @param any The any to set
	 */
	public void setAny(boolean any) {
		fAny = any;
	}

	/**
	 * Sets the empty.
	 * @param empty The empty to set
	 */
	public void setEmpty(boolean empty) {
		fEmpty = empty;
	}

}

