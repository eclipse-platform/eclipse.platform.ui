package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class TreeViewerFrameSource implements IFrameSource {
	protected AbstractTreeViewer viewer;
public TreeViewerFrameSource(AbstractTreeViewer viewer) {
	this.viewer = viewer;
}
/**
 * Connects this source as a listener on the frame list,
 * so that when the current frame changes, the viewer is updated.
 */
public void connectTo(FrameList frameList) {
	frameList.addPropertyChangeListener(new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			TreeViewerFrameSource.this.handlePropertyChange(event);
		}
	});
}
protected TreeFrame createFrame(Object input) {
	return new TreeFrame(viewer, input);
}
/**
 * Updates the viewer in response to the current frame changing.
 */
protected void frameChanged(TreeFrame frame) {
	viewer.getControl().setRedraw(false);
	viewer.setInput(frame.getInput());
	viewer.setExpandedElements(frame.getExpandedElements());
	viewer.setSelection(frame.getSelection(), true);
	viewer.getControl().setRedraw(true);
}
/**
 * Returns the current frame.
 */
protected Frame getCurrentFrame(int flags) {
	Object input = viewer.getInput();
	TreeFrame frame = createFrame(input);
	if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
		frame.setSelection(viewer.getSelection());
		frame.setExpandedElements(viewer.getExpandedElements());
	}
	return frame;
}
/* (non-Javadoc)
 * Method declared on IFrameSource.
 */
public Frame getFrame(int whichFrame, int flags) {
	switch (whichFrame) {
		case IFrameSource.CURRENT_FRAME:
			return getCurrentFrame(flags);
		case IFrameSource.PARENT_FRAME:
			return getParentFrame(flags);
		case IFrameSource.SELECTION_FRAME:
			return getSelectionFrame(flags);
		default:
			return null;
	}
}
/**
 * Returns the parent frame, or <code>null</code>.
 */
protected Frame getParentFrame(int flags) {
	Object input = viewer.getInput();
	ITreeContentProvider provider =
		(ITreeContentProvider) viewer.getContentProvider();
	Object parent = provider.getParent(input);
	if (parent == null) {
		return null;
	} else {
		TreeFrame frame = createFrame(parent);
		if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
			frame.setSelection(viewer.getSelection());
			// include current input in expanded set
			Object[] expanded = viewer.getExpandedElements();
			Object[] newExpanded = new Object[expanded.length + 1];
			System.arraycopy(expanded, 0, newExpanded, 0, expanded.length);
			newExpanded[newExpanded.length - 1] = input;
			frame.setExpandedElements(newExpanded);
		}
		return frame;
	}
}
/**
 * Returns the frame for the selection, or <code>null</code>.
 */
protected Frame getSelectionFrame(int flags) {
	IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
	if (sel.size() == 1) {
		Object o = sel.getFirstElement();
		if (viewer.isExpandable(o)) {
			TreeFrame frame = createFrame(o);
			if ((flags & IFrameSource.FULL_CONTEXT) != 0) {
				frame.setSelection(viewer.getSelection());
				frame.setExpandedElements(viewer.getExpandedElements());
			}
			return frame;
		}
	}
	return null;
}
/**
 * Handles a property change event from the frame list.
 */
protected void handlePropertyChange(PropertyChangeEvent event) {
	if (FrameList.P_CURRENT_FRAME.equals(event.getProperty())) {
		frameChanged((TreeFrame) event.getNewValue());
	}
}
}
