package org.eclipse.update.internal.ui.properties;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;
import org.eclipse.update.internal.ui.parts.SWTUtil;

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
							Program.launch(url.getProtocol() + ":" + url.getFile());
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
