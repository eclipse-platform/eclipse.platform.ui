package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * Pre-processes the content of a help url before sending it to client
 */
public interface OutputProcessor {
	byte[] processOutput(byte[] input);
}
