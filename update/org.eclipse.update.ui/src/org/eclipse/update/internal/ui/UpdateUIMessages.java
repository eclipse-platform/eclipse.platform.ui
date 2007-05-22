/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class UpdateUIMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.update.internal.ui.UpdateUIPluginResources";//$NON-NLS-1$

	private UpdateUIMessages() {
		// Do not instantiate
	}

	public static String RestartTitle;
	public static String RestartMessage;
	public static String OptionalRestartMessage;
	public static String ApplicationInRestartDialog;
	public static String ApplyChanges;
	public static String ConfigurationManagerAction_title;
	public static String ConfigurationManagerWindow_fileMenu;
	public static String ConfigurationManagerWindow_properties;
	public static String ConfigurationManagerWindow_close;
	public static String EditBookmarksAction_edit;
	public static String EditBookmarksAction_title;
	public static String InstallWizardAction_title;
	public static String InstallWizard_retryTitle;
	public static String InstallWizard_retry;
	public static String TargetPage_Feature_Size;
	public static String TargetPage_Feature_Version;
	public static String TargetPage_FeatureNameColumn;
	public static String TargetPage_InstallationDirectoryColumn;
	public static String WebBookmarksDialog_label;
	public static String WebBookmarksDialog_add;
	public static String WebBookmarksDialog_new;
	public static String WebBookmarksDialog_edit;
	public static String WebBookmarksDialog_editTitle;
	public static String WebBookmarksDialog_remove;
	public static String DiscoveryFolder_name;
	public static String FeatureAdapter_failure;
	public static String SiteBookmark_connecting;
	public static String SiteBookmark_downloading;
	public static String MainPreferencePage_checkSignature;
	public static String MainPreferencePage_automaticallyChooseMirror;
	public static String MainPreferencePage_historySize;
	public static String MainPreferencePage_invalidHistorySize;
	public static String MainPreferencePage_updateVersions;
	public static String MainPreferencePage_updateVersions_equivalent;
	public static String MainPreferencePage_updateVersions_compatible;
	public static String MainPreferencePage_updatePolicy;
	public static String MainPreferencePage_updatePolicyURL;
	public static String MainPreferencePage_proxyGroup;
	public static String MainPreferencePage_enableHttpProxy;
	public static String MainPreferencePage_httpProxyHost;
	public static String MainPreferencePage_httpProxyPort;
	public static String MainPreferencePage_digitalSignature_title;
	public static String MainPreferencePage_digitalSignature_message;
	public static String MainPreferencePage_invalidPort;
	public static String UpdateSettingsPreferencePage_description;
	public static String UpdateSettingsPreferencePage_label;
	public static String UpdateSettingsPreferencePage_invalid;
	public static String ConfigurationActivitiesPropertyPage_label;
	public static String ConfiguredSitePropertyPage_path;
	public static String ConfiguredSitePropertyPage_type;
	public static String ConfiguredSitePropertyPage_enabled;
	public static String ConfiguredSitePropertyPage_yes;
	public static String ConfiguredSitePropertyPage_no;
	public static String ConfiguredSitePropertyPage_private;
	public static String ConfiguredSitePropertyPage_extension;
	public static String ConfiguredSitePropertyPage_product;
	public static String ConfiguredSitePropertyPage_unknown;
	public static String FeatureCopyrightPropertyPage_showInBrowser;
	public static String FeatureCopyrightPropertyPage_noCopyright;
	public static String FeatureGeneralPropertyPage_name;
	public static String FeatureGeneralPropertyPage_id;
	public static String FeatureGeneralPropertyPage_version;
	public static String FeatureGeneralPropertyPage_provider;
	public static String FeatureGeneralPropertyPage_size;
	public static String FeatureGeneralPropertyPage_platforms;
	public static String FeatureGeneralPropertyPage_ws;
	public static String FeatureGeneralPropertyPage_arch;
	public static String FeatureGeneralPropertyPage_nl;
	public static String FeatureGeneralPropertyPage_all;
	public static String FeatureGeneralPropertyPage_os;
	public static String FeatureGeneralPropertyPage_desc;
	public static String FeatureGeneralPropertyPage_Kilobytes;
	public static String FeatureLicensePropertyPage_showInBrowser;
	public static String FeatureLicensePropertyPage_noLicense;
	public static String FeatureStatusPropertyPage_pendingChanges;
	public static String FeatureStatusPropertyPage_reason;
	public static String FeatureStatusPropertyPage_missingOptional;
	public static String FeatureStatusPropertyPage_missing;
	public static String FeatureStatusPropertyPage_goodConfiguration;
	public static String JarVerificationDialog_wtitle;
	public static String JarVerificationDialog_Title;
	public static String JarVerificationDialog_Verification;
	public static String JarVerificationDialog_ComponentNotInstalled;
	public static String JarVerificationDialog_AboutToInstall_File;
	public static String JarVerificationDialog_NotDigitallySigned_File;
	public static String JarVerificationDialog_CannotVerifyProvider_File;
	public static String JarVerificationDialog_CorruptedContent_File;
	public static String JarVerificationDialog_SignedComponent_File;
	public static String JarVerificationDialog_UnknownCertificate_File;
	public static String JarVerificationDialog_UnableToVerifyProvider_File;
	public static String JarVerificationDialog_ProviderKnown_File;
	public static String JarVerificationDialog_KnownCertificate_File;
	public static String JarVerificationDialog_AboutToInstall_Feature;
	public static String JarVerificationDialog_NotDigitallySigned_Feature;
	public static String JarVerificationDialog_CannotVerifyProvider_Feature;
	public static String JarVerificationDialog_CorruptedContent_Feature;
	public static String JarVerificationDialog_SignedComponent_Feature;
	public static String JarVerificationDialog_UnknownCertificate_Feature;
	public static String JarVerificationDialog_UnableToVerifyProvider_Feature;
	public static String JarVerificationDialog_ProviderKnown_Feature;
	public static String JarVerificationDialog_KnownCertificate_Feature;
	public static String JarVerificationDialog_Caution;
	public static String JarVerificationDialog_FileName;
	public static String JarVerificationDialog_FeatureName;
	public static String JarVerificationDialog_FeatureIdentifier;
	public static String JarVerificationDialog_Provider;
	public static String JarVerificationDialog_MayChooseToInstall;
	public static String JarVerificationDialog_MayChooseToContinue;
	public static String JarVerificationDialog_Install;
	public static String JarVerificationDialog_InstallAll;
	public static String JarVerificationDialog_Continue;
	public static String JarVerificationDialog_Cancel;
	public static String JarVerificationDialog_RootCA;
	public static String JarVerificationDialog_SubjectCA;
	public static String JarVerificationDialog_CertificateInfo;
	public static String UserVerificationDialog_PasswordRequired;
	public static String UserVerificationDialog_ConnectTo;
	public static String UserVerificationDialog_Password;
	public static String UserVerificationDialog_UserName;
	public static String ConfigurationPreviewForm_configDescription;
	public static String ConfigurationPreviewForm_install;
	public static String ConfigurationPreviewForm_AvailableTasks;
	public static String ConfigurationView_missingFeature;
	public static String ConfigurationView_current;
	public static String ConfigurationView_pending;
	public static String ConfigurationView_error;
	public static String ConfigurationView_collapseLabel;
	public static String ConfigurationView_collapseTooltip;
	public static String ConfigurationView_revertLabel;
	public static String ConfigurationView_installHistory;
	public static String ConfigurationView_extLocation;
	public static String ConfigurationView_uninstall;
	public static String ConfigurationView_unconfigureAndUninstall;
	public static String ConfigurationView_install;
	public static String ConfigurationView_anotherVersion;
	public static String ConfigurationView_findUpdates;
	public static String ConfigurationView_showNestedFeatures;
	public static String ConfigurationView_showNestedTooltip;
	public static String ConfigurationView_showInstall;
	public static String ConfigurationView_showInstallTooltip;
	public static String ConfigurationView_showDisabled;
	public static String ConfigurationView_showDisabledTooltip;
    public static String ConfigurationView_showActivitiesLabel;
	public static String ConfigurationView_new;
	public static String ConfigurationView_replaceWith;
	public static String ConfigurationView_revertPreviousLabel;
	public static String ConfigurationView_revertPreviousDesc;
	public static String ConfigurationView_updateLabel;
	public static String ConfigurationView_updateDesc;
	public static String ConfigurationView_detectedChanges;
	public static String ConfigurationView_detectedLabel;
	public static String ConfigurationView_detectedDesc;
	public static String ConfigurationView_installHistLabel;
	public static String ConfigurationView_installHistDesc;
	public static String ConfigurationView_activitiesLabel;
	public static String ConfigurationView_activitiesDesc;
	public static String ConfigurationView_enableLocDesc;
	public static String ConfigurationView_extLocLabel;
	public static String ConfigurationView_extLocDesc;
	public static String ConfigurationView_propertiesLabel;
	public static String ConfigurationView_installPropDesc;
	public static String ConfigurationView_replaceVersionLabel;
	public static String ConfigurationView_replaceVersionDesc;
	public static String ConfigurationView_enableFeatureDesc;
	public static String ConfigurationView_installOptionalLabel;
	public static String ConfigurationView_installOptionalDesc;
	public static String ConfigurationView_uninstallLabel;
	public static String ConfigurationView_uninstallDesc;
	public static String ConfigurationView_uninstallDesc2;
	public static String ConfigurationView_scanLabel;
	public static String ConfigurationView_scanDesc;
	public static String ConfigurationView_featurePropLabel;
	public static String ConfigurationView_featurePropDesc;
    public static String ConfigurationView_loading;
	public static String Actions_brokenConfigQuestion;
	public static String FeatureStateAction_disable;
	public static String FeatureStateAction_enable;
	public static String FeatureStateAction_disableQuestion;
	public static String FeatureStateAction_EnableQuestion;
	public static String FeatureStateAction_dialogTitle;
	public static String FeatureUninstallAction_uninstall;
	public static String FeatureUninstallAction_uninstallQuestion;
	public static String FeatureUninstallAction_dialogTitle;
	public static String FeatureUnconfigureAndUninstallAction_uninstall;
	public static String FeatureUnconfigureAndUninstallAction_question;
	public static String FeatureUnconfigureAndUninstallAction_dialogTitle;
	public static String SiteStateAction_dialogTitle;
	public static String SiteStateAction_disableLabel;
	public static String SiteStateAction_disableMessage;
	public static String SiteStateAction_enableLabel;
	public static String SiteStateAction_enableMessage;
	public static String FindUpdatesAction_updates;
	public static String InstallationHistoryAction_title;
	public static String InstallationHistoryAction_desc;
	public static String InstallationHistoryAction_activity;
	public static String InstallationHistoryAction_dateTime;
	public static String InstallationHistoryAction_target;
	public static String InstallationHistoryAction_action;
	public static String InstallationHistoryAction_status;
	public static String InstallationHistoryAction_errors;
	public static String NewExtensionLocationAction_selectExtLocation;
	public static String NewExtensionLocationAction_extInfoTitle;
	public static String NewExtensionLocationAction_extInfoMessage;
	public static String ActivitiesTableViewer_enabled;
	public static String ActivitiesTableViewer_featureInstalled;
	public static String ActivitiesTableViewer_featureRemoved;
	public static String ActivitiesTableViewer_siteInstalled;
	public static String ActivitiesTableViewer_siteRemoved;
	public static String ActivitiesTableViewer_disabled;
	public static String ActivitiesTableViewer_revert;
	public static String ActivitiesTableViewer_reconcile;
	public static String ActivitiesTableViewer_unknown;
	public static String ActivitiesTableViewer_date;
	public static String ActivitiesTableViewer_target;
	public static String ActivitiesTableViewer_action;
	public static String DeltaFeatureAdapter_shortName;
	public static String DeltaFeatureAdapter_longName;
	public static String DuplicateConflictsDialog_title;
	public static String DuplicateConflictsDialog_message;
	public static String DuplicateConflictsDialog_treeLabel;
	public static String InstallDeltaWizard_wtitle;
	public static String InstallDeltaWizard_processing;
	public static String InstallDeltaWizard_title;
	public static String InstallDeltaWizard_desc;
	public static String InstallDeltaWizard_label;
	public static String InstallDeltaWizard_delete;
	public static String InstallDeltaWizard_errors;
	public static String InstallDeltaWizard_message;
	public static String ModeSelectionPage_title;
	public static String ModeSelectionPage_desc;
	public static String ModeSelectionPage_updates;
	public static String ModeSelectionPage_updatesText;
	public static String ModeSelectionPage_newFeatures;
	public static String ModeSelectionPage_newFeaturesText;
	public static String MoreInfoGenerator_notInstalled;
	public static String MoreInfoGenerator_all;
	public static String MoreInfoGenerator_license;
	public static String MoreInfoGenerator_HTMLlicense;
	public static String MoreInfoGenerator_licenseAvailable;
	public static String MoreInfoGenerator_copyright;
	public static String MoreInfoGenerator_HTMLcopyright;
	public static String MoreInfoGenerator_copyrightAvailable;
	public static String MoreInfoGenerator_platforms;
	public static String MoreInfoGenerator_footprint;
	public static String MoreInfoGenerator_downloadSize;
	public static String MoreInfoGenerator_installSize;
	public static String MoreInfoGenerator_downloadTime;
	public static String MoreInfoGenerator_lessthanone;
	public static String MoreInfoGenerator_desc;
	public static String MoreInfoGenerator_moreInfo;
	public static String NewUpdateSiteDialog_name;
	public static String NewUpdateSiteDialog_url;
	public static String NewUpdateSiteDialog_error_nameOrUrlNotSpecified;
	public static String NewUpdateSiteDialog_error_duplicateName;
	public static String NewUpdateSiteDialog_error_duplicateUrl;
	public static String NewUpdateSiteDialog_error_incorrectUrl;
	public static String InstallWizard_isRunningTitle;
	public static String InstallWizard_isRunningInfo;
	public static String InstallWizard_jobName;
	public static String InstallWizard_download;
	public static String InstallWizard_downloadingFeatureJar;
	public static String InstallWizard_anotherJob;
	public static String InstallWizard_anotherJobTitle;
	public static String InstallWizard_wtitle;
	public static String InstallWizard_OptionalFeaturesPage_title;
	public static String InstallWizard_OptionalFeaturesPage_desc;
	public static String InstallWizard_OptionalFeaturesPage_treeLabel;
	public static String InstallWizard_OptionalFeaturesPage_selectAll;
	public static String InstallWizard_OptionalFeaturesPage_deselectAll;
	public static String InstallWizard_ReviewPage_counter;
	public static String InstallWizard_ReviewPage_title;
	public static String InstallWizard_ReviewPage_desc;
	public static String InstallWizard_ReviewPage_searching;
	public static String InstallWizard_ReviewPage_zeroUpdates;
	public static String InstallWizard_ReviewPage_zeroFeatures;
	public static String InstallWizard_ReviewPage_label;
	public static String InstallWizard_ReviewPage_selectAll;
	public static String InstallWizard_ReviewPage_deselectAll;
	public static String InstallWizard_ReviewPage_moreInfo;
	public static String InstallWizard_ReviewPage_properties;
	public static String InstallWizard_ReviewPage_selectRequired;
	public static String InstallWizard_ReviewPage_showStatus;
	public static String InstallWizard_ReviewPage_filterFeatures;
	public static String InstallWizard_ReviewPage_filterPatches;
	public static String InstallWizard_ReviewPage_filterOlderFeatures;
	public static String InstallWizard_ReviewPage_feature;
	public static String InstallWizard_ReviewPage_version;
	public static String InstallWizard_ReviewPage_provider;
	public static String InstallWizard_ReviewPage_prop;
	public static String InstallWizard_ReviewPage_invalid_long;
	public static String InstallWizard_ReviewPage_invalid_short;
	public static String InstallWizard_ReviewPage_cycle;
	public static String InstallWizard_LicensePage_accept;
	public static String InstallWizard_LicensePage_accept2;
	public static String InstallWizard_LicensePage_decline;
	public static String InstallWizard_LicensePage_decline2;
	public static String InstallWizard_LicensePage_desc;
	public static String InstallWizard_LicensePage_desc2;
	public static String InstallWizard_LicensePage_down;
	public static String InstallWizard_LicensePage_header;
	public static String InstallWizard_LicensePage_title;
	public static String InstallWizard_LicensePage_up;
	public static String InstallWizard_TargetPage_title;
	public static String InstallWizard_TargetPage_desc;
	public static String InstallWizard_TargetPage_jobsLabel;
	public static String InstallWizard_TargetPage_siteLabel;
	public static String InstallWizard_TargetPage_new;
	public static String InstallWizard_TargetPage_delete;
	public static String InstallWizard_TargetPage_requiredSpace;
	public static String InstallWizard_TargetPage_availableSpace;
	public static String InstallWizard_TargetPage_location;
	public static String InstallWizard_TargetPage_location_change;
	public static String InstallWizard_TargetPage_location_message;
	public static String InstallWizard_TargetPage_location_empty;
	public static String InstallWizard_TargetPage_location_exists;
	public static String InstallWizard_TargetPage_location_error_title;
	public static String InstallWizard_TargetPage_location_error_message;
	public static String InstallWizard_TargetPage_location_error_reason;
	public static String InstallWizard_TargetPage_size_KB;
	public static String InstallWizard_TargetPage_size_MB;
	public static String InstallWizard_TargetPage_size_GB;
	public static String InstallWizard_TargetPage_unknownSize;
	public static String InstallWizard_TargetPage_patchError;
	public static String InstallWizard_TargetPage_patchError2;
	public static String InstallWizard2_updateOperationHasFailed;
	public static String RevertConfigurationWizard_wtitle;
	public static String RevertConfigurationWizardPage_title;
	public static String RevertConfigurationWizardPage_desc;
	public static String RevertConfigurationWizardPage_label;
	public static String RevertConfigurationWizardPage_activities;
	public static String RevertConfigurationWizardPage_question;
	public static String Revert_ProblemDialog_title;
	public static String ShowActivitiesDialog_title;
	public static String ShowActivitiesDialog_date;
	public static String ShowActivitiesDialog_loc;
	public static String ShowActivitiesDialog_label;
	public static String SitePage_title;
	public static String SitePage_desc;
	public static String SitePage_label;
	public static String SitePage_addUpdateSite;
	public static String SitePage_addLocalSite;
	public static String SitePage_addLocalZippedSite;
	public static String SitePage_edit;
	public static String SitePage_remove;
	public static String SitePage_import;
	public static String SitePage_export;
	public static String SitePage_ignore;
	public static String SitePage_automaticallySelectMirrors;
	public static String SitePage_new;
	public static String SitePage_dialogEditLocal;
	public static String SitePage_dialogEditUpdateSite;
	public static String SitePage_connecting;
	public static String SitePage_remove_location_conf_title;
	public static String SitePage_remove_location_conf;
	public static String SearchRunner_connectionError;
	public static String LocalSiteSelector_dialogMessage;
	public static String LocalSiteSelector_dirInfoTitle;
	public static String LocalSiteSelector_dirInfoMessage;
	public static String LocalSiteSelector_dirDuplicateDefinition;
	public static String LocalSiteSelector_dialogMessagezip;
	public static String LocalSiteSelector_zipInfoTitle;
	public static String LocalSiteSelector_zipInfoMessage;
	public static String LocalSiteSelector_zipDuplicateDefinition;
	public static String LocalSiteSelector_dialogMessageImport;
	public static String LocalSiteSelector_importInfoTitle;
	public static String LocalSiteSelector_importInfoMessage;
	public static String LocalSiteSelector_dialogMessageExport;
	public static String InstallServlet_unknownServerURL;
	public static String InstallServlet_noFeatures;
	public static String InstallServlet_inProgress;
	public static String InstallServlet_incorrectURLFormat;
	public static String InstallServlet_contactWebmaster;
	public static String MissingFeature_provider;
    public static String MissingFeature_id;
	public static String MissingFeature_desc_unknown;
	public static String MissingFeature_desc_optional;
	public static String InstallDeltaWizard_reminder;
	public static String SwapFeatureWizard_title;
	public static String SwapFeatureWizardPage_title;
	public static String SwapFeatureWizardPage_desc;
	public static String SwapFeatureWizardPage_label;
	public static String MirrorsDialog_text;
	public static String MirrorsDialog_title;
	public static String AutomaticUpdatesJob_Updates;
	public static String AutomaticUpdatesJob_EclipseUpdates1;
	public static String AutomaticUpdatesJob_UpdatesAvailable;
	public static String AutomaticUpdatesJob_EclipseUpdates2;
	public static String AutomaticUpdatesJob_UpdatesDownloaded;
    
    public static String FeaturePage_optionalInstall_title;

	static {
		NLS.initializeMessages(BUNDLE_NAME, UpdateUIMessages.class);
	}

	public static String ConfigurationManagerWindow_searchTaskName;
	public static String FindUpdatesAction_trackedProgress;
	public static String FindUpdatesAction_allFeaturesSearch;
	public static String ReviewPage_validating;
}