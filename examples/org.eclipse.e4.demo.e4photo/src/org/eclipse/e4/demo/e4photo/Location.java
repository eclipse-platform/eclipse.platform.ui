package org.eclipse.e4.demo.e4photo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class Location {

	private Browser browser;
	private Composite browserParent;

	public Location(Composite parent) {
		parent.setLayout(new FillLayout());
		parent.setData("org.eclipse.e4.ui.css.id", "location");
		browserParent = parent;
	}

	public void setInput(Exif input) {
		Exif exif = (Exif) input;

		// Create Browser widget only when we have content to show
		// so that we can control background color when there is no content
		if (exif == null || exif.getGpsLatitude() == null) {
			if (browser != null) {
				browser.dispose();
				browser = null;
			}
		} else {
			if (browser == null) {
				browser = new Browser(browserParent, SWT.NONE);
				browserParent.layout();
			}
			browser.setUrl("http://maps.google.com/maps?q="
					+ exif.getGpsLatitude() + "+" + exif.getGpsLongitude());
		}
	}

}
