/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.api;
import java.net.URL;

import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestUpdateManagerUtilsAPI extends UpdateManagerTestCase {
	
	/**
	 * Test the getFeatures()
	 */
	public TestUpdateManagerUtilsAPI(String arg0) {
		super(arg0);
	}
	
	public void testgetURL() throws Exception {
		URL url1 = new URL("http://www.eclipse.org");
		URL url2 = new URL("file://c:/hello");
		URL url3 = new URL("file:/home/eclipse/");
		URL url4 = new URL("ftp:/host:8080/path/");
		URL url5 = new URL("jar:file:/tmp/100100!/");
		
		String default1 = "default1/default";
		String default2 = "/default2/";
		
		String str1 = "http://dev.eclipse.org";
		String str2 = "relative1/path/";
		String str3 = "/relative2/path";
		
		String result1=null;
		String result2=null;
		
		//****************************************************************************************
		// 1.0
		result1 = UpdateManagerUtils.getURL(url1,str1,default1).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("1.0",result2,result1);
		
		// 1.1
		 result1 = UpdateManagerUtils.getURL(null,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("1.1",result2,result1);
		
		// 1.2
		 result1 = UpdateManagerUtils.getURL(url1,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("1.2",result2,result1);
		
		// 1.3
		 result1 = UpdateManagerUtils.getURL(url1,str2,default1).toExternalForm();
		 result2 = "http://www.eclipse.org/relative1/path/";
		assertEquals("1.3",result2,result1);
		
		// 1.4
		 result1 = UpdateManagerUtils.getURL(url1,str3,default1).toExternalForm();
		 result2 = "http://www.eclipse.org/relative2/path";
		assertEquals("1.4",result2,result1);
		
		// 1.5
		 result1 = UpdateManagerUtils.getURL(url1,null,default1).toExternalForm();
		 result2 = "http://www.eclipse.org/default1/default";
		assertEquals("1.5",result2,result1);
		
		// 1.6
		 result1 = UpdateManagerUtils.getURL(url1,null,default2).toExternalForm();
		 result2 = "http://www.eclipse.org/default2/";
		assertEquals("1.6",result2,result1);
		
		//****************************************************************************************
		// 2.0
		 result1 = UpdateManagerUtils.getURL(url2,str1,default1).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("2.0",result2,result1);
		
		// 2.1
		 result1 = UpdateManagerUtils.getURL(null,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("2.1",result2,result1);
		
		// 2.2
		 result1 = UpdateManagerUtils.getURL(url2,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("2.2",result2,result1);
		
		// 2.3
		 result1 = UpdateManagerUtils.getURL(url2,str2,default1).toExternalForm();
		 result2 = "file://c:/relative1/path/";
		assertEquals("2.3",result2,result1);
		
		// 2.4
		 result1 = UpdateManagerUtils.getURL(url2,str3,default1).toExternalForm();
		 result2 = "file://c:/relative2/path";
		assertEquals("2.4",result2,result1);
		
		// 2.5
		 result1 = UpdateManagerUtils.getURL(url2,null,default1).toExternalForm();
		 result2 = "file://c:/default1/default";
		assertEquals("2.5",result2,result1);
		
		// 2.6
		 result1 = UpdateManagerUtils.getURL(url2,null,default2).toExternalForm();
		 result2 = "file://c:/default2/";
		assertEquals("2.6",result2,result1);
		

		//****************************************************************************************
		// 3.0
		 result1 = UpdateManagerUtils.getURL(url3,str1,default1).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("3.0",result2,result1);
		
		// 3.1
		 result1 = UpdateManagerUtils.getURL(null,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("3.1",result2,result1);
		
		// 3.2
		 result1 = UpdateManagerUtils.getURL(url3,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("3.2",result2,result1);
		
		// 3.3
		 result1 = UpdateManagerUtils.getURL(url3,str2,default1).toExternalForm();
		 result2 = "file:/home/eclipse/relative1/path/";
		assertEquals("3.3",result2,result1);
		
		// 3.4
		 result1 = UpdateManagerUtils.getURL(url3,str3,default1).toExternalForm();
		 result2 = "file:/home/eclipse/relative2/path";
		assertEquals("3.4",result2,result1);
		
		// 3.5
		 result1 = UpdateManagerUtils.getURL(url3,null,default1).toExternalForm();
		 result2 = "file:/home/eclipse/default1/default";
		assertEquals("3.5",result2,result1);
		
		// 3.6
		 result1 = UpdateManagerUtils.getURL(url3,null,default2).toExternalForm();
		 result2 = "file:/home/eclipse/default2/";
		assertEquals("3.6",result2,result1);
				
		//****************************************************************************************
		// 4.0
		 result1 = UpdateManagerUtils.getURL(url4,str1,default1).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("4.0",result2,result1);
		
		// 4.1
		 result1 = UpdateManagerUtils.getURL(null,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("4.1",result2,result1);
		
		// 4.2
		 result1 = UpdateManagerUtils.getURL(url4,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("4.2",result2,result1);
		
		// 4.3
		 result1 = UpdateManagerUtils.getURL(url4,str2,default1).toExternalForm();
		 result2 = "ftp:/host:8080/path/relative1/path/";
		assertEquals("4.3",result2,result1);
		
		// 4.4
		 result1 = UpdateManagerUtils.getURL(url4,str3,default1).toExternalForm();
		 result2 = "ftp:/host:8080/path/relative2/path";
		assertEquals("4.4",result2,result1);
		
		// 4.5
		 result1 = UpdateManagerUtils.getURL(url4,null,default1).toExternalForm();
		 result2 = "ftp:/host:8080/path/default1/default";
		assertEquals("4.5",result2,result1);
		
		// 4.6
		 result1 = UpdateManagerUtils.getURL(url4,null,default2).toExternalForm();
		 result2 = "ftp:/host:8080/path/default2/";
		assertEquals("4.6",result2,result1);
		
		//****************************************************************************************
		// 5.0
		 result1 = UpdateManagerUtils.getURL(url5,str1,default1).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("5.0",result2,result1);
		
		// 5.1
		 result1 = UpdateManagerUtils.getURL(null,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("5.1",result2,result1);
		
		// 5.2
		 result1 = UpdateManagerUtils.getURL(url5,str1,null).toExternalForm();
		 result2 = "http://dev.eclipse.org";
		assertEquals("5.2",result2,result1);
		
		// 5.3
		 result1 = UpdateManagerUtils.getURL(url5,str2,default1).toExternalForm();
		 result2 = "jar:file:/tmp/100100!/relative1/path/";
		assertEquals("5.3",result2,result1);
		
		// 5.4
		 result1 = UpdateManagerUtils.getURL(url5,str3,default1).toExternalForm();
		 result2 = "jar:file:/tmp/100100!/relative2/path";
		assertEquals("5.4",result2,result1);
		
		// 5.5
		 result1 = UpdateManagerUtils.getURL(url5,null,default1).toExternalForm();
		 result2 = "jar:file:/tmp/100100!/default1/default";
		assertEquals("5.5",result2,result1);
		
		// 5.6
		 result1 = UpdateManagerUtils.getURL(url5,null,default2).toExternalForm();
		 result2 = "jar:file:/tmp/100100!/default2/";
		assertEquals("5.6",result2,result1);				

	}
	
	

}

