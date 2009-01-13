package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	public static final String ID = "org.eclipse.e4.ui.examples.css.rcp.view";

	private boolean read = false;
	private Label dateWidget;
	
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);
		
		// setup bold font
		// we'll do this in CSS now instead
//		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);    
		
		Label l = new Label(banner, SWT.WRAP);
		l.setText("Subject:");
//		l.setFont(boldFont);
		l.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "title");
		l = new Label(banner, SWT.WRAP);
		l.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "messageSubject");
		l.setText("This is a message about the cool Eclipse RCP!");
		
		l = new Label(banner, SWT.WRAP);
		l.setText("From:");
//		l.setFont(boldFont);
		l.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "title");
    
		final Link link = new Link(banner, SWT.NONE);
		link.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "messageSender");
		link.setText("<a>nicole@mail.org</a>");
		link.addSelectionListener(new SelectionAdapter() {    
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getSite().getShell(), "Not Implemented", "Imagine the address book or a new message being created now.");
			}    
		});
    
		l = new Label(banner, SWT.WRAP);
		l.setText("Date:");
//		l.setFont(boldFont);
		l.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "title");
		l = new Label(banner, SWT.WRAP);
		dateWidget = l;		
		l.setText("10:34 am");
		updateCSSForReadState();
		
		// message contents
		Text text = new Text(top, SWT.MULTI | SWT.WRAP);
		text.setText("This RCP Application was generated from the PDE Plug-in Project wizard. This sample shows how to:\n"+
						"- add a top-level menu and toolbar with actions\n"+
						"- add keybindings to actions\n" +
						"- create views that can't be closed and\n"+
						"  multiple instances of the same view\n"+
						"- perspectives with placeholders for new views\n"+
						"- use the default about dialog\n"+
						"- create a product definition\n");
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void setFocus() {
	}

	public void markAsRead() {
		read = true;
		updateCSSForReadState();		
	}

	private void updateCSSForReadState() {
		dateWidget.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, 
			read
				? "messageDateRead"
				: "messageDateUnRead");
		
		//Ideally just changing the widget's CSS class would trigger a restyling,
		//but until bug #260407 is fixed we must call this next line 
		ApplicationWorkbenchAdvisor.INSTANCE.engine.applyStyles(dateWidget, true);
	}
}
