package org.eclipse.ui.internal.commands.gestures;

public class GestureSequence {

	private GestureStroke[] gestureStrokes;
	
	public GestureSequence(GestureStroke[] gestureStrokes) {
		super();
		this.gestureStrokes = gestureStrokes;
	}

	public GestureStroke[] getGestureStrokes() {
		return (GestureStroke[]) gestureStrokes.clone();
	}
}
