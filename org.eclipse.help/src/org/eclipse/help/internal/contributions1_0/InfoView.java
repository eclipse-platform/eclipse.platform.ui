package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.help.topics.ITopics;

/**
 * View contribution
 */
public interface InfoView extends Contribution, /* 1.0 nav support */ITopics/* eo 1.0 nav support */{

	/**
	 */
	Topic getRoot();
}
