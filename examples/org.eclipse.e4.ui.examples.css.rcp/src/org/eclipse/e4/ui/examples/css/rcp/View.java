/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	public static final String ID = "org.eclipse.e4.ui.examples.css.rcp.view";
	public static View TOPMOST;

	private boolean read = false;
	private Label subject;
	
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setCSSClassName(top, "messageBanner");
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		topLayout.numColumns = 1;
		top.setLayout(topLayout);

		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		setCSSClassName(banner, "messageBanner");
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));

		GridLayout bannerLayout = new GridLayout();
		bannerLayout.marginHeight = 15;
		bannerLayout.marginWidth = 10;
		bannerLayout.numColumns = 1;
		banner.setLayout(bannerLayout);
				
		// group for sender and date
		Composite senderAndDate = new Composite(banner, SWT.NONE);
		setCSSClassName(banner, "messageBanner");
//		senderAndDate.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));

		GridLayout senderAndDateLayout = new GridLayout();
		senderAndDateLayout.marginHeight = 0;
		senderAndDateLayout.marginWidth = 0;
		senderAndDateLayout.numColumns = 2;
		senderAndDate.setLayout(senderAndDateLayout);
		
		final Link link = new Link(senderAndDate, SWT.NONE);
		link.setText("<a>nicole@mail.org</a>");
		setCSSClassName(link, "messageSender");
		link.addSelectionListener(new SelectionAdapter() {    
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getSite().getShell(), "Not Implemented", "Imagine the address book or a new message being created now.");
			}    
		});
  	
		Label l = new Label(senderAndDate, SWT.WRAP);
		l.setText("  10:34 am"); 
		setCSSClassName(l, "messageDate");
		

		subject = new Label(banner, SWT.WRAP);
		setCSSClassName(subject, "messageSubject");
		subject.setText("This is a message about the cool Eclipse CSS! ");
		    
		updateCSSForReadState();
		
		// message contents
		Text text = new Text(top, SWT.MULTI | SWT.WRAP);
		text.setText("\n" +
						"This RCP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:\n"+
						"- add a top-level menu and toolbar with actions\n"+
						"- add keybindings to actions\n" +
						"- create views that can't be closed and\n"+
						"  multiple instances of the same view\n"+
						"- perspectives with placeholders for new views\n"+
						"- use the default about dialog\n"+
						"- create a product definition\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TOPMOST = this;
	}

	public void setFocus() {
		TOPMOST = this;
	}

	//TODO: the recommended way to do this but it's because this examples uses views not editors
	public boolean isTopMost() {
		return TOPMOST == this;
	}

	public void markAsRead() {
		read = true;
		updateCSSForReadState();		
	}

	private void setCSSClassName(Widget widget, String name) {
		WidgetElement.setCSSClass(widget, name);
		//Ideally just changing the widget's CSS class would trigger a re-styling,
		//but until bug #260407 is fixed we must call this next line
		WidgetElement.getEngine(widget).applyStyles(widget, true);
	}

	private void updateCSSForReadState() {
		setCSSClassName(
			subject,
			read
				? "messageSubjectRead"
				: "messageSubjectUnRead"
			);
	}
}
