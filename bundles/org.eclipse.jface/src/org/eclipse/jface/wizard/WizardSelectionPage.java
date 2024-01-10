/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.wizard;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of a wizard page that manages a
 * set of embedded wizards.
 * <p>
 * A wizard selection page should present a list of wizard nodes
 * corresponding to other wizards. When the end user selects one of
 * them from the list, the first page of the selected wizard becomes
 * the next page. The only new methods introduced by this class are
 * <code>getSelectedNode</code> and <code>setSelectedNode</code>.
 * Otherwise, the subclass contract is the same as <code>WizardPage</code>.
 * </p>
 */
public abstract class WizardSelectionPage extends WizardPage {

	/**
	 * The selected node; <code>null</code> if none.
	 */
	private IWizardNode selectedNode = null;

	/**
	 * List of wizard nodes that have cropped up in the past
	 * (element type: <code>IWizardNode</code>).
	 */
	private List<IWizardNode> selectedWizardNodes = new ArrayList<>();

	/**
	 * Creates a new wizard selection page with the given name, and
	 * with no title or image.
	 *
	 * @param pageName the name of the page
	 */
	protected WizardSelectionPage(String pageName) {
		super(pageName);
		// Cannot finish from this page
		setPageComplete(false);
	}

	/**
	 * Adds the given wizard node to the list of selected nodes if
	 * it is not already in the list.
	 *
	 * @param node the wizard node, or <code>null</code>
	 */
	private void addSelectedNode(IWizardNode node) {
		if (node == null) {
			return;
		}

		if (selectedWizardNodes.contains(node)) {
			return;
		}

		selectedWizardNodes.add(node);
	}

	/**
	 * The <code>WizardSelectionPage</code> implementation of
	 * this <code>IWizardPage</code> method returns <code>true</code>
	 * if there is a selected node.
	 */
	@Override
	public boolean canFlipToNextPage() {
		return selectedNode != null;
	}

	/**
	 * The <code>WizardSelectionPage</code> implementation of an <code>IDialogPage</code>
	 * method disposes of all nested wizards. Subclasses may extend.
	 */
	@Override
	public void dispose() {
		super.dispose();
		// notify nested wizards
		for (IWizardNode selectedWizardNode : selectedWizardNodes) {
			selectedWizardNode.dispose();
		}
	}

	/**
	 * The <code>WizardSelectionPage</code> implementation of
	 * this <code>IWizardPage</code> method returns the first page
	 * of the currently selected wizard if there is one.
	 */
	@Override
	public IWizardPage getNextPage() {
		if (selectedNode == null) {
			return null;
		}

		boolean isCreated = selectedNode.isContentCreated();

		IWizard wizard = selectedNode.getWizard();

		if (wizard == null) {
			setSelectedNode(null);
			return null;
		}

		if (!isCreated) {
			// Allow the wizard to know its surrounding container
			wizard.setContainer(getContainer());
			// Allow the wizard to create its pages
			wizard.addPages();
		}

		return wizard.getStartingPage();
	}

	/**
	 * Returns the currently selected wizard node within this page.
	 *
	 * @return the wizard node, or <code>null</code> if no node is selected
	 */
	public IWizardNode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * Sets or clears the currently selected wizard node within this page.
	 *
	 * @param node the wizard node, or <code>null</code> to clear
	 */
	protected void setSelectedNode(IWizardNode node) {
		addSelectedNode(node);
		selectedNode = node;
		if (isCurrentPage()) {
			getContainer().updateButtons();
		}
	}
}
