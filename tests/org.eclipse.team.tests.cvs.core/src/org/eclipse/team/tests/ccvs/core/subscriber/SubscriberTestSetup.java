/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.subscriber;

import junit.framework.Test;

import org.eclipse.team.tests.ccvs.core.CVSTestSetup;

public class SubscriberTestSetup extends CVSTestSetup {

	private static SyncInfoSource syncInfoSource = new SyncInfoSource();
	
	public static SyncInfoSource getSyncInfoSource() {
		return syncInfoSource;
	}

	public SubscriberTestSetup(Test test, SyncInfoSource source) {
		super(test);
		syncInfoSource = source;
	}

}
