/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import org.eclipse.help.IContext;
import org.eclipse.help.internal.webapp.servlet.ContextServlet;
import org.eclipse.ua.tests.help.other.UserContext;
import org.eclipse.ua.tests.help.other.UserTopic;

public class MockContextServlet extends ContextServlet {

	private static final String ORG_ECLIPSE_UA_TESTS_TEST = "org.eclipse.ua.tests.test";
	private static final long serialVersionUID = 3615294041471900830L;

	protected IContext getContext(String locale, String id) {
		if (id.startsWith(ORG_ECLIPSE_UA_TESTS_TEST)) {
			String suffix = id.substring(ORG_ECLIPSE_UA_TESTS_TEST.length());
			String title = "context" + suffix + '_' + locale;
			UserContext context = new UserContext(title, "Decriptiion for " + title);
			UserTopic topic = new UserTopic(title, "topic.html", true);
			context.addTopic(topic);
			return context;	
		}
		return null;
	}
	

}
