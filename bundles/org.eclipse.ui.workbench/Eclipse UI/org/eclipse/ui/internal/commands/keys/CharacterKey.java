package org.eclipse.ui.internal.commands.keys;

public class CharacterKey extends NonModifierKey {

	private char character;
	
	CharacterKey(char character) {
		super();
		this.character = character;
	}

	public char getCharacter() {
		return character;
	}

	public String toString() {
		return Character.toString(character);
	}
}
