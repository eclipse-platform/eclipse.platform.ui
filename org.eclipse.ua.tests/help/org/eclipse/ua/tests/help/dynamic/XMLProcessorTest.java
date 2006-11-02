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
package org.eclipse.ua.tests.help.dynamic;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.FilterHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.XMLProcessor;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.FileUtil;
import org.eclipse.ua.tests.util.ResourceFinder;
import org.eclipse.ua.tests.util.XMLUtil;
import org.osgi.framework.Bundle;

public class XMLProcessorTest extends TestCase {

	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(XMLProcessorTest.class);
	}
	
	public void testXMLProcessor() throws Exception {
		NodeHandler[] handlers = new NodeHandler[] {
				new IncludeHandler(Platform.getNL()),
				new ExtensionHandler(Platform.getNL()),
				new FilterHandler(),
		};
		XMLProcessor processor = new XMLProcessor(handlers);
		Bundle bundle = UserAssistanceTestPlugin.getDefault().getBundle();
		String[] paths = ResourceFinder.findFiles(bundle, "data/help/dynamic", ".xml");
		for (int i=0;i<paths.length;++i) {
			InputStream in = bundle.getEntry(FileUtil.getResultFile(paths[i])).openStream();
			InputStream in2 = processor.process(bundle.getEntry(paths[i]).openStream(), '/' + bundle.getSymbolicName() + '/' + paths[i]);
			XMLUtil.assertXMLEquals("XML content was not processed correctly: " + paths[i], in, in2);
		}
	}
}
