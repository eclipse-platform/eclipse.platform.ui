/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A PreferenceAreaEntry is an object that manages the widgets
 * that display a preference page.
 */
public class PreferenceAreaEntry implements IPreferencePageContainer {

	private IPreferenceNode node;

	private FilteredPreferenceDialog dialog;
	
	private Composite titleArea;

	private Label titleImage;
	
	private DialogMessageArea messageArea;
	
	private boolean showingError = true;
	
	private final String ellipsis = "..."; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver on the preference node.
	 */
	public PreferenceAreaEntry(IPreferenceNode pageNode, FilteredPreferenceDialog preferenceDialog) {
		node = pageNode;
		dialog = preferenceDialog;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		return dialog.getPreferenceStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		dialog.updateButtons();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
		IPreferencePage currentPage = node.getPage();
		
		String message = currentPage.getMessage();
		int messageType = IMessageProvider.NONE;
		if (message != null && currentPage instanceof IMessageProvider)
			messageType = ((IMessageProvider) currentPage).getMessageType();
		String errorMessage = currentPage.getErrorMessage();
		if (errorMessage != null) {
			message = errorMessage;
			messageType = IMessageProvider.ERROR;
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;
				titleImage.setImage(null);
				titleImage.setBackground(JFaceColors
						.getErrorBackground(titleImage.getDisplay()));
				titleImage.setSize(0, 0);
				titleImage.getParent().layout();
			}
		} else {
			if (showingError) {
				// we were previously showing an error
				showingError = false;
				titleImage
						.setImage(JFaceResources.getImage(PreferenceDialog.PREF_DLG_TITLE_IMG));
				titleImage.computeSize(SWT.NULL, SWT.NULL);
				titleImage.getParent().layout();
			}
		}
		messageArea.updateText(getShortenedString(message), messageType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		IPreferencePage currentPage = node.getPage();
		messageArea.showTitle(currentPage.getTitle(), currentPage.getImage());

	}

	/**
	 * Create the contents of the receiver in Control
	 * @param control
	 */
	public void createContents(Composite control) {
		
		Composite mainContainer = new Composite(control,SWT.BORDER);
		GridLayout layout = new GridLayout();
		mainContainer.setLayout(layout);
		
		mainContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createTitleArea(mainContainer);
		titleArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
		node.createPage();
		node.getPage().createControl(mainContainer);
		
		node.getPage().getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		mainContainer.layout();
		updateTitle();
		control.pack();

	}

	/**
	 * Create the titleArea in the parent.
	 * @param parent
	 */
	private void createTitleArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		int margins = 2;
		titleArea = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = margins;
		layout.marginWidth = margins;
		titleArea.setLayout(layout);
		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color background = JFaceColors.getBannerBackground(display);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = JFaceResources.getImage(PreferenceDialog.PREF_DLG_TITLE_IMG)
				.getBounds().height
				+ (margins * 3);
		titleArea.setLayoutData(layoutData);
		titleArea.setBackground(background);

		titleArea.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(titleArea.getDisplay().getSystemColor(
						SWT.COLOR_WIDGET_NORMAL_SHADOW));
				Rectangle bounds = titleArea.getClientArea();
				bounds.height = bounds.height - 2;
				bounds.width = bounds.width - 1;
				e.gc.drawRectangle(bounds);
			}
		});

		// Message label
		messageArea = new DialogMessageArea();
		messageArea.createContents(titleArea);

		titleArea.addControlListener(new ControlAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlResized(ControlEvent e) {
				updateMessage();
			}
		});

		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.BANNER_FONT.equals(event.getProperty()))
					updateMessage();
			}
		};

		titleArea.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});
		JFaceResources.getFontRegistry().addListener(fontListener);
		// Title image
		titleImage = new Label(titleArea, SWT.LEFT);
		titleImage.setBackground(background);
		titleImage.setImage(JFaceResources.getImage(PreferenceDialog.PREF_DLG_TITLE_IMG));
		FormData imageData = new FormData();
		imageData.right = new FormAttachment(100);
		imageData.top = new FormAttachment(0);
		imageData.bottom = new FormAttachment(100);
		titleImage.setLayoutData(imageData);
		messageArea.setTitleLayoutData(createMessageAreaData());
		messageArea.setMessageLayoutData(createMessageAreaData());

	}

	/**
	 * Create the layout data for the message area.
	 * 
	 * @return FormData for the message area.
	 */
	private FormData createMessageAreaData() {
		FormData messageData = new FormData();
		messageData.top = new FormAttachment(0);
		messageData.bottom = new FormAttachment(titleImage, 0, SWT.BOTTOM);
		messageData.right = new FormAttachment(titleImage, 0);
		messageData.left = new FormAttachment(0);
		return messageData;
	}
	
	/**
	 * Shortened the message if too long.
	 * 
	 * @param textValue
	 *            The messgae value.
	 * @return The shortened string.
	 */
	private String getShortenedString(String textValue) {
		if (textValue == null)
			return null;
		Display display = titleArea.getDisplay();
		GC gc = new GC(display);
		int maxWidth = titleArea.getBounds().width - 28;
		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int ellipsisWidth = gc.textExtent(ellipsis).x;
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = gc.textExtent(s1).x;
			int l2 = gc.textExtent(s2).x;
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				gc.dispose();
				return s1 + ellipsis + s2;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}

}
