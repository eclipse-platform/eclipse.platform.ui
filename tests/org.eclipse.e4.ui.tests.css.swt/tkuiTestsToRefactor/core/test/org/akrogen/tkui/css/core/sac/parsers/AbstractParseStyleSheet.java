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
package org.akrogen.tkui.css.core.sac.parsers;

import org.akrogen.tkui.css.core.resources.CSSCoreResources;
import org.akrogen.tkui.css.core.sac.ISACParserFactory;
import org.akrogen.tkui.css.core.sac.MockDocumentHandler;
import org.akrogen.tkui.css.core.sac.SACParserFactory;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

public abstract class AbstractParseStyleSheet {

	private String parserName;

	public AbstractParseStyleSheet(String parserName) {
		this.parserName = parserName;
	}

	public void parseStyleSheet() {
		ISACParserFactory factory = SACParserFactory.newInstance();
		try {
			// 1. Get SAC Parser
			Parser parser = factory.makeParser(parserName);
			if (parser != null) {
				System.out.println("SAC Parser used="
						+ parser.getClass().getName());
				// 2. Set SAC Document Handler into parser
				DocumentHandler handler = new MockDocumentHandler();
				parser.setDocumentHandler(handler);
				// 3. Parse text.css
				InputSource styleSheetSource = new InputSource();
				styleSheetSource
						.setByteStream(CSSCoreResources.getHTMLSimple());
				parser.parseStyleSheet(styleSheetSource);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
