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
	private Label nameLabel;
	private String provider;
	private Label providerLabel;
	private String size;
	private Label sizeLabel;
	private String description;
	private Label descriptionLabel;
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
	int hmargin = 0;
	int vmargin = 3;
	int vspacing = 3;
	int checkboxSpacing = 10;

	protected Point computeSize(Composite parent, int wHint, int hHint, boolean flush) {
		int width = 0;
		int height = 0;
		int parentWidth = parent.getSize().x;
		boolean useParent=false;
		
		if (parentWidth > 0) {
			width = parentWidth;
			useParent = true;
		}
	
		// add the checkbox
		Point csize = checkbox.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		if (!useParent) {
			width += csize.x + checkboxSpacing;
			width += hmargin + hmargin;
		}
		height += vmargin + vmargin;

		if (nameLabel!=null) {
			Point nsize = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += nsize.y;
			height += vspacing;
			if (!useParent) width += nsize.x;
		}
		
		if (providerLabel != null) {
			Point psize = providerLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += psize.y;
			height += vspacing;
			if (!useParent)
			   width = Math.max(width, psize.x);
		}
		if (sizeLabel != null) {
			Point ssize = sizeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += ssize.y;
			height += vspacing;
			if (!useParent)
			   width = Math.max(width, ssize.x);
		}
		if (descriptionLabel!=null) {
			Point dsize = descriptionLabel.computeSize(width, SWT.DEFAULT, flush);
			height += dsize.y;
			height += vspacing;
		}
		if (infoLink != null) {
			Point isize = infoLinkLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
			height += isize.y;
		}
		return new Point(width, height);
	}
	protected void layout(Composite parent, boolean flush) {
		int width = parent.getClientArea().width;
		int height = parent.getClientArea().height;
		
		int x = hmargin;
		int y = vmargin;
		
		Point csize = checkbox.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		checkbox.setBounds(0, 0, csize.x, csize.y);
		x += csize.x + checkboxSpacing;
		int dataWidth = width - x;
		
		Point nsize = nameLabel.computeSize(dataWidth, SWT.DEFAULT, flush);
		nameLabel.setBounds(x, y, dataWidth, nsize.y);
		y += nsize.y + vspacing;
		
		Point psize = providerLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		providerLabel.setBounds(x, y, psize.x, psize.y);
		y += psize.y + vspacing;

		Point ssize = sizeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
		sizeLabel.setBounds(x, y, ssize.x, ssize.y);
		y += ssize.y + vspacing;

		Point dsize = descriptionLabel.computeSize(dataWidth, SWT.DEFAULT, flush);
		descriptionLabel.setBounds(x, y, dataWidth, dsize.y);
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
	providerLabel = factory.createLabel(section, getProviderName());
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

public String getProviderName() {
	return provider;
}

public void setProviderName(String providerName) {
	this.provider = "by "+providerName;
	if (providerLabel!=null) {
		providerLabel.setText(providerName);
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

