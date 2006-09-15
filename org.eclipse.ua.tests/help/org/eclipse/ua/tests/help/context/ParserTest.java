/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.context;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.context.ContextsBuilder;
import org.eclipse.help.internal.context.ContextsFile;
import org.eclipse.help.internal.context.PluginContexts;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.ua.tests.help.util.ContextModelSerializer;
import org.eclipse.ua.tests.help.util.ContextModelSerializerTest;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.osgi.framework.Bundle;

public class ParserTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(ParserTest.class);
	}
	
	/*
	 * Ensure that org.eclipse.help.ui is started. It contributes extra content
	 * filtering that is used by this test. See UIContentFilterProcessor.
	 */
	protected void setUp() throws Exception {
		HelpUIPlugin.getDefault();
	}

	public void testParser() {
		Iterator iter = ContextModelSerializerTest.getContextFiles().iterator();
		while (iter.hasNext()) {
			ContextsFile file = (ContextsFile)iter.next();
			PluginContexts contexts = new PluginContexts();
			ContextsBuilder builder = new ContextsBuilder(contexts);
			builder.build(file, Platform.getNL());
			
			Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
			String relativePath = file.getHref();
			
			try {
				String expected = FileUtil.getContents(bundle, FileUtil.getResultFile(relativePath));
				String actual = ContextModelSerializer.serialize(contexts);
				Assert.assertEquals("Serialized context help model for " + relativePath + " did not match the expected result", expected, actual);
			}
			catch(IOException e) {
				Assert.fail("An error occured while loading expected result file for context help at: " + relativePath + ": " + e);
			}
		}
	}
}
