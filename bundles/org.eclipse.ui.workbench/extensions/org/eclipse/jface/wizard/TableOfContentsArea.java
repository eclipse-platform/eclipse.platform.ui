package org.eclipse.jface.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * The WizardTableOfContents is a class that displays a series
 * of widgets depicting the current state of pages within a wizard.
 */
class TableOfContentsArea {

	private Canvas canvas;
	private ITableOfContentsNode[] nodes = new ITableOfContentsNode[0];
	private IWizard initialWizard;
	private ITableOfContentsNode currentNode;

	static final int NODE_SIZE = 20;

	public TableOfContentsArea() {
		super();
	}

	/**
	 * Add the nodes for the current wizard. This should be called whenever
	 * the wizard is updated for a dialog using the WizardTableOfContentsHeader.
	 */
	private void addNodesForWizard(IWizard wizard) {

		ITableOfContentsNode[] newNodes;

		if (wizard instanceof ITableOfContentsWizard) {
			newNodes = ((ITableOfContentsWizard) wizard).getInitialNodes();
		} else {
			IWizardPage[] pages = wizard.getPages();
			newNodes = new TableOfContentsNode[1];
			newNodes[0] = new TableOfContentsNode(pages[0]);
		}

		addNodes(newNodes);

	}
	/**
	 * Add the newNodes to the collection of nodes being displayed.
	 */

	private void addNodes(ITableOfContentsNode[] newNodes) {

		int oldSize = nodes.length;
		ITableOfContentsNode[] mergeNodes =
			new ITableOfContentsNode[oldSize + newNodes.length];
		System.arraycopy(nodes, 0, mergeNodes, 0, oldSize);
		System.arraycopy(newNodes, 0, mergeNodes, oldSize, newNodes.length);

		nodes = mergeNodes;
		if (canvas != null)
			canvas.redraw();
	}

	/**
	* Add the nodes for newWizard. If we haven't created anything
	* set cache the wizard.
	*/
	public void addWizard(IWizard newWizard) {
		//Add nodes if the table is already created
		if (canvas == null)
			initialWizard = newWizard;
		else {
			if (currentNode != null) {
				int currentIndex = indexOfPage(currentNode.getPage());

				if (nodes.length > currentIndex) {
					ITableOfContentsNode nextNode = nodes[currentIndex];

					//Replace the next pages if required
					if (nextNode.getPage().getWizard().equals(newWizard))
						return;
					else {
						for (int i = currentIndex + 1; i < nodes.length; i++) {
							nodes[i].dispose();
						}
						ITableOfContentsNode[] newNodes =
							new ITableOfContentsNode[currentIndex + 1];
						for (int i = 0; i <= currentIndex; i++) {
							newNodes[i] = nodes[i];
						}

						nodes = newNodes;
					}
				}
			}
			addNodesForWizard(newWizard);
		}
	}

	/**
	 * Select the node corresponding to the page.
	 * @param IWorkbenchPage
	 */
	void updateFor(IWizardPage page) {

		//We may not have created anything yet
		int index = indexOfPage(page);
		if (index == -1) {

			ITableOfContentsNode[] newNodes = new ITableOfContentsNode[1];
			ITableOfContentsNode newNode = new TableOfContentsNode(page);
			newNodes[0] = newNode;
			currentNode = newNode;
			addNodes(newNodes);

		} else {
			currentNode = nodes[index];
			ITableOfContentsNode checkNode = currentNode;
			for (int i = index + 1; i < nodes.length; i++) {
				nodes[i].setEnabled(checkNode.getPage().canFlipToNextPage());
				checkNode = nodes[i];
			}
		}
	}

	/**
	 * Return the index of the node of the page.
	 * @param IWorkbenchPage
	 */
	private int indexOfPage(IWizardPage page) {

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getPage().equals(page))
				return i;
		}
		return -1;

	}

	/**
	 * Create the table of contents canvas area.
	 */
	public Control createControl(Composite parent) {

		canvas = new Canvas(parent, SWT.BORDER);

		canvas.addPaintListener(new PaintListener() {
			/**
			 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
			 */
			public void paintControl(PaintEvent event) {
				drawNodes(event.gc);
			}
		});

		canvas.addMouseListener(new MouseListener() {
			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent event) {

				int index = event.x / NODE_SIZE;
				if (index < nodes.length) {
					ITableOfContentsNode selectedNode = nodes[index];
					IWizardPage page = selectedNode.getPage();
					page.getWizard().getContainer().showPage(page		);
				}
			}
			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
			}
			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent e) {
			}
		});

		//Create the nodes if the wizard has not been set.
		if (initialWizard != null) {
			addNodesForWizard(initialWizard);
			currentNode = nodes[0];
		}

		return canvas;

	}

	/**
	 * Draw the nodes for the receiver on the supplied gc.
	 */
	private void drawNodes(GC gc) {
		Point size = canvas.getSize();
		gc.drawLine(0, size.y / 2, size.x, size.y / 2);

		for (int i = 0; i < nodes.length; i++) {
			Image image = nodes[i].getImage();
			Rectangle imageSize = image.getBounds();
			int xOffset = (NODE_SIZE - imageSize.width) / 2;
			int yOffset = (NODE_SIZE - imageSize.height) / 2;
			gc.drawImage(image, i * NODE_SIZE + xOffset, yOffset);
		}
	}
}
