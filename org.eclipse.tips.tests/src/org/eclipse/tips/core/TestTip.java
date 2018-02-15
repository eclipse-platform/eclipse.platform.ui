/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.core;

import java.util.Date;

import org.eclipse.tips.ui.internal.util.DateUtil;

public class TestTip extends Tip implements IHtmlTip {

	private String fSubject;
	private String fHTML;

	public TestTip(String providerId, String html, String subject) {
		super(providerId);
		fHTML = html;
		fSubject = subject;
	}

	@Override
	public Date getCreationDate() {
		return DateUtil.getDateFromYYMMDD("31/12/1964");
	}

	@Override
	public String getHTML() {
		return fHTML;
	}

	@Override
	public String getSubject() {
		return fSubject;
	}

	@Override
	public TipImage getImage() {
		return null;
	}
}
