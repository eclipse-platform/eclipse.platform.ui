package org.eclipse.jface.wizard;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialogHeader;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The WizardTableOfContents is a class that displays a series
 * of widgets depicting the current state of pages within a wizard.
 */
public class WizardTableOfContentsHeader extends TitleAreaDialogHeader {

	private Composite tableComposite;
	private WizardTableOfContentsNode[] nodes =
		new WizardTableOfContentsNode[0];
	private IWizard initialWizard;
	private WizardTableOfContentsNode currentNode;

	public WizardTableOfContentsHeader(WizardDialog wizardDialog) {
		super(wizardDialog);
	}

	/**
	 * Create the contents of the table of contents. The layout data of the
	 * control should not be set as this will be determined by the creator
	 * of parent.
	 * @param parent
	 */
	public Control createTableOfContents(Composite parent) {

		tableComposite = new Composite(parent, SWT.NULL);
		tableComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		//Create the nodes if the wizard has not been set.
		if (initialWizard != null){
			addNodesForWizard(initialWizard);
			currentNode = nodes[0];
		}

		return tableComposite;

	}

	/**
	 * Add the nodes for the current wizard. This should be called whenever
	 * the wizard is updated for a dialog using the WizardTableOfContentsHeader.
	 */
	private void addNodesForWizard(IWizard wizard) {
		IWizardPage[] pages = wizard.getPages();
		int oldSize = nodes.length;
		WizardTableOfContentsNode[] newNodes = new 
			WizardTableOfContentsNode[oldSize + pages.length];
		System.arraycopy(nodes,0,newNodes,0,oldSize);
		
		for (int i = 0; i < pages.length; i++) {
			WizardTableOfContentsNode node =
				new WizardTableOfContentsNode(pages[i]);
			node.createWidgets(tableComposite);
			newNodes[oldSize + i] = node;
		}
		
		nodes = newNodes;
	}

	/**
	* Creates the  title area.
	*
	* @param parent the SWT parent for the title area widgets
	* @return Control with the highest x axis value.
	*/
	public Control createTitleArea(
		Composite parent,
		FontMetrics parentMetrics) {

		this.metrics = parentMetrics;

		// add a dispose listener
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {

				if (errorMsgAreaBackground != null)
					errorMsgAreaBackground.dispose();
			}
		});

		// Determine the background color of the title bar
		Display display = parent.getDisplay();

		Color background = JFaceColors.getBannerBackground(display);
		Color foreground = JFaceColors.getBannerForeground(display);

		int verticalSpacing =
			Dialog.convertVerticalDLUsToPixels(
				metrics,
				IDialogConstants.VERTICAL_SPACING);
		int horizontalSpacing =
			Dialog.convertHorizontalDLUsToPixels(
				metrics,
				IDialogConstants.HORIZONTAL_SPACING);
		parent.setBackground(background);

		Control tableOfContents = createTableOfContents(parent);

		FormData tocData = new FormData();
		tocData.top = new FormAttachment(0, verticalSpacing);
		tocData.left = new FormAttachment(0, horizontalSpacing);
		tocData.right = new FormAttachment(100, horizontalSpacing);
		tableOfContents.setLayoutData(tocData);

		// Message image @ bottom, left
		messageImageLabel = new Label(parent, SWT.CENTER);
		messageImageLabel.setBackground(background);

		// Message label @ bottom, center
		messageLabel = new Label(parent, SWT.WRAP);
		JFaceColors.setColors(messageLabel, foreground, background);
		messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
		messageLabel.setFont(JFaceResources.getDialogFont());

		// Filler labels
		leftFillerLabel = new Label(parent, SWT.CENTER);
		leftFillerLabel.setBackground(background);

		bottomFillerLabel = new Label(parent, SWT.CENTER);
		bottomFillerLabel.setBackground(background);

		setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);

		return messageLabel;

	}

	/*
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#setLayoutsForNormalMessage(int, int)
	 */

	protected void setLayoutsForNormalMessage(
		int verticalSpacing,
		int horizontalSpacing) {

		FormData messageImageData = new FormData();
		messageImageData.top =
			new FormAttachment(tableComposite, verticalSpacing);
		messageImageData.left = new FormAttachment(0, H_GAP_IMAGE);
		messageImageLabel.setLayoutData(messageImageData);

		FormData messageLabelData = new FormData();
		messageLabelData.top =
			new FormAttachment(tableComposite, verticalSpacing);
		messageLabelData.right = new FormAttachment(0, horizontalSpacing);
		messageLabelData.left =
			new FormAttachment(messageImageLabel, horizontalSpacing);

		messageLabel.setLayoutData(messageLabelData);

		FormData fillerData = new FormData();
		fillerData.left = new FormAttachment(0, horizontalSpacing);
		fillerData.top = new FormAttachment(messageImageLabel, 0);
		fillerData.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		bottomFillerLabel.setLayoutData(fillerData);

		FormData data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0);
		leftFillerLabel.setLayoutData(data);
	}

	/*
	 * @see org.eclipse.jface.dialogs.TitleAreaDialogHeader#setLayoutsForErrorMessage(int, int)
	 */
	protected void setLayoutsForErrorMessage(
		int verticalSpacing,
		int horizontalSpacing) {

		messageImageLabel.setVisible(true);
		bottomFillerLabel.setVisible(true);
		leftFillerLabel.setVisible(true);

		/**
		 * Note that we do not use horizontalSpacing here 
		 * as when the background of the messages changes
		 * there will be gaps between the icon label and the
		 * message that are the background color of the shell.
		 * We add a leading space elsewhere to compendate for this.
		 */

		FormData data = new FormData();
		data.left = new FormAttachment(0, H_GAP_IMAGE);
		data.top = new FormAttachment(tableComposite, verticalSpacing);
		messageImageLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0, SWT.RIGHT);
		bottomFillerLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0);
		leftFillerLabel.setLayoutData(data);

		FormData messageLabelData = new FormData();
		messageLabelData.top =
			new FormAttachment(tableComposite, verticalSpacing);
		messageLabelData.right = new FormAttachment(100, horizontalSpacing);
		messageLabelData.left = new FormAttachment(messageImageLabel, 0);

		messageLabel.setLayoutData(messageLabelData);

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
			tableComposite.pack();
		}
	}

	/**
	 * Select the node corresponding to the page.
	 * @param IWorkbenchPage
	 */
	public void selectPage(IWizardPage page) {
		
		//We may not have created anything yet
		int index = indexOfPage(page);
		if(index == -1)
			return;
			
		currentNode = nodes[index];
		WizardTableOfContentsNode checkNode = currentNode;
		for (int i = index + 1; i < nodes.length; i++) {
			nodes[i].setEnabled(checkNode.getPage().canFlipToNextPage());
			checkNode = nodes[i];
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

}
