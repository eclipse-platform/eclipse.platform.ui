package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * A fake view action set.
 */
public class FakeViewActionSet extends FakeActionSetDescriptor {
/**
 * Constructs a new action set.
 */
public FakeViewActionSet(IViewDescriptor desc) {
	super(desc.getID(), desc);
}
/**
 * Returns the action image descriptor.
 */
protected ImageDescriptor getActionImageDescriptor() {
	return getView().getImageDescriptor();
}
/**
 * Returns the action text.
 */
protected String getActionLabel() {
	return getView().getLabel();
}
/**
 * Returns the descriptor
 */
public IViewDescriptor getView() {
	return (IViewDescriptor)getData();
}
}
