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
package org.akrogen.tkui.css.core.sac;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

public class MockDocumentHandler implements org.w3c.css.sac.DocumentHandler {

	public void comment(String text) throws CSSException {
		System.out.println("MockDocumentHandler#comment], text=" + text);

	}

	public void endDocument(InputSource source) throws CSSException {
		System.out.println("[MockDocumentHandler#endDocument], source= "
				+ source);

	}

	public void endFontFace() throws CSSException {
		System.out.println("[MockDocumentHandler#endFontFace]");

	}

	public void endMedia(SACMediaList media) throws CSSException {
		System.out.println("[MockDocumentHandler#media], media=" + media);

	}

	public void endPage(String name, String pseudo_page) throws CSSException {
		System.out.println("[MockDocumentHandler#endPage], name=" + name
				+ ", pseudo_page=" + pseudo_page);

	}

	public void endSelector(SelectorList selectors) throws CSSException {
		int length = selectors.getLength();
		System.out.println("[MockDocumentHandler#endSelector], selectors="
				+ selectors + ", length=" + length);
		for (int i = 0; i < length; i++) {
			Selector selector = selectors.item(i);
			if (selector instanceof ElementSelector) {
				// Element selector
				ElementSelector elementSelector = (ElementSelector) selector;
				System.out.println("\tElementSelector=> localName="
						+ elementSelector.getLocalName());
			} else if (selector instanceof ConditionalSelector) {
				ConditionalSelector conditionalSelector = (ConditionalSelector) selector;
				System.out.println("\tConditionalSelector");
				Condition condition = conditionalSelector.getCondition();
				if (condition instanceof AttributeCondition) {
					AttributeCondition attributeCondition = (AttributeCondition) condition;
					System.out
							.println("\t\tCondition (type=AttributeCondition)=> localName="
									+ attributeCondition.getLocalName()
									+ ", value="
									+ attributeCondition.getValue());
				} else {
					System.out.println("\t\tCondition=>" + condition);
				}

			} else
				System.out.println(selector);
		}

	}

	public void ignorableAtRule(String atRule) throws CSSException {
		// TODO Auto-generated method stub

	}

	public void importStyle(String uri, SACMediaList media,
			String defaultNamespaceURI) throws CSSException {
		// TODO Auto-generated method stub

	}

	public void namespaceDeclaration(String prefix, String uri)
			throws CSSException {
		// TODO Auto-generated method stub

	}

	public void property(String name, LexicalUnit value, boolean important)
			throws CSSException {
		System.out.println("[MockDocumentHandler#property], name=" + name
				+ ", value=" + value);

	}

	public void startDocument(InputSource source) throws CSSException {
		// TODO Auto-generated method stub

	}

	public void startFontFace() throws CSSException {
		// TODO Auto-generated method stub

	}

	public void startMedia(SACMediaList media) throws CSSException {
		// TODO Auto-generated method stub

	}

	public void startPage(String name, String pseudo_page) throws CSSException {
		// TODO Auto-generated method stub

	}

	public void startSelector(SelectorList selectors) throws CSSException {
		// TODO Auto-generated method stub

	}

}
