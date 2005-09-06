/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.index;

public class IndexTopic implements IIndexTopic {
	String label;
	String href;
	String location;
	
	public IndexTopic(String label, String href, String location) {
		this.label = label;
		this.href = href;
		this.location = location;
	}
	
	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}
	
	public String getLocation() {
		return location;
	}
}
