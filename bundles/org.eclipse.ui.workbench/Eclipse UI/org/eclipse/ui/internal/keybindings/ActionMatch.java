package org.eclipse.ui.internal.keybindings;

public final class ActionMatch {

	public static ActionMatch create(Action action, int match)
		throws IllegalArgumentException {
		return new ActionMatch(action, match);
	}
	
	private Action action;
	private int match;

	private ActionMatch(Action action, int match)
		throws IllegalArgumentException {
		if (action == null || match < 0)
			throw new IllegalArgumentException();
			
		this.action = action;
		this.match = match;
	}

	public Action getAction() {
		return action;
	}
	
	public int getMatch() {
		return match;	
	}	
}
