package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.update.ui.forms.FormWidgetFactory;
import org.eclipse.update.core.*;
import org.eclipse.swt.layout.*;
import java.net.URL;

public class InfoGroup extends ExpandableGroup {
	private IInfo info;
	private Label textLink;
	private Label urlLink;
	private String textLabelText;
	private String urlLabelText;
	private DetailsView view;

	public InfoGroup(DetailsView view) {
		this.view = view;
		textLabelText = "Plain Text Version";
		urlLabelText = "HTML Version";
	}

	public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
   		GridLayout layout = new GridLayout();
  		expansion.setLayout(layout);
   		layout.marginWidth = 0;
		textLink = factory.createHyperlinkLabel(expansion, textLabelText, new HyperlinkAdapter() {
			public void linkActivated(Control link) {
				showText();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		//textLink.setLayoutData(gd);
		urlLink = factory.createHyperlinkLabel(expansion, urlLabelText, new HyperlinkAdapter() {
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
	
	protected URL resolveURL(URL inputURL) {
		return inputURL;
	}
	
	private void showURL() {
		URL url = resolveURL(info.getURL());
		view.showURL(url.toString());
	}
}