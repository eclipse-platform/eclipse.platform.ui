/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.core.sac.parsers.flute;

import org.akrogen.tkui.css.core.SACConstants;
import org.akrogen.tkui.css.core.sac.parsers.AbstractParseStyleSheet;

public class ParseStyleSheet extends AbstractParseStyleSheet {

	public ParseStyleSheet() {
		super(SACConstants.SACPARSER_FLUTE);
	}

	public static void main(String[] args) {
		ParseStyleSheet p = new ParseStyleSheet();
		p.parseStyleSheet();
	}
}
