/*******************************************************************************
 *  Copyright (c) 2008, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

/**
 * Test for function which determines whether a topic path can be passed to the content frame
 */

public class RestrictedTopicParameter extends TestCase {
	
	private static final String RESTRICT_TOPIC = "restrictTopicParameter";
	private boolean restrictTopic;
	private int helpMode;
	
	protected void setUp() throws Exception {
		restrictTopic = Platform.getPreferencesService().getBoolean
	     (HelpBasePlugin.PLUGIN_ID, RESTRICT_TOPIC,
			      false, null);
		helpMode = BaseHelpSystem.getMode();
	}
	
	protected void tearDown() throws Exception {
		setRestrictTopic(restrictTopic);
		BaseHelpSystem.setMode(helpMode);
	}

	private void setRestrictTopic(boolean isRestrict) {
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(HelpBasePlugin.PLUGIN_ID);
		pref.putBoolean(RESTRICT_TOPIC, isRestrict);		
	}

	public void testWorkbenchMode() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_WORKBENCH);
		setRestrictTopic(true);
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
		setRestrictTopic(false);
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
	}
	
	public void testStandaloneMode() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_STANDALONE);
		setRestrictTopic(true);
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
		setRestrictTopic(false);
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
	}

	public void testInfocenterUnrestricted() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		setRestrictTopic(false);
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("org.eclipse.platform.doc.user/reference/ref-43.htm"));
	}
	
	public void testInfocenterResestricted() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		setRestrictTopic(true);
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("http://www.eclipse.org"));
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("https://www.eclipse.org"));
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("HTTP://www.eclipse.org"));
		assertFalse(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("file://somepath.html"));
		assertTrue(UrlUtil.isValidTopicParamOrWasOpenedFromHelpDisplay("org.eclipse.platform.doc.user/reference/ref-43.htm"));
	}
	
}
