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

import junit.framework.TestCase;

import org.eclipse.help.IContext;
import org.eclipse.help.IContext3;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;

public class GetContextUsingRemoteHelp extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
        RemotePreferenceStore.savePreferences();
        mode = BaseHelpSystem.getMode();
		RemotePreferenceStore.setMockRemoteServer();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}

	public void testContextDefaultLocale() throws Exception {
		IContext context = HelpPlugin.getContextManager().getContext("org.eclipse.ua.tests.test_one", "en");
		assertNotNull(context);
		IHelpResource[] relatedTopics = context.getRelatedTopics();
		assertEquals(1, relatedTopics.length);
		String topicLabel = relatedTopics[0].getLabel();
		assertEquals("context_one_en", topicLabel);
		String title = ((IContext3)context).getTitle();
		assertEquals("context_one_en", title);
		RemotePreferenceStore.disableRemoteHelp();
		context = HelpPlugin.getContextManager().getContext("org.eclipse.ua.tests.test_one", "en");
        assertNull(context);
	}
	
	public void testLocalContextBeatsRemote() throws Exception {
		IContext context = HelpPlugin.getContextManager().getContext("org.eclipse.ua.tests.test_cheatsheets", "en");
		assertNotNull(context);
		IHelpResource[] relatedTopics = context.getRelatedTopics();
		assertEquals(1, relatedTopics.length);
		String topicLabel = relatedTopics[0].getLabel();
		assertEquals("abcdefg", topicLabel);
	}
	
	public void testContextDeLocale() throws Exception {
		IContext context = HelpPlugin.getContextManager().getContext("org.eclipse.ua.tests.test_one", "de");
		assertEquals("context_one_de", ((IContext3)context).getTitle());
	}
	
	public void testContextNotFound() throws Exception {
		IContext context = HelpPlugin.getContextManager().getContext("org.eclipse.ua.tests.no_such_ctx", "en");
        assertNull(context);
	}

}
