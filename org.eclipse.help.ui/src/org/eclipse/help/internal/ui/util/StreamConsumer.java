package org.eclipse.help.internal.ui.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;

/**
 * Used to destroy output from processes
 */
public class StreamConsumer extends Thread {
	InputStream is;
	byte[] buf;
	public StreamConsumer(InputStream inputStream) {
		super();
		this.is = inputStream;
		buf = new byte[512];
	}
	public void run() {
		try {
			int n = 0;
			while (n >= 0)
				n = is.read(buf);
		} catch (IOException ioe) {
		}
	}
}
