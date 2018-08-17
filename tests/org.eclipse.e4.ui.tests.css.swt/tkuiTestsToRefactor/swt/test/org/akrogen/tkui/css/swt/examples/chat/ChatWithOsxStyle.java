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
package org.akrogen.tkui.css.swt.examples.chat;

import org.akrogen.tkui.css.swt.resources.CSSSWTResources;

public class ChatWithOsxStyle extends AbstractChatExample {

	public ChatWithOsxStyle() {
		super(CSSSWTResources.getSWTOsx());
	}

	public static void main(String[] args) {
		ChatWithOsxStyle testOsx = new ChatWithOsxStyle();
		try {
			testOsx.display();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
