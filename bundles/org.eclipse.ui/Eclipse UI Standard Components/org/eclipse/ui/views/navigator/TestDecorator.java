package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.*;
import org.eclipse.core.resources.*;

/**
 * Note: For experimental use only.
 * Shows how to write a decorator.  This one shows the size of an IFile as a decoration.
 * This also fires an event when the file contents changes, causing the label to be updated.
 * Note that WorkbenchContentProvider (used by the ResourceNavigator) does not normally update
 * elements when their contents changes.
 */
public class TestDecorator extends LabelProvider implements ILabelDecorator, IResourceChangeListener {
public TestDecorator() {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	workspace.addResourceChangeListener(this);
}
public Image decorateImage(Image input, Object element) {
	return null;
}
public String decorateText(String input, Object element) {
	if (element instanceof IFile) {
		long size = ((IFile) element).getLocation().toFile().length();
		return input + " (" + size + " bytes)";//$NON-NLS-2$//$NON-NLS-1$
	}
	else {
		return input;
	}
}
public void dispose() {
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	workspace.removeResourceChangeListener(this);
}
void processDelta(IResourceDelta delta) {
	if (delta.getKind() == IResourceDelta.CHANGED && delta.getResource() instanceof IFile && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, delta.getResource()));
	}
	IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.CHANGED);
	for (int i = 0; i < children.length; ++i) {
		processDelta(children[i]);
	}
}
public void resourceChanged(IResourceChangeEvent event) {
	processDelta(event.getDelta());
}
}
