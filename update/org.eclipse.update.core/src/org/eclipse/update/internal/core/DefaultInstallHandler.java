package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.jar.JarEntry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.JarContentReference.ContentSelector;
import org.eclipse.update.core.model.InstallAbortedException;

/**
 * Default Implementation of InstallHandler
 */
public class DefaultInstallHandler extends BaseInstallHandler {

	/*
	 * @see IInstallHandler#nonPluginDataDownloaded(INonPluginEntry[], IVerificationListener)
	 */
	public void nonPluginDataDownloaded(
		INonPluginEntry[] nonPluginData,
		IVerificationListener listener)
		throws CoreException {

		// verify non-plugin archives. The DefaultInstallHandler assumes
		// the verifier associated with the feature is able to verify the
		// data archives.
		if (nonPluginData == null || nonPluginData.length == 0)
			return;

		this.nonPluginEntries = nonPluginData;
		IFeatureContentProvider provider = this.feature.getFeatureContentProvider();
		IVerifier verifier = provider.getVerifier();
		if (verifier == null)
			return;

		for (int i = 0; i < this.nonPluginEntries.length; i++) {
			ContentReference[] archives =
				provider.getNonPluginEntryArchiveReferences(nonPluginEntries[i], this.monitor);
			IVerificationResult result;
			for (int j = 0; j < archives.length; j++) {

				// see if the data entry is a jar
				ContentReference archive = archives[j];
				if (!(archives[j] instanceof JarContentReference)
					&& archives[j].getIdentifier().endsWith(".jar")) {
					try {
						archive =
							new JarContentReference(archives[j].getIdentifier(), archives[j].asFile());
					} catch (IOException e) {
					}
				}

				result = verifier.verify(this.feature, archive, false, this.monitor);
				if (result != null)
					promptForVerification(result, listener);
			}
		}
	}

	/*
	 * @see IInstallHandler#completeInstall(IFeatureContentConsumer)
	 */
	public void completeInstall(IFeatureContentConsumer consumer)
		throws CoreException {

		// plugins have been installed. Check to see if we have any
		// non-plugin entries that need to be handled.
		if (this.nonPluginEntries == null || this.nonPluginEntries.length == 0)
			return;

		// install non-plugin archives
		IFeatureContentProvider provider = this.feature.getFeatureContentProvider();
		for (int i = 0; i < this.nonPluginEntries.length; i++) {
			ContentReference[] archive =
				provider.getNonPluginEntryArchiveReferences(nonPluginEntries[i], this.monitor);
			IContentConsumer nonPluginConsumer = consumer.open(nonPluginEntries[i]);
			for (int j = 0; j < archive.length; j++) {
				String id = archive[j].getIdentifier();
				if (id.endsWith(".jar")) {
					// the non-plugin archive is a jar. Unpack it into
					// a directory constructed using the archive id
					try {
						final String prefix = id.substring(0, id.length() - 4) + "/";
						JarContentReference jarRef = new JarContentReference("", archive[j].asFile());
						ContentSelector selector = new ContentSelector() {
							public String defineIdentifier(JarEntry entry) {
								if (entry == null)
									return null;
								else
									return prefix + entry.getName();
							}
						};
						ContentReference[] entries = jarRef.peek(selector, this.monitor);
						for (int k = 0; k < entries.length; k++) {
							nonPluginConsumer.store(entries[k], this.monitor);
						}
					} catch (IOException e) {
						throw Utilities
							.newCoreException(Policy.bind("JarVerificationService.CancelInstall", id),
						//$NON-NLS-1$
						e);
					}

				} else {
					// the non-plugin archive is not a jar. Install it asis.
					nonPluginConsumer.store(archive[j], this.monitor);
				}
			}
			nonPluginConsumer.close();
		}
	}

	/*
	 * 
	 */
	private void promptForVerification(
		IVerificationResult verificationResult,
		IVerificationListener listener)
		throws CoreException {

		if (listener == null)
			return;
		int result = listener.prompt(verificationResult);

		if (result == IVerificationListener.CHOICE_ABORT) {
			Exception e = verificationResult.getVerificationException();
			throw new InstallAbortedException(Policy.bind("JarVerificationService.CancelInstall"),e);
		}
		if (result == IVerificationListener.CHOICE_ERROR) {
			throw Utilities
				.newCoreException(Policy.bind("JarVerificationService.UnsucessfulVerification"),
			//$NON-NLS-1$
			verificationResult.getVerificationException());
		}

		return;
	}
}