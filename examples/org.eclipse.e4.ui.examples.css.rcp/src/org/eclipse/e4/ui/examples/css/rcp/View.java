package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {

	public static final String ID = "org.eclipse.e4.ui.examples.css.rcp.view";
	public static View TOPMOST;

	private boolean read = false;
	private Label dateWidget;
	
	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		setCSSClassName(top, "messageBanner");
		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		banner.setLayout(layout);
		setCSSClassName(banner, "messageBanner");
		
		// setup bold font
		// we'll do this in CSS now instead
//		Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);    
		
		Label l = new Label(banner, SWT.WRAP);
		l.setText("Subject:");
//		l.setFont(boldFont);
		setCSSClassName(l, "messageTitle");

		l = new Label(banner, SWT.WRAP);
		setCSSClassName(l, "messageBannerContent");
		setCSSClassName(l, "messageSubject");
		l.setText("This is a message about the cool Eclipse RCP!");
		
		l = new Label(banner, SWT.WRAP);
		l.setText("From:");
//		l.setFont(boldFont);
		setCSSClassName(l, "messageTitle");
    
		final Link link = new Link(banner, SWT.NONE);
		link.setText("<a>nicole@mail.org</a>");
		setCSSClassName(link, "messageBannerContent");
		link.addSelectionListener(new SelectionAdapter() {    
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getSite().getShell(), "Not Implemented", "Imagine the address book or a new message being created now.");
			}    
		});
    
		l = new Label(banner, SWT.WRAP);
		l.setText("Date: ");
//		l.setFont(boldFont);
		setCSSClassName(l, "messageTitle");
		l = new Label(banner, SWT.WRAP);
		dateWidget = l;		
		l.setText("10:34 am "); // add space since we know it will be italic and that gets clipped due to SWT bug

		setCSSClassName(l, "messageBannerContent");
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
		widget.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, name);
		//Ideally just changing the widget's CSS class would trigger a re-styling,
		//but until bug #260407 is fixed we must call this next line
		ApplicationWorkbenchAdvisor.INSTANCE.engine.applyStyles(widget, true);
	}

	private void updateCSSForReadState() {
		setCSSClassName(
			dateWidget,
			read
				? "messageDateRead"
				: "messageDateUnRead"
			);
	}
}
