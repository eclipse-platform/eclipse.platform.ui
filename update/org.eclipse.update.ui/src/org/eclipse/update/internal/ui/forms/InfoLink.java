package org.eclipse.update.internal.ui.forms;
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
import org.eclipse.update.internal.ui.views.*;

public class InfoLink extends HyperlinkAdapter {
	private IURLEntry info;
	private SelectableFormLabel linkLabel;
	private DetailsView view;
	private boolean visible = true;
	private String text="";

	public InfoLink(DetailsView view) {
		this.view = view;
	}
	
	public void createControl(Composite composite, FormWidgetFactory factory) {
		linkLabel = factory.createSelectableLabel(composite, text);
		factory.turnIntoHyperlink(linkLabel, this);
		linkLabel.setVisible(visible);
	}
	
	public Control getControl() {
		return linkLabel;
	}

	public void linkActivated(Control link) {
		if (info==null) return;
		URL url = info.getURL();
		if (url!=null) {
			view.showURL(url.toString());
			return;
		}
		String annotation = info.getAnnotation();
		if (annotation !=null && annotation.length()>0) {
			view.showText(annotation);
		}
	}
	
	public void setInfo(IURLEntry info) {
		this.info = info;
		if (info!=null) {
			boolean hasText = info.getAnnotation()!=null && info.getAnnotation().length()>0;
			boolean hasURL = info.getURL()!=null;
			setVisible(hasText || hasURL);
		}
		else setVisible(false);
	}
	
	private void setVisible(boolean visible) {
		this.visible = visible;
		if (linkLabel!=null) {
			linkLabel.setVisible(visible);
		}
	}
	/**
	 * Gets the text.
	 * @return Returns a String
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text) {
		this.text = text;
		if (linkLabel != null) {
			linkLabel.setText(text);
		}
	}

}