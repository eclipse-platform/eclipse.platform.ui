package org.eclipse.jface.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * The WizardTableOfContents is a class that displays a series
 * of widgets depicting the current state of pages within a wizard.
 */
public class WizardTableOfContentsHeader {

	private ScrolledComposite tableComposite;
	private Composite inner;
	private WizardTableOfContentsNode[] nodes = new WizardTableOfContentsNode[0];
	private IWizard initialWizard;
	private WizardTableOfContentsNode currentNode;
	private WizardDialog mainDialog;

	public WizardTableOfContentsHeader(WizardDialog wizardDialog) {
		mainDialog = wizardDialog;
	}

	/**
	 * Add the nodes for the current wizard. This should be called whenever
	 * the wizard is updated for a dialog using the WizardTableOfContentsHeader.
	 */
	private void addNodesForWizard(IWizard wizard) {

		WizardTableOfContentsNode[] newNodes;

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

	private void addNodes(WizardTableOfContentsNode[] newNodes, Display display) {

		int oldSize = nodes.length;
		WizardTableOfContentsNode[] mergeNodes =
			new WizardTableOfContentsNode[oldSize + newNodes.length];
		System.arraycopy(nodes, 0, mergeNodes, 0, oldSize);

		for (int i = 0; i < newNodes.length; i++) {
			newNodes[i].createWidgets(inner);
			mergeNodes[i + oldSize] = newNodes[i];
		}
		nodes = mergeNodes;
		//Lay it out again as the children have changed.
		inner.layout();
		tableComposite.setMinWidth(
			inner.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
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
					WizardTableOfContentsNode nextNode = nodes[currentIndex];

					//Replace the next pages if required
					if (nextNode.getPage().getWizard().equals(newWizard))
						return;
					else {
						for (int i = currentIndex + 1; i < nodes.length; i++) {
							nodes[i].dispose();
						}
						WizardTableOfContentsNode[] newNodes =
							new WizardTableOfContentsNode[currentIndex + 1];
						for (int i = 0; i <= currentIndex; i++) {
							newNodes[i] = nodes[i];
						}

						nodes = newNodes;
					}
				}
			}
			addNodesForWizard(newWizard);
			System.out.println(inner.getChildren().length);
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
			WizardTableOfContentsNode[] newNodes = new WizardTableOfContentsNode[1];
			WizardTableOfContentsNode newNode = new WizardTableOfContentsNode(page);
			newNodes[0] = newNode;
			currentNode = newNode;
			addNodes(newNodes, page.getControl().getDisplay());

		} else {
			currentNode = nodes[index];
			WizardTableOfContentsNode checkNode = currentNode;
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
	protected Control createTableOfContentsControl(Composite parent) {

		tableComposite = new ScrolledComposite(parent, SWT.H_SCROLL);

		inner = new Composite(tableComposite, SWT.NULL);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		inner.setLayout(layout);

		//Create the nodes if the wizard has not been set.
		if (initialWizard != null) {
			addNodesForWizard(initialWizard);
			currentNode = nodes[0];
		}

		tableComposite.setContent(inner);
		return tableComposite;
	}
}
