/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

final class ActionMatch {

	static ActionMatch create(Identifier action, int match)
		throws IllegalArgumentException {
		return new ActionMatch(action, match);
	}
	
	private Identifier action;
	private int match;

	private ActionMatch(Identifier action, int match)
		throws IllegalArgumentException {
		if (action == null || match < 0)
			throw new IllegalArgumentException();
			
		this.action = action;
		this.match = match;
	}

	Identifier getAction() {
		return action;
	}
	
	int getMatch() {
		return match;	
	}	
}
