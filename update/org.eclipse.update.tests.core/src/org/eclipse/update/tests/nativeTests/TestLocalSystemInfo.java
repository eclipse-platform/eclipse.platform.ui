/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.nativeTests;
import java.io.File;

import org.eclipse.update.configuration.IVolume;
import org.eclipse.update.configuration.LocalSystemInfo;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestLocalSystemInfo extends UpdateManagerTestCase {

	/**
	 * Test the natives
	 */
	public TestLocalSystemInfo(String arg0) {
		super(arg0);
	}

	public void testNative() throws Exception {
	
		// mount point
       IVolume[] a = LocalSystemInfo.getVolumes();
        if (a==null) throw new Exception("cannot find native library");
		System.out.println("Found "+a.length+" mount points.");
		for (int i =0; i<a.length;i++){
			System.out.print("#"+i+" - "+a[i]);
			
			File root = a[i].getFile();
			String label = a[i].getLabel();
			if (label==null) label="NO LABEL";
			int type = a[i].getType();
			long size = LocalSystemInfo.getFreeSpace(root);
			System.out.println(" ->:"+label+":"+getType(type)+":"+getSize(size));
		}
	}
	
	private String getType(int type){
		switch (type) {
			case LocalSystemInfo.VOLUME_FIXED :
				return "VOLUME FIXED";
			case LocalSystemInfo.VOLUME_REMOTE :
				return "VOLUME REMOTE";
			case LocalSystemInfo.VOLUME_REMOVABLE :
				return "VOLUME REMOVABLE";
			case LocalSystemInfo.VOLUME_CDROM :
				return "VOLUME CDROM";				
			case LocalSystemInfo.VOLUME_UNKNOWN :
				return "VOLUME UNKNOWN";
			default :
				return "WRONG VOLUME INFO";
		}
	}
	
	private String getSize(long size){
		if (size==LocalSystemInfo.SIZE_UNKNOWN) return "UNKNOWN SIZE";
		
		long kb = size/(1024*1024);
		return new String(size+" ("+kb+"MB)");
	}
}
