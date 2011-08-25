/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.examples.chat;

import org.akrogen.tkui.css.swt.resources.CSSSWTResources;


public class ChatWithMatrixStyle extends AbstractChatExample {

	public ChatWithMatrixStyle() {
		super(CSSSWTResources.getSWTMatrix());
	}

	public static void main(String[] args) {
		ChatWithMatrixStyle testMatrix = new ChatWithMatrixStyle();
		try {
			testMatrix.display();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
