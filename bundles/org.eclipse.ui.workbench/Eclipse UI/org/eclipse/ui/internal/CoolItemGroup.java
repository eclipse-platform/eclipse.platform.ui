/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.GroupMarker;

/**
 */
public class CoolItemGroup extends GroupMarker implements ICoolItemGroup {
	private String contributingId;
	public CoolItemGroup(String groupName, String contributingId) {
		super(groupName);
		this.contributingId = contributingId;
	}
	/**
	 * Returns the action set id.
	 */
	public String getContributingId() {
		return contributingId;
}
}
