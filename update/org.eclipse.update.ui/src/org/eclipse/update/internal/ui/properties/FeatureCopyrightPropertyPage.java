package org.eclipse.update.internal.ui.properties;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;

/**
 * @see PropertyPage
 */
public class FeatureCopyrightPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public FeatureCopyrightPropertyPage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent) {
		try {
			IFeatureAdapter adapter = (IFeatureAdapter) getElement();
			IFeature feature = adapter.getFeature(null);

			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout());

			Label label = new Label(composite, SWT.WRAP);
			GridData gd =
				new GridData(
					GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
			gd.widthHint = computeWidthLimit(label, 80);
			label.setLayoutData(gd);

			IURLEntry copyright = feature.getCopyright();
			String annotation = (copyright != null) ? copyright.getAnnotation() : null;

			if (annotation != null && annotation.length() > 0) {
				label.setText(annotation);
				final URL url = copyright.getURL();
				String filename = (url != null) ? url.getFile() : null;
				if (filename != null
					&& (filename.endsWith(".htm") || filename.endsWith(".html"))) {
					Button button = new Button(composite, SWT.PUSH);
					button.setText("Show in Browser");
					button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
					SWTUtil.setButtonDimensionHint(button);
					button.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							String urlName = url.getProtocol() + ":" + url.getFile();
							UpdateUI.showURL(urlName);
						}
					});
				}
			} else {
				label.setText("Feature does not contain a copyright statement");
			}
			Dialog.applyDialogFont(parent);
		} catch (CoreException e) {
		}
		return null;
	}
	
	private int computeWidthLimit(Label label, int nchars) {
		GC gc = new GC(label);
		gc.setFont(label.getFont());
		FontMetrics fontMetrics= gc.getFontMetrics();
		gc.dispose();
		return Dialog.convertWidthInCharsToPixels(fontMetrics, nchars);
	}

}
