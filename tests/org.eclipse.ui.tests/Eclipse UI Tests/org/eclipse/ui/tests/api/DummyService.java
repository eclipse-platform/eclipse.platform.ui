/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import org.eclipse.ui.internal.services.INestable;

/**
 * 
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class DummyService implements INestable {

	private boolean active;


	public boolean isActive() {
		return active;
	}
	
	@Override
	public void activate() {
		active = true;
	}

	
	@Override
	public void deactivate() {
		active = false;
	}

}
