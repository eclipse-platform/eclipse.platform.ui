/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.model;

public enum FolderType {
	Inbox("Inbox"), Drafts("Drafts"), Sent("Sent");

	private String name;

	private FolderType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
