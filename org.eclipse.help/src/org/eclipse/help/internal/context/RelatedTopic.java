/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import org.eclipse.help.*;
import org.xml.sax.*;
/**
 * Default implementation for a topic contribution
 */
public class RelatedTopic extends ContextsNode implements IHelpResource {
	protected String href;
	protected String label;
	public RelatedTopic(Attributes attrs) {
		super(attrs);
		if (attrs == null)
			return;
		href = attrs.getValue(ContextsNode.RELATED_HREF);
		this.label = attrs.getValue(ContextsNode.RELATED_LABEL);
		if (this.label == null)
			this.label = ""; //$NON-NLS-1$
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Returns the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @see ContextsNode#build(ContextsBuilder)
	 */
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
}
