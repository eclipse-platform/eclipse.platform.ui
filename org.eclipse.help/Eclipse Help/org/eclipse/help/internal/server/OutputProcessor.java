package org.eclipse.help.internal.server;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.lang.*;

/**
 * Pre-processes the content of a help url before sending it to client
 */
public interface OutputProcessor {
	byte[] processOutput(byte[] input);
}
