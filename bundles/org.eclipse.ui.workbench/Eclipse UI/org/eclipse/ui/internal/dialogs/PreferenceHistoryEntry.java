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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.Assert;

/**
 * A preference history entry.
 * 
 * @since 3.1
 */
final class PreferenceHistoryEntry {
	private String id;
	private String label;
	private Object argument;
	
	/**
	 * Creates a new entry.
	 * 
	 * @param id the preference page id
	 * @param label the label to display, usually the preference page label
	 * @param argument an argument to pass to the preference page, may be
	 *        <code>null</code>
	 */
	public PreferenceHistoryEntry(String id, String label, Object argument) {
		Assert.isLegal(id != null);
		Assert.isLegal(label != null);
		this.id= id;
		this.label= label;
		this.argument= argument;
	}
	/**
	 * Returns the preference page id.
	 * 
	 * @return the preference page id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Returns the preference page argument.
	 * 
	 * @return the preference page argument
	 */
	public Object getArgument() {
		return argument;
	}
	/**
	 * Returns the preference page label.
	 * 
	 * @return the preference page label
	 */
	public String getLabel() {
		return label;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (argument == null) {
			return id;
		}
		return id + "(" + argument + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PreferenceHistoryEntry) {
			PreferenceHistoryEntry other= (PreferenceHistoryEntry) obj;
			return id.equals(other.id)
					&& (argument == null && other.argument == null
							|| argument.equals(other.argument));
		}
		return super.equals(obj);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int argHash= argument == null ? 0 : argument.hashCode() & 0x0000ffff;
		return id.hashCode() << 16 | argHash;
	}
}
