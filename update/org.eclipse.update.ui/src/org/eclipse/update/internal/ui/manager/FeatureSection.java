package org.eclipse.update.internal.ui.manager;
import org.eclipse.swt.widgets.*;
import java.net.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.resource.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;


public class FeatureSection implements IPropertyChangeListener {
	private String name;
	protected Label nameLabel;
	protected String size;
	protected Label sizeLabel;
	private String description;
	protected Label descriptionLabel;
	private Composite control;
	private Button checkbox;
	private Label infoLinkLabel;
	private String infoLink;
	private URL infoLinkURL;
	

	
/*
 * This is a special layout for the section. Both the
 * header and the description labels will wrap and
 * they will use client's size to calculate needed
 * height. This kind of behaviour is not possible
 * with stock grid layout.
 */
class SectionLayout extends Layout {
	int vspacing = 3;
	int checkboxSpacing = 10;
	int sepHeight = 2;


	protected Point computeSize(Composite parent, int wHint, int hHint, boolean flush) {
		int width = 0;
		int height = 0;
		int cwidth = 0;
	
		if (wHint != SWT.DEFAULT)
		   width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;

		cwidth = width;
				
		if (hHint==SWT.DEFAULT && nameLabel!=null) {
			Point nsize = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += nsize.y;
			height += vspacing;
			if (cwidth==0) cwidth = nsize.x;
		}
		
		if (hHint==SWT.DEFAULT && sizeLabel != null) {
			Point ssize = sizeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += ssize.y;
			height += vspacing;
			if (wHint==SWT.DEFAULT)
			   cwidth = Math.max(cwidth, ssize.x);
		}
		if (hHint == SWT.DEFAULT && descriptionLabel!=null) {
			Point dsize = descriptionLabel.computeSize(cwidth, SWT.DEFAULT, flush);
			height += dsize.y;
			height += vspacing;
		}
		if (hHint == SWT.DEFAULT && infoLink != null) {
			Point isize = infoLinkLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += isize.y;
			height += vspacing;
		}
		
		// add the checkbox
		Point csize = checkbox.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		width += csize.x + checkboxSpacing;
		return new Point(width, height);
	}
	protected void layout(Composite parent, boolean flush) {
		int width = parent.getClientArea().width;
		int height = parent.getClientArea().height;
		
		Point csize = checkbox.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		checkbox.setBounds(0, 0, csize.x, csize.y);
		int x = csize.x + checkboxSpacing;
		int y = 0;
		
		Point nsize = nameLabel.computeSize(width, SWT.DEFAULT, flush);
		nameLabel.setBounds(x, y, width, nsize.y);
		y += nsize.y + vspacing;
		
		Point ssize = sizeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		sizeLabel.setBounds(x, y, ssize.x, ssize.y);
		y += ssize.y + vspacing;

		Point dsize = descriptionLabel.computeSize(width, SWT.DEFAULT, flush);
		descriptionLabel.setBounds(x, y, width, dsize.y);
		y += dsize.y + vspacing;

		if (infoLink!=null) {
			Point isize = infoLinkLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			infoLinkLabel.setBounds(x, y, isize.x, isize.y);
		}
	}
}
	
	
public FeatureSection() {
	JFaceResources.getFontRegistry().addListener(this);
}

public final Control createControl(
	Composite parent,
	FormWidgetFactory factory) {
	Composite section = factory.createComposite(parent);
	SectionLayout slayout = new SectionLayout();
	section.setLayout(slayout);
	section.setData(this);

	checkbox = factory.createButton(section, null, SWT.CHECK);
	nameLabel = factory.createHeadingLabel(section, getName());
	sizeLabel = factory.createLabel(section, getSize());
	descriptionLabel = factory.createLabel(section, getDescription(), SWT.WRAP);
	if (infoLink!=null) {
	   infoLinkLabel = factory.createHyperlinkLabel(section, 
	   infoLink, new IHyperlinkListener() {
	   		public void linkActivated(Control linkLabel) {
	   			openInfoURL();
	   		}
			public void linkEntered(Control linkLabel) {
			}
			public void linkExited(Control linkLabel) {
			}
	   });
	}
	section.setData(this);
	control = section;
	return section;
}

public void dispose() {
	JFaceResources.getFontRegistry().removeListener(this);
}

public void propertyChange(PropertyChangeEvent arg0) {
	if (control!=null && nameLabel!=null) {
		nameLabel.setFont(JFaceResources.getBannerFont());
		control.layout(true);
	}
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
	if (nameLabel!=null) {
		nameLabel.setText(name);
		control.layout();
	}
}
	
public String getSize() {
	return size;
}
	
public void setSize(String size) {
	this.size = size;
	if (sizeLabel != null) {
		sizeLabel.setText(size);
		control.layout();
	}
}
	
public String getDescription() {
	return description;
}
	
public void setDescription(String description) {
	this.description = description;
	if (descriptionLabel != null) {
		descriptionLabel.setText(description);
		control.layout();
	}
}
	
public URL getInfoLinkURL() {
	return infoLinkURL;
}
	
public void setInfoLinkURL(URL url) {
	this.infoLinkURL = url;
}

public String getInfoLink() {
	return infoLink;
}
	
public void setInfoLink(String infoLink) {
	this.infoLink = infoLink;
	if (infoLinkLabel != null) {
		infoLinkLabel.setText(infoLink);
		control.layout();
	}
}

private void openInfoURL() {
}

}

