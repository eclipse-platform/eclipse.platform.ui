package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
