package org.eclipse.ui.internal.commands.gestures;

public class Direction {

	public final static Direction DOWN = new Direction("DOWN"); 
	public final static Direction LEFT = new Direction("LEFT"); 
	public final static Direction RIGHT = new Direction("RIGHT"); 
	public final static Direction UP = new Direction("UP"); 

	private String string;
	
	private Direction(String string) {
		super();
		this.string = string;
	}

	public String toString() {
		return string;
	}
}
