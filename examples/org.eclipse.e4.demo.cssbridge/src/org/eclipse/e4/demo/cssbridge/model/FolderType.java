/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
