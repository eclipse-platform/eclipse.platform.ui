/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.xhtml;

public class UATopicExtension {

	private String targetHref;
	private String targetAnchorId;
	private String contentHref;
	private String contentElementId;
	
	public UATopicExtension(String targetHref, String targetAnchorId, String contentHref, String contentElementId) {
		this.targetHref = targetHref;
		this.targetAnchorId = targetAnchorId;
		this.contentHref = contentHref;
		this.contentElementId = contentElementId;
	}

	public String getTargetHref() {
		return targetHref;
	}

	public String getTargetAnchorId() {
		return targetAnchorId;
	}

	public String getContentHref() {
		return contentHref;
	}

	public String getContentElementId() {
		return contentElementId;
	}
}
