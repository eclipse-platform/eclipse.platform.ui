package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;

/**
 * A fake view action set.
 */
public class FakePerspectiveActionSet extends FakeActionSetDescriptor {
/**
 * Constructs a new action set.
 */
public FakePerspectiveActionSet(IPerspectiveDescriptor desc) {
	super(desc.getId(), desc);
}
/**
 * Returns the action image descriptor.
 */
protected ImageDescriptor getActionImageDescriptor() {
	return getPerspective().getImageDescriptor();
}
/**
 * Returns the action text.
 */
protected String getActionLabel() {
	return getPerspective().getLabel();
}
/**
 * Returns the descriptor
 */
public IPerspectiveDescriptor getPerspective() {
	return (IPerspectiveDescriptor)getData();
}
}
