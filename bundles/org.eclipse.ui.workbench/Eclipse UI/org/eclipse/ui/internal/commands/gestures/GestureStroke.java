package org.eclipse.ui.internal.commands.gestures;

public class GestureStroke {

	private Direction direction;
	
	public GestureStroke(Direction direction) {
		super();
		this.direction = direction;
	}

	public Direction getDirection() {
		return direction;
	}
}
