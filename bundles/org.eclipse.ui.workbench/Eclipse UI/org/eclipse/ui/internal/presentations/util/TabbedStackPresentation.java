package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.0
 */
public final class TabbedStackPresentation extends StackPresentation {

	AbstractTabFolder tabFolder;
	TabList tabs;
	private boolean ignoreSelectionChanges = false;
	private boolean shellActive = true;
	
	public TabbedStackPresentation(IStackPresentationSite site, AbstractTabFolder folder, TabList tabs) {
		super(site);
		tabFolder = folder;
		this.tabs = tabs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#computeMinimumSize()
	 */
	public Point computeMinimumSize() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
	 */
	public void setActive(int newState) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setVisible(boolean)
	 */
	public void setVisible(boolean isVisible) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#setState(int)
	 */
	public void setState(int state) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#getControl()
	 */
	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#addPart(org.eclipse.ui.presentations.IPresentablePart, java.lang.Object)
	 */
	public void addPart(IPresentablePart newPart, Object cookie) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#removePart(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void removePart(IPresentablePart oldPart) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#selectPart(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void selectPart(IPresentablePart toSelect) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#dragOver(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
	 */
	public StackDropResult dragOver(Control currentControl, Point location) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showSystemMenu()
	 */
	public void showSystemMenu() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#showPaneMenu()
	 */
	public void showPaneMenu() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.StackPresentation#getTabList(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public Control[] getTabList(IPresentablePart part) {
		// TODO Auto-generated method stub
		return null;
	}
}
