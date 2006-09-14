/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.List;

import org.eclipse.help.ITopic;
import org.eclipse.help.internal.Node;

public class Topic extends Node implements ITopic {

	private String href;
	private String label;
	private ITopic[] subtopics;
	
	public Topic(String href, String label) {
		this.href = href;
		this.label = label;
	}
	
	public ITopic[] getSubtopics() {
		if (subtopics == null) {
			List list = getChildren(ITopic.class);
			subtopics = (ITopic[])list.toArray(new ITopic[list.size()]);
		}
		return subtopics;
	}

	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}
	
	public void setHref(String href) {
		this.href = href;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
