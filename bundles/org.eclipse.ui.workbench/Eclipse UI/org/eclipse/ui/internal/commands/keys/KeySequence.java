package org.eclipse.ui.internal.commands.keys;

public class KeySequence {

	private KeyStroke[] keyStrokes;
	
	public KeySequence(KeyStroke[] keyStrokes) {
		super();
		this.keyStrokes = keyStrokes;
	}

	public KeyStroke[] getKeyStrokes() {
		return (KeyStroke[]) keyStrokes.clone();
	}
}
