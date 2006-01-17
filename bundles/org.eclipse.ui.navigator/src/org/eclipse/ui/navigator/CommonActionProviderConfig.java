package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * 
 * Provides access to information required for the initialization of
 * CommonActionProviders.
 * 
 * <p>
 * See the documentation of the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point and {@link CommonActionProvider} for more information on
 * declaring {@link CommonActionProvider}s.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class CommonActionProviderConfig {

	private String extensionId;

	private ICommonViewerSite commonViewerSite;

	private INavigatorContentService contentService;

	private StructuredViewer structuredViewer;

	/**
	 * Create a config element for the initialization of Common Action
	 * Providers.
	 * 
	 * @param anExtensionId
	 *            The unique identifier of the associated content extension or
	 *            the top-level action provider. <b>May NOT be null.</b>
	 * @param aCommonViewerSite
	 *            The common viewer site may be used to access information
	 *            about the part for which the instantiated CommonActionProvider
	 *            will be used. <b>May NOT be null.</b>
	 * @param aContentService
	 *            The associated content service to allow coordination with
	 *            content extensions via the IExtensionStateModel. Clients may
	 *            access the content providers and label providers as necessary
	 *            also to render labels or images in their UI. <b>May NOT be
	 *            null.</b>
	 * @param aStructuredViewer
	 *            The viewer control that will use the instantiated Common
	 *            Action Provider. <b>May NOT be null.</b>
	 */
	public CommonActionProviderConfig(String anExtensionId,
			ICommonViewerSite aCommonViewerSite, INavigatorContentService aContentService,
			StructuredViewer aStructuredViewer) {
		Assert.isNotNull(anExtensionId);
		Assert.isNotNull(aContentService);
		Assert.isNotNull(aCommonViewerSite);
		Assert.isNotNull(aStructuredViewer);
		extensionId = anExtensionId;
		commonViewerSite = aCommonViewerSite;
		contentService = aContentService;
		structuredViewer = aStructuredViewer;

	}

	/**
	 * By default, the extension state model returned is for the associated
	 * content extension (if this is NOT a top-level action provider).
	 * Otherwise, clients may use
	 * {@link INavigatorContentService#findStateModel(String)} to locate the
	 * state model of another content extension.
	 * 
	 * @return The extension state model of the associated Content Extension (if
	 *         any) or a state model specifically for this
	 *         ICommonActionProvider.
	 * @see IExtensionStateModel
	 */
	public IExtensionStateModel getExtensionStateModel() {
		return contentService.findStateModel(getExtensionId());
	}

	/**
	 * 
	 * @return The unique identifier of the associated content extension or the
	 *         top-level Common Action Provider.
	 */
	public String getExtensionId() {
		return extensionId;
	}

	/**
	 * 
	 * @return The associated content service for the instantiated Common Action
	 *         Provider.
	 */
	public INavigatorContentService getContentService() {
		return contentService;
	}

	/**
	 * 
	 * @return The associated structured viewer for the instantiated Common
	 *         Action Provider.
	 */
	public StructuredViewer getStructuredViewer() {
		return structuredViewer;
	}
 
	/**
	 * 
	 * @return The ICommonViewerSite from the CommonViewer. 
	 */
	public ICommonViewerSite getViewSite() {
		return commonViewerSite;
	}
}
