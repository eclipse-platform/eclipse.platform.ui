package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.model.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Content outline page for the readme editor.
 */
public class ReadmeContentOutlinePage extends ContentOutlinePage {
	protected IFile input;
/**
 * Creates a new ReadmeContentOutlinePage.
 */
public ReadmeContentOutlinePage(IFile input) {
	super();
	this.input = input;
}
/* (non-Javadoc)
 * Method declared on ContentOutlinePage
 */
public void createControl(Composite parent) {
	super.createControl(parent);

	WorkbenchHelp.setHelp(getControl(), new String[] {IReadmeConstants.CONTENT_OUTLINE_PAGE_CONTEXT});

	TreeViewer viewer = getTreeViewer();
	viewer.setContentProvider(new WorkbenchContentProvider());
	viewer.setLabelProvider(new WorkbenchLabelProvider());
	viewer.setInput(getContentOutline(input));
	initDragAndDrop();
}
/**
 * Gets the content outline for a given input element.
 * Returns the outline (a list of MarkElements), or null
 * if the outline could not be generated.
 */
private IAdaptable getContentOutline(IAdaptable input) {
	return ReadmeModelFactory.getInstance().getContentOutline(input);
}
/**
 * Initializes drag and drop for this content outline page.
 */
private void initDragAndDrop() {
	int ops = DND.DROP_COPY | DND.DROP_MOVE;
	Transfer[] transfers = new Transfer[] {TextTransfer.getInstance(), PluginTransfer.getInstance()};
	getTreeViewer().addDragSupport(ops, transfers, new ReadmeContentOutlineDragListener(this));
}
/**
 * Forces the page to update its contents.
 *
 * @see ReadmeEditor#doSave(IProgressMonitor)
 */
public void update() {
	getControl().setRedraw(false);
	getTreeViewer().setInput(getContentOutline(input));
	getTreeViewer().expandAll();
	getControl().setRedraw(true);
}
}
