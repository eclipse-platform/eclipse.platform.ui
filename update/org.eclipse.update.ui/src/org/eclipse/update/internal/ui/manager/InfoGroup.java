package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.core.*;
import org.eclipse.swt.layout.*;
import java.net.URL;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class InfoGroup extends ExpandableGroup {
	private static final String KEY_TEXT = "InfoGroup.plainTextVersion";
	private static final String KEY_HTML = "InfoGroup.htmlVersion";
	private IInfo info;
	private SelectableFormLabel textLink;
	private SelectableFormLabel urlLink;
	private String textLabelText;
	private String urlLabelText;
	private DetailsView view;

	public InfoGroup(DetailsView view) {
		this.view = view;
		textLabelText = UpdateUIPlugin.getResourceString(KEY_TEXT);
		urlLabelText = UpdateUIPlugin.getResourceString(KEY_HTML);
	}

	public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
   		GridLayout layout = new GridLayout();
  		expansion.setLayout(layout);
   		layout.marginWidth = 0;
		textLink = new SelectableFormLabel(expansion, SWT.NULL);
		textLink.setText(textLabelText);
		factory.turnIntoHyperlink(textLink, new HyperlinkAdapter() {
			public void linkActivated(Control link) {
				showText();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		//textLink.setLayoutData(gd);
		urlLink = new SelectableFormLabel(expansion, SWT.NULL);
		urlLink.setText(urlLabelText);
		factory.turnIntoHyperlink(urlLink, new HyperlinkAdapter() {
			public void linkActivated(Control link) {
				showURL();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		//urlLink.setLayoutData(gd);
	}
	
	protected void linkActivated() {
		boolean hasText = info.getText()!=null && info.getText().length()>0;
		boolean hasURL = info.getURL()!=null;
		if (hasText && hasURL) return;
		if (hasText)
			showText();
		else if (hasURL)
		   showURL();
	}
	
	public void setInfo(IInfo info) {
		this.info = info;
		boolean hasText = info.getText()!=null && info.getText().length()>0;
		boolean hasURL = info.getURL()!=null;
		setExpandable(hasText && hasURL);
		setExpanded(false);
	}
	
	public void setTextLabel(String text) {
		textLabelText = text;
	}
	public void setURLLabel(String text) {
		urlLabelText = text;
	}
	
	private void showText() {
		String text = info.getText();
		view.showText(text);
	}
	
	private void showURL() {
		URL url = info.getURL();
		view.showURL(url.toString());
	}
}