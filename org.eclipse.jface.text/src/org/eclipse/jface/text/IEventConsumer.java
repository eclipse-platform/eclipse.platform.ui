package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.VerifyEvent;

/**
 * Implementers can register with an text viewer and 
 * receive <code>VerifyEvent</code>s before the text viewer 
 * they are registered with. If the event consumer marks events
 * as processed by turning their <code>doit</code> field to 
 * <code>false</code> the text viewer subsequently ignores them.
 * Clients may implement this interface.
 * 
 * @see ITextViewer
 * @see org.eclipse.swt.events.VerifyEvent
 */
public interface IEventConsumer {
	
	/**
	 * Processes the given event and marks it as done if it should 
	 * be ignored by subsequent receivers.
	 *
	 * @param event the verify event which will be investigated
	 */ 
	public void processEvent(VerifyEvent event);
}
