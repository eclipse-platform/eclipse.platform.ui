package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.help.topics.ITopic;

/**
 * Topic contribution
 */
public interface Topic extends Contribution/* 1.0 nav support */, ITopic/* eo 1.0 nav support */ {

	/**
	 */
	String getHref();
}
