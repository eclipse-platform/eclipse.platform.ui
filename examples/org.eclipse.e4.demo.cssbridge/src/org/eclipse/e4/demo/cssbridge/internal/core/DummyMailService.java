/*******************************************************************************
 * Copyright (c) 2014, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.internal.core;

import static org.eclipse.e4.demo.cssbridge.util.DateUtils.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.demo.cssbridge.core.IMailService;
import org.eclipse.e4.demo.cssbridge.model.FolderType;
import org.eclipse.e4.demo.cssbridge.model.Importance;
import org.eclipse.e4.demo.cssbridge.model.Mail;
import org.eclipse.e4.demo.cssbridge.util.MailBuilder;
import org.osgi.service.component.annotations.Component;

@Component(service = IMailService.class)
public class DummyMailService implements IMailService {
	private static final String MAILBOX_NAME = "me@this.com";

	private Map<FolderType, List<Mail>> folderTypeToMails;

	@Override
	public List<Mail> getMails(FolderType type) {
		List<Mail> mails = getFolderTypeToMails().get(type);
		if (mails == null) {
			mails = Collections.emptyList();
		}
		return mails;
	}

	private Map<FolderType, List<Mail>> getFolderTypeToMails() {
		if (folderTypeToMails == null) {
			folderTypeToMails = new HashMap<>() {
				{
					put(FolderType.Inbox, createDummyInboxFolderMails());
					put(FolderType.Sent, createDummySentFolderMails());
				}
			};
		}
		return folderTypeToMails;
	}

	private List<Mail> createDummyInboxFolderMails() {
		return new ArrayList<>() {
			{
				MailBuilder mailBuilder = new MailBuilder();
				add(mailBuilder
						.mail()
						.withImportance(Importance.High)
						.withDate(parse("2013-02-11 10:12"))
						.withSender("admin")
						.withSubject("Power outage")
						.withBody(
								"We got the information about the power outage during the incoming weekend. Please check your UPS devices")
						.create());

				add(mailBuilder.mail().withImportance(Importance.Low)
						.withDate(parse("2014-02-12 11:50"))
						.withSender("[Spam] Lottery")
						.withSubject("Win the prize")
						.withBody("Send SMS and win one of the 1000 prizes!")
						.create());

				add(mailBuilder
						.mail()
						.withImportance(Importance.Normal)
						.withDate(parse("2014-02-12 08:01"))
						.withSender("nicole@mail.org")
						.withSubject(
								"This is a message about the cool Eclipse RCP!")
						.withBody(
								"""
									This RCP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:
									- add a top-level menu and toolbar with actions
									- add keybindings to actions
									- create views that can't be closed and
									  multiple instances of the same view
									- perspectives with placeholders for new views
									- use the default about dialog
									- create a product definition
									""")
						.create());
			}
		};
	}

	private List<Mail> createDummySentFolderMails() {
		return new ArrayList<>() {
			{
				MailBuilder mailBuilder = new MailBuilder();
				add(mailBuilder.mail().withImportance(Importance.High)
						.withDate(parse("2013-02-12 10:00")).withSender("me")
						.withSubject("Re: Power outage").withBody("Done")
						.create());

				add(mailBuilder.mail().withImportance(Importance.Normal)
						.withDate(parse("2014-02-15 11:01"))
						.withSubject("Mailing list test").withBody("test")
						.create());
			}
		};
	}

	@Override
	public String getMailboxName() {
		return MAILBOX_NAME;
	}
}
