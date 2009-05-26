/*******************************************************************************
 *  Copyright (c) 2007, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.dynamic.DocumentReader;
import org.eclipse.help.internal.util.ResourceLocator;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.xml.sax.SAXException;

public class DocumentReaderTest extends TestCase {
	private final static int RUNNING = 0;
	private final static int SUCCESS = 1;
	private final static int FAILURE = 2;

	public static Test suite() {
		return new TestSuite(DocumentReaderTest.class);
	}

	private void readFile(DocumentReader docReader, String file) throws IOException, SAXException, ParserConfigurationException {
		 String pluginId = UserAssistanceTestPlugin.getPluginId();
		 String locale = "en";
	     InputStream in  = ResourceLocator.openFromPlugin(pluginId, file, locale);
		 docReader.read(in);
	}

	public void testDocumentReader() throws IOException, SAXException, ParserConfigurationException {
        DocumentReader docReader = new DocumentReader();
		ResourceReader[] resReaders = new ResourceReader[3];
		resReaders[0] = new ResourceReader(docReader,"data/help/index/assembler/a.xml");
		resReaders[1] = new ResourceReader(docReader,"data/help/index/assembler/b.xml");
		resReaders[2] = new ResourceReader(docReader,"data/help/index/assembler/c.xml");
		for (int i = 0; i < 3; i++) {
			resReaders[i].start();
		}
		int count = 0;
		for (int i = 0 ; i < 3; i++) {
			while (resReaders[i].status == RUNNING && count < 50) {
				try {
					Thread.sleep(100);
					count++;
				} catch (InterruptedException e) { 
				}
			}
		}
		assertTrue(resReaders[0].status == SUCCESS);
		assertTrue(resReaders[1].status == SUCCESS);
		assertTrue(resReaders[2].status == SUCCESS);
	}
	
	private class ResourceReader extends Thread {
		
		private DocumentReader reader;
		private String file;
		ResourceReader(DocumentReader reader, String file) {
			this.reader = reader;
			this.file = file;
		}
		public int status = RUNNING;
		
		public void run() {
			try {
				for (int i = 0; i < 10; i++) {
				    readFile(reader, file);
				}
				status = SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
				status = FAILURE;
			}
		}
	}

}
