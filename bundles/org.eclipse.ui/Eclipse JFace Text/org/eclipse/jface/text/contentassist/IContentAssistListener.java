package org.eclipse.jface.text.contentassist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import org.eclipse.swt.events.VerifyEvent;

import org.eclipse.jface.text.IEventConsumer;



/**
 * An interface whereby listeners can not only receive key events,
 * but can also consume them to prevent subsequent listeners from
 * processing the event.
 */
interface IContentAssistListener extends IEventConsumer {
	
	/**
	 * Verifies the key event.
	 *
	 * @return <code>true</code> if processing should be continued by additional listeners
	 * @see VerifyKeyListener#verifyKey
	 */
	public boolean verifyKey(VerifyEvent event);
}
