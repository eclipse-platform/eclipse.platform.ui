package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;

public class FileUtil {
		
	public static void deepDelete(File resource) {
		if (resource.isDirectory()) {
			File[] fileList = resource.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				deepDelete(fileList[i]);
			}
		}
		resource.delete();
	}

	public static void transfer(InputStream in, OutputStream out) throws IOException {
		final byte[] BUFFER = new byte[4096];
		int read = 0;
		long totalRead = 0;
		synchronized (BUFFER) {
			while ((read = in.read(BUFFER)) != -1) {
				out.write(BUFFER, 0, read);
			}
		}
		out.flush();
	}
}