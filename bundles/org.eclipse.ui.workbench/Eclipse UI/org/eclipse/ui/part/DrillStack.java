package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Stack;

/* (non-Javadoc)
 * A <code>DrillStack</code> manages a stack of DrillFrames.
 * This class is not intended for use beyond the package.
 */
/* package */ class DrillStack {
	Stack fStack = null;
/**
 * Allocates a new DrillStack.
 */
public DrillStack() {
	reset();
}
/**
 * Adds a drill frame to the stack.
 *
 * @param oRecord the new drill frame
 */
public DrillFrame add(DrillFrame oRecord) {
	fStack.push(oRecord);
	return oRecord;
}
/**
 * Returns true if backward navigation is possible.  This is only true
 * if the stack size is greater than 0.
 *
 * @return true if backward navigation is possible
 */
public boolean canGoBack() {
	return (fStack.size() > 0);
}
/**
 * Returns true if "go home" is possible.  This is only true
 * if the stack size is greater than 0.
 *
 * @return true if "go home" is possible
 */
public boolean canGoHome() {
	return (fStack.size() > 0);
}
/**
 * Navigate backwards one record.
 */
public DrillFrame goBack() {
	DrillFrame aFrame = (DrillFrame)fStack.pop();
	return aFrame;
}
/**
 * Navigate to the home record.
 */
public DrillFrame goHome() {
	DrillFrame aFrame = (DrillFrame)fStack.elementAt(0);
	reset();
	return aFrame;
}
/**
 * Clears the navigation stack.
 */
public void reset() {
	fStack = new Stack();
}
/**
 * Returns the stack size.
 *
 * @return the stack size
 */
public int size() {
	return fStack.size();
}
/**
 * Returns the top element on the stack.
 *
 * @return the top element on the stack
 */
public DrillFrame top() {
	return (DrillFrame)fStack.peek();
}
}
