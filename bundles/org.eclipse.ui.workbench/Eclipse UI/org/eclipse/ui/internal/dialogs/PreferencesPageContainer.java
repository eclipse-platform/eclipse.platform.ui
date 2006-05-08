/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The PreferencesPageContainer is the container object for the preference pages
 * in a node.
 */
public class PreferencesPageContainer implements IPreferencePageContainer {

	private Composite control;

	private ScrolledComposite scrolled;

	private class PreferenceEntry {

		Composite composite;

		IPreferenceNode node;

		String title;

		int offset;

		Label expandImage;

		Label titleLabel;

		Composite pageContainer;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param displayedNode
		 * @param pageTitle
		 */
		PreferenceEntry(IPreferenceNode displayedNode, String pageTitle) {
			node = displayedNode;
			title = pageTitle;
		}

		/**
		 * Add the subnodes of the receiver.
		 */
		private void addSubNodes() {
			IPreferenceNode[] subnodes = node.getSubNodes();
			PreferenceEntry previous = null;
			for (int i = 0; i < subnodes.length; i++) {
				PreferenceEntry entry = createEntry(subnodes[i], subnodes[i]
						.getLabelText(), offset + 1);
				if (previous == null) {
					entry.composite.moveBelow(this.composite);
				} else {
					entry.composite.moveBelow(previous.composite);
				}
				previous = entry;
			}
		}

		/**
		 * Create the contents of the entry in parent. When laying this out
		 * indent the composite ident units.
		 * 
		 * @param indent
		 */
		void createContents(int indent) {

			composite = new Composite(control, SWT.NULL);

			// Create the title area which will contain
			// a title, message, and image.

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalIndent = IDialogConstants.SMALL_INDENT * indent;
			composite.setLayoutData(gridData);

			FormLayout layout = new FormLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);

			Font titleFont = JFaceResources.getBannerFont();

			expandImage = new Label(composite, SWT.RIGHT);
			expandImage.setText("+");//$NON-NLS-1$
			expandImage.setFont(titleFont);

			FormData imageData = new FormData();
			imageData.top = new FormAttachment(0);
			imageData.left = new FormAttachment(0,
					IDialogConstants.HORIZONTAL_SPACING);
			expandImage.setLayoutData(imageData);

			// Title image
			titleLabel = new Label(composite, SWT.LEFT);
			titleLabel.setText(title);
			titleLabel.setFont(titleFont);

			FormData titleData = new FormData();
			titleData.right = new FormAttachment(100);
			titleData.top = new FormAttachment(0);
			titleData.left = new FormAttachment(expandImage,
					IDialogConstants.HORIZONTAL_SPACING);
			titleLabel.setLayoutData(titleData);

			titleLabel.addMouseListener(new MouseAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
				 */
				public void mouseDown(MouseEvent e) {

					if (pageContainer == null) {
						boolean adjustScrollbars = false;

						pageContainer = new Composite(composite, SWT.BORDER);

						FormData containerData = new FormData();
						containerData.top = new FormAttachment(titleLabel, 0);
						containerData.left = new FormAttachment(0);
						containerData.right = new FormAttachment(100);
						pageContainer.setLayoutData(containerData);

						pageContainer.setLayout(new GridLayout());

						node.createPage();
						node.getPage().createControl(pageContainer);
						node.getPage().setContainer(
								PreferencesPageContainer.this);
						node.getPage().getControl().setLayoutData(
								new GridData(GridData.FILL_BOTH));
						adjustScrollbars = true;

						Point contentSize = node.getPage().computeSize();
						Rectangle totalArea = composite.getClientArea();

						if (contentSize.x < totalArea.width) {
							contentSize.x = totalArea.width;
						}

						node.getPage().setSize(contentSize);
						if (adjustScrollbars) {
							adjustScrollbars(contentSize);
						}

						expandImage.setText("-");//$NON-NLS-1$

						addSubNodes();

						setSelectionColors(composite.getDisplay()
								.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					} else {
						setSelectionColors(null);
						pageContainer.dispose();
						pageContainer = null;
						expandImage.setText("+");//$NON-NLS-1$
					}

					control.layout(true);
				}
			});
			offset = indent;
		}

		/**
		 * Set the background colors and the labels due to selection.
		 * 
		 * @param highlight
		 *            The highlight to set
		 */
		private void setSelectionColors(Color highlight) {
			composite.setBackground(highlight);
			titleLabel.setBackground(highlight);
			expandImage.setBackground(highlight);
		}

	}

	/**
	 * Create a new instance of the receiver.
	 */
	public PreferencesPageContainer() {
		super();
	}

	/**
	 * Create the contents area of the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	void createContents(Composite parent, int style) {
		scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		GridData newPageData = new GridData(GridData.FILL_BOTH);
		scrolled.setLayoutData(newPageData);

		control = new Composite(scrolled, style);

		scrolled.setContent(control);
		scrolled.setExpandVertical(true);
		scrolled.setExpandHorizontal(true);
		GridData controlData = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(controlData);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		control.setLayout(layout);

	}

	/**
	 * Return the top level control
	 * 
	 * @return Control
	 */
	Control getControl() {
		return control;
	}

	/**
	 * Show the selected node. Return whether or not this succeeded.
	 * 
	 * @param node
	 * @return <code>true</code> if the page selection was sucessful
	 *         <code>false</code> is unsuccessful
	 */
	boolean show(IPreferenceNode node) {
		createGeneralEntry(node);
		control.layout(true);
		return true;
	}

	/**
	 * Create an entry for the receiver with the general tag. Do not recurse
	 * through the children as this is the implied top node.
	 * 
	 * @param node
	 */
	private void createGeneralEntry(IPreferenceNode node) {
		PreferenceEntry entry = createEntry(node, "General", 0); //$NON-NLS-1$
		entry.addSubNodes();

	}

	/**
	 * Create an entry with the given title for the IPreferenceNode with an
	 * indent i.
	 * 
	 * @param node
	 * @param name
	 * @param indent
	 * @return the entry
	 */
	private PreferenceEntry createEntry(IPreferenceNode node, String name,
			int indent) {
		PreferenceEntry entry = new PreferenceEntry(node, name);
		entry.createContents(indent);
		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		// TODO Auto-generated method stub

	}

	/**
	 * Adjust the scrollbars for the content that just got added.
	 * 
	 * @param contentSize
	 */
	private void adjustScrollbars(Point contentSize) {

		Point size = control.getSize();
		scrolled.setMinHeight(size.y + contentSize.y);
		scrolled.setMinWidth(Math.max(size.x, contentSize.x));
	}

}
