/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Ralf Heydenreich - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

/**
 * Container for hyperlink ranges for the about dialog text.
 */
final class HyperlinkRange {

	private final int offset;
	private final int length;

	public HyperlinkRange(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	public int offset() {
		return offset;
	}

	public int length() {
		return length;
	}

	public boolean contains(int offset) {
		return offset >= this.offset && offset < this.offset + this.length;
	}
}
