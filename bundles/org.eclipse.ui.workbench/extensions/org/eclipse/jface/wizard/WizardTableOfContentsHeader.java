package org.eclipse.jface.wizard;

import org.eclipse.jface.dialogs.TitleAreaDialogHeader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;



/**
 * The WizardTableOfContents is a class that displays a series
 * of widgets depicting the current state of pages within a wizard.
 */
public class WizardTableOfContentsHeader extends TitleAreaDialogHeader {

	private ScrolledComposite tableComposite;
	private Composite inner;
	private ITableOfContentsNode[] nodes = new ITableOfContentsNode[0];
	private IWizard initialWizard;
	private ITableOfContentsNode currentNode;

	public WizardTableOfContentsHeader(WizardDialog wizardDialog) {
		super(wizardDialog);
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
			newNodes = new WizardTableOfContentsNode[1];
			newNodes[0] = new WizardTableOfContentsNode(pages[0]);
		}

		Display display = wizard.getContainer().getShell().getDisplay();
		addNodes(newNodes, display);		

	}
	/**
	 * Add the newNodes to the collection of nodes being displayed.
	 */

	private void addNodes(ITableOfContentsNode[] newNodes, Display display) {

		int oldSize = nodes.length;
		ITableOfContentsNode[] mergeNodes =
			new ITableOfContentsNode[oldSize + newNodes.length];
		System.arraycopy(nodes, 0, mergeNodes, 0, oldSize);
		Color foreground = getTitleForeground(display);
		Color background = getTitleBackground(display);

		for (int i = 0; i < newNodes.length; i++) {
			newNodes[i].createWidgets(inner, foreground, background);
			mergeNodes[i + oldSize] = newNodes[i];
		}
		nodes = mergeNodes;
		tableComposite.setMinWidth(inner.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		inner.pack();
	}

	/**
	* Add the nodes for newWizard. If we haven't created anything
	* set cache the wizard.
	*/
	public void addWizard(IWizard newWizard) {
		//Add nodes if the table is already created
		if (tableComposite == null)
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
	public void updateFor(IWizardPage page) {

		//We may not have created anything yet
		int index = indexOfPage(page);
		if (index == -1) {
			//If there is nothing return. If not we have a new page added
			if (tableComposite == null)
				return;
			ITableOfContentsNode[] newNodes = new ITableOfContentsNode[1];
			ITableOfContentsNode newNode = new WizardTableOfContentsNode(page);
			newNodes[0] = newNode;
			currentNode = newNode;
			addNodes(newNodes, page.getControl().getDisplay());

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
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#getTitleControl()
	 */
	protected Control getTitleControl() {
		return tableComposite;
	}

	/*
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#createTitleControl(Composite, Color, int, int)
	 */
	protected void createTitleControl(
		Composite parent,
		int verticalSpacing,
		int horizontalSpacing) {
			
		tableComposite = new ScrolledComposite(parent,SWT.H_SCROLL);

		inner = new Composite(tableComposite, SWT.NULL);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		inner.setLayout(layout);
		Color foreground = getTitleForeground(parent.getDisplay());
		Color background = getTitleBackground(parent.getDisplay());
		inner.setForeground(foreground);
		inner.setBackground(background);
		tableComposite.setForeground(foreground);
		tableComposite.setBackground(background);

		//Create the nodes if the wizard has not been set.
		if (initialWizard != null) {
			addNodesForWizard(initialWizard);
			currentNode = nodes[0];
		}
		
		tableComposite.setContent(inner);
	}
}
