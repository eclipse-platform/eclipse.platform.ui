/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import org.eclipse.help.ICommandLink;
import org.w3c.dom.Element;

public class CommandLink extends UAElement implements ICommandLink {

	public static final String NAME = "command"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LABEL = "label"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SERIALIZATION = "serialization"; //$NON-NLS-1$

	public CommandLink(ICommandLink src) {
		super(NAME, src);
		setLabel(src.getLabel());
		setSerialization(src.getSerialization());
	}
	
	public CommandLink(Element src) {
		super(src);
	}

	public String getLabel() {
		return getAttribute(ATTRIBUTE_LABEL);
	}

	public String getSerialization() {
		return getAttribute(ATTRIBUTE_SERIALIZATION);
	}

	public void setLabel(String label) {
		setAttribute(ATTRIBUTE_LABEL, label);
	}
	
	public void setSerialization(String serialization) {
		setAttribute(ATTRIBUTE_SERIALIZATION, serialization);
	}
}
