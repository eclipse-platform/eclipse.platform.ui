/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
package org.eclipse.help.servlet;

import java.util.*;

/**
 * A basic filter helper that inserts data into the header section
 */
public class HeadFilterHelper 
{	
	private static final byte[] headTagBegin = "<head".getBytes();
	private static final byte[] headTagBeginCaps = "<HEAD".getBytes();
	private static final char headTagEnd = '>';
	
	public static byte[] filter(byte[] input, byte[] insertData)
	{
		// Create new buffer
		byte[] buffer = new byte[input.length + insertData.length];
		int bufPointer = 0;
		int inputPointer = 0;
		boolean foundHeadTagBegin = false;
		boolean foundHeadTagEnd = false;
		while (inputPointer < input.length) {
			// copy character
			buffer[bufPointer++] = input[inputPointer++];
			// look for head tag copied
			if (!foundHeadTagEnd
				&& !foundHeadTagBegin
				&& (bufPointer >= headTagBegin.length)) {
				for (int i = 0; i < headTagBegin.length; i++) {
					if ((buffer[bufPointer - headTagBegin.length + i] != headTagBegin[i])
						&& (buffer[bufPointer - headTagBegin.length + i] != headTagBeginCaps[i])) {
						break;
					}
					if (i == headTagBegin.length - 1)
						foundHeadTagBegin = true;
				}
			}
			if (!foundHeadTagEnd && foundHeadTagBegin && buffer[bufPointer - 1] == '>') {
				foundHeadTagEnd = true;
				//embed Script
				System.arraycopy(insertData, 0, buffer, bufPointer, insertData.length);
				bufPointer += insertData.length;
				// copy rest
				System.arraycopy(
					input,
					inputPointer,
					buffer,
					bufPointer,
					input.length - inputPointer);
				return buffer;
			}
		}
		return buffer;
	}
}
