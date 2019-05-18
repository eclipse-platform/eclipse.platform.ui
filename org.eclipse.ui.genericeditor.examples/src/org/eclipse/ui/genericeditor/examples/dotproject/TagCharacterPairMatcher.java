/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Bug 538111 - [generic editor] Extension point for ICharacterPairMatcher
 */
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;

public class TagCharacterPairMatcher extends DefaultCharacterPairMatcher {

	public TagCharacterPairMatcher() {
		super(new char[] { '<', '>', '"', '"' });
	}

}
