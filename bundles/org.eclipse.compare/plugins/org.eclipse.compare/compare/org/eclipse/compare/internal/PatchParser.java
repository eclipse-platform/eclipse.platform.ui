/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.*;

public class PatchParser {
	
	static void main(String[] args) {
		
		try {
			InputStream is= new FileInputStream("C:\\in.patch");
		} catch (FileNotFoundException ex) {
			System.out.println("patch file not found: ");
		}
		
		
	}
}

