package org.eclipse.jface.wizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The WizardTableOfContents is a class that displays a series
 * of widgets depicting the current state of pages within a wizard.
 */
class TableOfContentsArea {

	private Canvas canvas;
	private ITableOfContentsNode[] nodes = new ITableOfContentsNode[0];
	private IWizard initialWizard;
	private ITableOfContentsNode currentNode;
	/**
	 * Keys for support images.
	 */
	private static final String BREAK_ENABLED = "break_enabled";
	private static final String BREAK_DISABLED = "break_disabled";
	private static final String FINISH_NOT_PRESSED = "finish_not_pressed";
	private static final String FINISH_PRESSED = "finish_pressed";
	private static final String FINISH_DISABLED = "finish_disabled";
	private static final String START = "start";

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		URL installURL =
			WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();

		try {
			installURL = new URL(installURL, "icons/full/");

			reg.put(BREAK_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/break_toc.gif"))); //$NON-NLS-1$
			reg.put(FINISH_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/finish_toc.gif"))); //$NON-NLS-1$

			reg.put(BREAK_ENABLED, ImageDescriptor.createFromURL(new URL(installURL, "etoc/break_toc.gif"))); //$NON-NLS-1$
			reg.put(FINISH_NOT_PRESSED, ImageDescriptor.createFromURL(new URL(installURL, "etoc/finish_toc.gif"))); //$NON-NLS-1$

			reg.put(FINISH_PRESSED, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/finish_toc.gif"))); //$NON-NLS-1$
			reg.put(START, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/start_toc.gif"))); //$NON-NLS-1$

		} catch (MalformedURLException exception) {
			IStatus errorStatus =
				new Status(
					IStatus.ERROR,
					WorkbenchPlugin
						.getDefault()
						.getDescriptor()
						.getUniqueIdentifier(),
					0,
					JFaceResources.getString("Problem_Occurred"),
				//$NON-NLS-1$
	exception);
			WorkbenchPlugin.getDefault().getLog().log(errorStatus);
		}

	}

	public TableOfContentsArea() {
		super();
	}

	/**
	 * Add the nodes for the current wizard. This should be called whenever
	 * the wizard is updated for a dialog using the WizardTableOfContentsHeader.
	 */
	private void addNodesForWizard(IWizard wizard) {

		ITableOfContentsNode[] newNodes;
		boolean addUnknown = false;

		if (wizard instanceof ITableOfContentsWizard) {
			newNodes = ((ITableOfContentsWizard) wizard).getInitialNodes();
		} else {
			IWizardPage[] pages = wizard.getPages();
			int nodesLength = pages.length;
			if (pages[nodesLength - 1] instanceof IDecisionPage) {
				nodesLength++; //Leave room for an unknown page
				addUnknown = true;
			}

			newNodes = new TableOfContentsNode[nodesLength];
			for (int i = 0; i < pages.length; i++) {
				newNodes[i] = new TableOfContentsNode(pages[i]);
			}
		}

		if (addUnknown)
			newNodes[newNodes.length - 1] = new TableOfContentsNode(null);

		addNodes(newNodes);

	}
	/**
	 * Add the newNodes to the collection of nodes being displayed.
	 */

	private void addNodes(ITableOfContentsNode[] newNodes) {

		int oldSize = getOldNodesLength();
		ITableOfContentsNode[] mergeNodes =
			new ITableOfContentsNode[oldSize + newNodes.length];
		System.arraycopy(nodes, 0, mergeNodes, 0, oldSize);
		System.arraycopy(newNodes, 0, mergeNodes, oldSize, newNodes.length);

		nodes = mergeNodes;
		if (canvas != null)
			canvas.redraw();
	}

	/**
	 * Return the highest index of the existing nodes that 
	 * has a non-null page;
	 */
	private int getOldNodesLength() {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getPage() == null)
				return i;
		}
		return nodes.length;
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
				int currentIndex = indexOfPage(newWizard.getStartingPage());

				if (currentIndex >= 0) {
					ITableOfContentsNode[] newNodes =
						new ITableOfContentsNode[currentIndex];
					System.arraycopy(nodes, 0, newNodes, 0, currentIndex);
					nodes = newNodes;
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
			ITableOfContentsNode newNode = new TableOfContentsNode(page);
			ITableOfContentsNode[] newNodes;
			if (page instanceof IDecisionPage && page.getNextPage() == null) {
				//Unknown next page
				newNodes = new ITableOfContentsNode[2];
				newNodes[1] = new TableOfContentsNode(null);
			} else {
				newNodes = new ITableOfContentsNode[1];
			}

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
			if (canvas != null)
				canvas.redraw();
		}
	}

	/**
	 * Return the index of the node of the page.
	 * @param IWorkbenchPage
	 */
	private int indexOfPage(IWizardPage page) {
		if (page != null) {
			for (int i = 0; i < nodes.length; i++) {
				IWizardPage nextPage = nodes[i].getPage();
				if (page.equals(nextPage))
					return i;
			}
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

				int nodeSize = getNodeSize();
				//Take into account that the first entry is the start icon
				int index = ((event.x - getNodeOffset()) / nodeSize);
				if (index >= 0 && index < nodes.length) {
					ITableOfContentsNode selectedNode = nodes[index];
					IWizardPage page = selectedNode.getPage();
					if (page != null)
						page.getWizard().getContainer().showPage(page);
				}
			}
			/**
																									 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
																									 */
			public void mouseDoubleClick(MouseEvent e) {
			} /**
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
	} /**
														 * Draw the nodes for the receiver on the supplied gc.
														 */
	private void drawNodes(GC gc) {
		Point size = canvas.getSize();
		int nodeSize = getNodeSize();
		int lineY = nodeSize / 2;

		Image startImage = JFaceResources.getImage(START);
		Rectangle imageSize = startImage.getBounds();
		int xOffset = (nodeSize - imageSize.width) / 2;
		int yOffset = (nodeSize - imageSize.height) / 2;
		gc.drawImage(startImage, xOffset, yOffset);
		
		//Get an offset for centering the nodes.
		int nodeOffset = getNodeOffset();
		
		int xEnd = xOffset + imageSize.width;

		boolean past = true;
		for (int i = 0; i < nodes.length; i++) {
			int positionConstant = ITableOfContentsNode.FUTURE_NODE;
			if (past) {
				if (nodes[i] == currentNode) {
					positionConstant = ITableOfContentsNode.CURRENT_NODE;
					past = false;
				} else
					positionConstant = ITableOfContentsNode.PAST_NODE;
			}

			Image image = nodes[i].getImage(positionConstant);
			Rectangle imageBounds = image.getBounds();
			xOffset = (nodeSize - imageBounds.width) / 2;
			yOffset = (nodeSize - imageBounds.height) / 2;
			//Draw the preceeding line
			int lineEnd = (i * nodeSize) + xOffset + nodeOffset;
			gc.drawLine(xEnd, lineY, lineEnd, lineY);
			gc.drawImage(image, lineEnd, yOffset);
			xEnd = lineEnd + imageBounds.width;
		}

		Image endImage;
		if (currentNode.getPage().getWizard().canFinish())
			endImage = JFaceResources.getImage(FINISH_NOT_PRESSED);
		else
			endImage = JFaceResources.getImage(FINISH_DISABLED);

		yOffset = (nodeSize - endImage.getBounds().height) / 2;
		int stopX = size.x - nodeSize;
		gc.drawLine(xEnd, lineY, stopX, lineY);
		gc.drawImage(endImage, stopX, yOffset);
	}

	/**
	 * Return the size between nodes in the canvas.
	 * @return int
	 */
	private int getNodeSize() {

		return canvas.getSize().y;
	}

	/**
	 * Get the offset required to center the nodes. 
 	 * @return int
	 */
	private int getNodeOffset() {

		int nodesSpace = getNodeSize() * nodes.length;
		return (canvas.getSize().x  - nodesSpace) / 2;
	}
}
