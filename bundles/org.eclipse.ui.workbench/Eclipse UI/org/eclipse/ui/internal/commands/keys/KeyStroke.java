package org.eclipse.ui.internal.commands.keys;

public class KeyStroke {

	private ModifierKey[] modifierKeys;
	private NonModifierKey nonModifierKey;
	
	public KeyStroke(ModifierKey[] modifierKeys, NonModifierKey nonModifierKey) {
		super();
		this.modifierKeys = modifierKeys;
		this.nonModifierKey = nonModifierKey;		
	}

	public ModifierKey[] getModifierKeys() {
		return (ModifierKey[]) modifierKeys.clone();
	}

	public NonModifierKey getNonModifierKey() {
		return nonModifierKey;
	}
}
