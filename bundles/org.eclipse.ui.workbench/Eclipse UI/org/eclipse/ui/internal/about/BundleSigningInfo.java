/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474273
 *******************************************************************************/

package org.eclipse.ui.internal.about;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignerInfo;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;

/**
 * @since 3.3
 */
public class BundleSigningInfo {

	private Composite composite;
	private Text date;
	private Label signingType;
	private StyledText certificate;
	private AboutBundleData data;

	public void setData(AboutBundleData data) {
		this.data = data;
		startJobs();
	}

	public Control createContents(Composite parent) {

		composite = new Composite(parent, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		// date
		{
			Label label = new Label(composite, SWT.NONE);
			label.setText(WorkbenchMessages.BundleSigningTray_Signing_Date);
			GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			label.setLayoutData(data);

			date = new Text(composite, SWT.READ_ONLY);
			data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			GC gc = new GC(date);
			gc.setFont(JFaceResources.getDialogFont());
			Point size = gc.stringExtent(DateFormat.getDateTimeInstance().format(new Date()));
			data.widthHint = size.x;
			gc.dispose();
			date.setText(WorkbenchMessages.BundleSigningTray_Working);
			date.setLayoutData(data);
		}

		// signer
		{
			Label label = new Label(composite, SWT.NONE);
			label.setText(WorkbenchMessages.BundleSigningTray_SigningType);
			GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			label.setLayoutData(data);

			signingType = new Label(composite, SWT.NONE);
			data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			signingType.setData(data);
			signingType.setText(WorkbenchMessages.BundleSigningTray_Working);
		}

		certificate = new StyledText(composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		certificate.setLayoutData(data);
		Dialog.applyDialogFont(composite);

		startJobs(); // start the jobs that will prime the content
		return composite;
	}

	private void startJobs() {
		if (!isOpen())
			return;
		certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
		date.setText(WorkbenchMessages.BundleSigningTray_Working);

		final AboutBundleData myData = data;
		final Job signerJob = Job.create(
				NLS.bind(WorkbenchMessages.BundleSigningTray_Determine_Signer_For, myData.getId()),
				(IJobFunction) monitor -> {
					if (myData != data)
						return Status.OK_STATUS;
					SignedContent signedContent = myData.getSignedContent();
					if (signedContent == null) {
						StatusManager.getManager().handle(new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
								WorkbenchMessages.BundleSigningTray_Cant_Find_Service), StatusManager.LOG);
						return Status.OK_STATUS;
					}
					if (myData != data)
						return Status.OK_STATUS;
					SignerInfo[] signers = signedContent.getSignerInfos();
					final String signerText, dateText, signingTypeText;
					if (!isOpen() && BundleSigningInfo.this.data == myData)
						return Status.OK_STATUS;

					if (signers.length == 0) {
						AboutBundleData.ExtendedSigningInfo info = data.getInfo();
						Bundle bundle = data.getBundle();
						if (info != null && info.isSigned(bundle)) {
							signerText = info.getSigningDetails(bundle);
							dateText = DateFormat.getDateTimeInstance().format(info.getSigningTime(bundle));
							signingTypeText = info.getSigningType(bundle);
						} else {
							signerText = WorkbenchMessages.BundleSigningTray_Unsigned;
							dateText = WorkbenchMessages.BundleSigningTray_Unsigned;
							signingTypeText = WorkbenchMessages.BundleSigningTray_Unsigned;
						}
					} else {
						Properties[] certs = parseCerts(signers[0].getCertificateChain());
						if (certs.length == 0)
							signerText = WorkbenchMessages.BundleSigningTray_Unknown;
						else {
							StringBuilder buffer = new StringBuilder();
							for (Iterator<Entry<Object, Object>> i = certs[0].entrySet().iterator(); i.hasNext();) {
								Entry<Object, Object> entry = i.next();
								buffer.append(entry.getKey());
								buffer.append('=');
								buffer.append(entry.getValue());
								if (i.hasNext())
									buffer.append('\n');
							}
							signerText = buffer.toString();
						}

						Date signDate = signedContent.getSigningTime(signers[0]);
						if (signDate != null)
							dateText = DateFormat.getDateTimeInstance().format(signDate);
						else
							dateText = WorkbenchMessages.BundleSigningTray_Unknown;
						signingTypeText = WorkbenchMessages.BundleSigningTray_X509Certificate;
					}

					PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
						// check to see if the tray is still visible
						// and if
						// we're still looking at the same item
						if (!isOpen() && BundleSigningInfo.this.data != myData)
							return;
						certificate.setText(signerText);
						date.setText(dateText);
						signingType.setText(signingTypeText);
						signingType.getParent().layout(true);
					});
					return Status.OK_STATUS;
				});
		signerJob.setSystem(true);
		signerJob.belongsTo(signerJob);
		signerJob.schedule();
	}

	private boolean isOpen() {
		return certificate != null && !certificate.isDisposed();
	}

	private Properties[] parseCerts(Certificate[] chain) {
		List<Properties> certs = new ArrayList<>(chain.length);
		for (Certificate e : chain) {
			if (!(e instanceof X509Certificate)) {
				continue;
			}
			Properties cert = parseCert(((X509Certificate) e).getSubjectX500Principal().getName());
			if (cert != null)
				certs.add(cert);
		}
		return certs.toArray(new Properties[certs.size()]);

	}

	private Properties parseCert(String certString) {
		StringTokenizer toker = new StringTokenizer(certString, ","); //$NON-NLS-1$
		Properties cert = new Properties();
		while (toker.hasMoreTokens()) {
			String pair = toker.nextToken();
			int idx = pair.indexOf('=');
			if (idx > 0 && idx < pair.length() - 2) {
				String key = pair.substring(0, idx).trim();
				String value = pair.substring(idx + 1).trim();
				if (value.length() > 2) {
					if (value.charAt(0) == '\"')
						value = value.substring(1);

					if (value.charAt(value.length() - 1) == '\"')
						value = value.substring(0, value.length() - 1);
				}
				cert.setProperty(key, value);
			}
		}
		return cert;
	}

	public void dispose() {
		composite.dispose();
	}
}
