/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

/**
 * Filter for the servlet output
 */
public interface IFilter
{
	byte[] filter(byte[] input);
}
