package org.eclipse.ui.internal.commands.keys;

public class ModifierKey extends Key {

	public final static ModifierKey ALT = new ModifierKey("ALT"); 
	public final static ModifierKey COMMAND = new ModifierKey("COMMAND"); 
	public final static ModifierKey CTRL = new ModifierKey("CTRL"); 
	public final static ModifierKey SHIFt = new ModifierKey("SHIFT"); 

	private String string;
	
	private ModifierKey(String string) {
		super();
		this.string = string;
	}

	public String toString() {
		return string;
	}
}
