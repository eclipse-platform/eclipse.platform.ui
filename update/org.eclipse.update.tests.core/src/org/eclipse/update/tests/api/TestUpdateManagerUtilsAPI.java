package org.eclipse.update.tests.api;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
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
		 result2 = "file://c/hello/relative1/path/";
		assertEquals("2.3",result2,result1);
		
		// 2.4
		 result1 = UpdateManagerUtils.getURL(url2,str3,default1).toExternalForm();
		 result2 = "file://c/hello/relative2/path";
		assertEquals("2.4",result2,result1);
		
		// 2.5
		 result1 = UpdateManagerUtils.getURL(url2,null,default1).toExternalForm();
		 result2 = "file://c/hello/default1/default";
		assertEquals("2.5",result2,result1);
		
		// 2.6
		 result1 = UpdateManagerUtils.getURL(url2,null,default2).toExternalForm();
		 result2 = "file://c/hello/default2/";
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
	
	
	public void testResolve() throws Exception {
		URL url1 = new URL("http://www.eclipse.org/index.html");
		URL url2 = new URL("file://eclipse.org/hello.txt");
		URL url3 = new URL("jar",null,SOURCE_FILE_SITE.toExternalForm()+"features/features2.jar!/license.txt");
		
		String str1 = "path/file.1";
		String str2 = "path/file";
		String str3 = "file";
		
		String result1=null;
		String result2=null;
		URL resultURL1 = null;
		
		//******************************************************
		//1.0
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url1);
		resultURL1.openStream();
		assertEquals("1.0","file",resultURL1.getProtocol());
		
		//1.1
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url1,str1,null);
		resultURL1.openStream();
		assertEquals("1.1 file:","file",resultURL1.getProtocol());
		assertTrue("1.1 path:",resultURL1.getFile().endsWith("path"+File.separator+"file.1"));
		
		//1.2
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url1,str2,null);
		resultURL1.openStream();
		assertEquals("1.2 file:","file",resultURL1.getProtocol());
		assertTrue("1.2 path:",resultURL1.getFile().endsWith("path"+File.separator+"file"));
		
		//1.3
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url1,str3,null);
		resultURL1.openStream();
		assertEquals("1.3 file:","file",resultURL1.getProtocol());
		assertTrue("1.3 path:",resultURL1.getFile().endsWith("file"));
		
		//******************************************************
		//2.0
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url2);
		assertEquals("2.0","file",resultURL1.getProtocol());
		assertEquals("2.0.1",url2.getFile(),resultURL1.getFile());
		
		//2.1
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url2,str1,null);
		assertEquals("2.1 file:","file",resultURL1.getProtocol());
		assertEquals("2.1.1",url2.getFile(),resultURL1.getFile());
		
		//2.2
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url2,str2,null);
		assertEquals("2.2 file:","file",resultURL1.getProtocol());
		assertEquals("2.2.1",url2.getFile(),resultURL1.getFile());
		
		//2.3
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url2,str3,null);
		assertEquals("2.3 file:","file",resultURL1.getProtocol());
		assertEquals("2.0.1",url2.getFile(),resultURL1.getFile());
		
		//******************************************************
		//3.0
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url3);
		resultURL1.openStream();
		assertEquals("3.0","file",resultURL1.getProtocol());
		
		//3.1
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url3,str1,null);
		resultURL1.openStream();
		assertEquals("3.1 file:","file",resultURL1.getProtocol());
		assertTrue("3.1 path:",resultURL1.getFile().endsWith("path"+File.separator+"file.1"));
		
		//3.2
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url3,str2,null);
		resultURL1.openStream();
		assertEquals("3.2 file:","file",resultURL1.getProtocol());
		assertTrue("3.2 path:",resultURL1.getFile().endsWith("path"+File.separator+"file"));
		
		//3.3
		resultURL1 = UpdateManagerUtils.resolveAsLocal(url3,str3,null);
		resultURL1.openStream();
		assertEquals("3.3 file:","file",resultURL1.getProtocol());
		assertTrue("3.3 path:",resultURL1.getFile().endsWith("file"));		
		
		
	}

}

