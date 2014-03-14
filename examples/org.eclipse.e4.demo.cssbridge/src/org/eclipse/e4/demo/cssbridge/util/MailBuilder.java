/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.util;

import java.util.Date;

import org.eclipse.e4.demo.cssbridge.model.Importance;
import org.eclipse.e4.demo.cssbridge.model.Mail;

public class MailBuilder {
	private Mail mail;

	public MailBuilder mail() {
		mail = new Mail();
		return this;
	}

	public MailBuilder withImportance(Importance importance) {
		mail.setImportance(importance);
		return this;
	}

	public MailBuilder withSender(String sender) {
		mail.setSender(sender);
		return this;
	}

	public MailBuilder withSubject(String subject) {
		mail.setSubject(subject);
		return this;
	}

	public MailBuilder withDate(Date date) {
		mail.setDate(date);
		return this;
	}

	public MailBuilder withBody(String body) {
		mail.setBody(body);
		return this;
	}

	public Mail create() {
		return mail;
	}
}
