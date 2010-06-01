/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.about;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentFactory;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 * 
 */
public class BundleSigningInfo {

	private Composite composite;
	private Text date;
	private StyledText certificate;
	private AboutBundleData data;

	public BundleSigningInfo() {
	}

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
			GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			date = new Text(composite, SWT.READ_ONLY);
			GC gc = new GC(date);
			gc.setFont(JFaceResources.getDialogFont());
			Point size = gc.stringExtent(DateFormat.getDateTimeInstance()
					.format(new Date()));
			data.widthHint = size.x;
			gc.dispose();
			date.setText(WorkbenchMessages.BundleSigningTray_Working);
			date.setLayoutData(data);
		}
		// signer
		{
			Label label = new Label(composite, SWT.NONE);
			label
					.setText(WorkbenchMessages.BundleSigningTray_Signing_Certificate);
			GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true,
					false);
			data.horizontalSpan = 2;
			data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.horizontalSpan = 2;
			certificate = new StyledText(composite, SWT.READ_ONLY | SWT.MULTI
					| SWT.WRAP);
			certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
			certificate.setLayoutData(data);
		}
		Dialog.applyDialogFont(composite);

		startJobs(); // start the jobs that will prime the content
		return composite;
	}

	/**
	 * 
	 */
	private void startJobs() {
		if (!isOpen())
			return;
		certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
		date.setText(WorkbenchMessages.BundleSigningTray_Working);
		final BundleContext bundleContext = WorkbenchPlugin.getDefault()
				.getBundleContext();
		final ServiceReference factoryRef = bundleContext
				.getServiceReference(SignedContentFactory.class.getName());
		if (factoryRef == null) {
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.WARNING,
									WorkbenchPlugin.PI_WORKBENCH,
									WorkbenchMessages.BundleSigningTray_Cant_Find_Service),
							StatusManager.LOG);
			return;
		}

		final SignedContentFactory contentFactory = (SignedContentFactory) bundleContext
				.getService(factoryRef);
		if (contentFactory == null) {
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.WARNING,
									WorkbenchPlugin.PI_WORKBENCH,
									WorkbenchMessages.BundleSigningTray_Cant_Find_Service),
							StatusManager.LOG);
			return;
		}

		final AboutBundleData myData = data;
		final Job signerJob = new Job(NLS.bind(
				WorkbenchMessages.BundleSigningTray_Determine_Signer_For,
				myData.getId())) {

			protected IStatus run(IProgressMonitor monitor) {
				final String[] signingInfo = new String[] {
						WorkbenchMessages.BundleSigningTray_Unsigned,
						WorkbenchMessages.BundleSigningTray_Unsigned };
				try {
					if (myData != data)
						return Status.OK_STATUS;
					SignedContent signedContent = contentFactory
							.getSignedContent(myData.getBundle());
					if (myData != data)
						return Status.OK_STATUS;
					SignerInfo[] signers = signedContent.getSignerInfos();
					if (!isOpen() && BundleSigningInfo.this.data == myData)
						return Status.OK_STATUS;

					if (signers.length > 0) {
						Properties[] certs = parseCerts(signers[0]
								.getCertificateChain());
						if (certs.length == 0)
							signingInfo[0] = WorkbenchMessages.BundleSigningTray_Unknown;
						else {
							StringBuffer buffer = new StringBuffer();
							for (Iterator i = certs[0].entrySet().iterator(); i
									.hasNext();) {
								Map.Entry entry = (Entry) i.next();
								buffer.append(entry.getKey());
								buffer.append('=');
								buffer.append(entry.getValue());
								if (i.hasNext())
									buffer.append('\n');
							}
							signingInfo[0] = buffer.toString();
						}

						Date signDate = signedContent
								.getSigningTime(signers[0]);
						if (signDate != null)
							signingInfo[1] = DateFormat.getDateTimeInstance().format(
									signDate);
						else
							signingInfo[1] = WorkbenchMessages.BundleSigningTray_Unknown;
					}
				} catch (IOException e) {
					// default "unsigned info" is ok for the user, but log the
					// problem.
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, e.getMessage(),
									e), StatusManager.LOG);
				} catch (GeneralSecurityException e) {
					// default "unsigned info is ok, but log the problem.
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, e.getMessage(),
									e), StatusManager.LOG);
				}
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						// check to see if the tray is still visible
						// and if we're still looking at the same item
						if (!isOpen() && BundleSigningInfo.this.data != myData)
							return;
						certificate.setText(signingInfo[0]);
						date.setText(signingInfo[1]);
					}
				});

				return Status.OK_STATUS;
			}
		};
		signerJob.setSystem(true);
		signerJob.belongsTo(signerJob);
		signerJob.schedule();

		Job cleanup = new Job(
				WorkbenchMessages.BundleSigningTray_Unget_Signing_Service) {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					getJobManager().join(signerJob, monitor);
				} catch (OperationCanceledException e) {
				} catch (InterruptedException e) {
				}
				bundleContext.ungetService(factoryRef);
				return Status.OK_STATUS;
			}
		};
		cleanup.setSystem(true);
		cleanup.schedule();

	}

	/**
	 * 
	 */
	private boolean isOpen() {
		return certificate != null && !certificate.isDisposed();
	}

	private Properties[] parseCerts(Certificate[] chain) {
		List certs = new ArrayList(chain.length);
		for (int i = 0; i < chain.length; i++) {
			if (!(chain[i] instanceof X509Certificate))
				continue;
			Map cert = parseCert(((X509Certificate) chain[i]).getSubjectDN()
					.getName());
			if (cert != null)
				certs.add(cert);
		}
		return (Properties[]) certs.toArray(new Properties[certs.size()]);

	}

	/**
	 * @param certString
	 * @return
	 */
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
