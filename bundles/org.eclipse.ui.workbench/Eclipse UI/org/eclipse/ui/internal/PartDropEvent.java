package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public class PartDropEvent {

	public int x;
	public int y;
	public int cursorX;
	public int cursorY;
	public int relativePosition;
	public LayoutPart dragSource;
	public LayoutPart dropTarget;
}
