/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.runtime.PlatformMessages;

// Runtime plugin message catalog
public class Messages extends PlatformMessages {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.runtime.messages"; //$NON-NLS-1$

	public static String ok;

	// application
	public static String application_notFound;
	public static String application_returned;
	public static String application_noIdFound;
	public static String application_invalidExtension;

	// plugins
	public static String plugin_extDefNotFound;
	public static String plugin_extDefNoClass;
	public static String plugin_deactivatedLoad;
	public static String plugin_loadClassError;
	public static String plugin_instantiateClassError;
	public static String plugin_initObjectError;
	public static String plugin_bundleNotFound;
	public static String plugin_notPluginClass;
	public static String plugin_startupProblems;
	public static String plugin_shutdownProblems;
	public static String plugin_pluginDisabled;
	public static String plugin_unableToResolve;
	public static String plugin_mismatchRuntime;
	public static String plugin_delegatingLoaderTrouble;
	public static String plugin_eventListenerError;

	// parsing/resolve
	public static String parse_error;
	public static String parse_errorProcessing;
	public static String parse_errorNameLineColumn;
	public static String parse_extPointUnknown;
	public static String parse_extPointDisabled;
	public static String parse_prereqDisabled;
	public static String parse_unsatisfiedPrereq;
	public static String parse_prereqLoop;
	public static String parse_registryProblems;
	public static String parse_fragmentMissingAttr;
	public static String parse_fragmentMissingIdName;
	public static String parse_pluginMissingAttr;
	public static String parse_pluginMissingIdName;
	public static String parse_unknownTopElement;
	public static String parse_initializationTrouble;
	public static String parse_internalStack;
	public static String parse_validMatch;
	public static String parse_validExport;
	public static String parse_missingFragmentPd;
	public static String parse_unsatisfiedOptPrereq;
	public static String parse_prereqOptLoop;
	public static String parse_unknownLibraryType;
	public static String parse_duplicatePlugin;
	public static String parse_unknownEntry;
	public static String parse_nullPluginIdentifier;
	public static String parse_nullFragmentIdentifier;
	public static String parse_missingPluginName;
	public static String parse_missingPluginId;
	public static String parse_missingPluginVersion;
	public static String parse_missingFPName;
	public static String parse_missingFPVersion;
	public static String parse_postiveMajor;
	public static String parse_postiveMinor;
	public static String parse_postiveService;
	public static String parse_emptyPluginVersion;
	public static String parse_separatorStartVersion;
	public static String parse_separatorEndVersion;
	public static String parse_doubleSeparatorVersion;
	public static String parse_oneElementPluginVersion;
	public static String parse_fourElementPluginVersion;
	public static String parse_numericMajorComponent;
	public static String parse_numericMinorComponent;
	public static String parse_numericServiceComponent;
	public static String parse_badPrereqOnFrag;
	public static String parse_duplicateFragment;
	public static String parse_duplicateLib;

	// new parsing messages
	public static String parse_problems;
	public static String parse_missingAttribute;
	public static String parse_missingAttributeLine;
	public static String parse_unknownAttribute;
	public static String parse_unknownAttributeLine;
	public static String parse_unknownElement;
	public static String parse_unknownElementLine;
	public static String parse_xmlParserNotAvailable;
	public static String parse_failedParsingManifest;


	// metadata
	public static String meta_appNotInit;
	public static String meta_instanceDataUnspecified;
	public static String meta_instanceDataAlreadySpecified;
	public static String meta_keyringFileAlreadySpecified;
	public static String meta_noDataModeSpecified;
	public static String meta_authFormatChanged;
	public static String meta_couldNotCreate;
	public static String meta_exceptionParsingLog;
	public static String meta_failCreateLock;
	public static String meta_inUse;
	public static String meta_notDir;
	public static String meta_platform;
	public static String meta_pluginProblems;
	public static String meta_readonly;
	public static String meta_readPlatformMeta;
	public static String meta_unableToReadAuthorization;
	public static String meta_unableToWriteAuthorization;
	public static String meta_writePlatformMeta;
	public static String meta_invalidRegDebug;
	public static String meta_infoRegDebug;
	public static String meta_unableToDeleteCache;
	public static String meta_writeVersion;
	public static String meta_versionCheckRun;
	public static String meta_checkVersion;
	public static String meta_fileManagerInitializationFailed;

	// Extension Registry
	public static String meta_registryCacheWriteProblems;
	public static String meta_registryCacheReadProblems;
	public static String meta_regCacheIOExceptionWriting;
	public static String meta_regCacheIOExceptionReading;
	public static String meta_registryCacheEOFException;
	public static String meta_registryCacheInconsistent;
	public static String meta_unableToWriteRegistry;
	public static String meta_unableToCreateCache;
	public static String meta_unableToReadCache;
	public static String meta_unableToCreateRegDebug;
	public static String meta_unableToWriteDebugRegistry;

	// URL
	public static String url_noaccess;
	public static String url_createConnection;
	public static String url_invalidURL;
	public static String url_badVariant;
	public static String url_resolveFragment;
	public static String url_resolvePlugin;

	// Preferences
	public static String preferences_errorReading;
	public static String preferences_errorWriting;
	public static String preferences_fileNotFound;
	public static String preferences_incompatible;
	public static String preferences_invalidProperty;
	public static String preferences_validate;
	public static String preferences_validationException;

	public static String preferences_removeRoot;
	public static String preferences_classCast;
	public static String preferences_removedNode;
	public static String preferences_syncException;
	public static String preferences_loadException;
	public static String preferences_saveException;
	public static String preferences_exportProblems;
	public static String preferences_importProblems;
	public static String preferences_missingScopeAttribute;
	public static String preferences_failedDelete;
	public static String preferences_noLocation;
	public static String preferences_invalidParentClass;
	public static String preferences_applyProblems;
	public static String preferences_saveProblems;
	public static String preferences_invalidExtensionSuperclass;
	public static String preferences_removeExported;

	// Job Manager and Locks
	public static String jobs_blocked0;
	public static String jobs_blocked1;
	public static String jobs_internalError;
	public static String jobs_waitFamSub;

	// Adapter manager
	public static String adapters_badAdapterFactory;

	// Content type manager
	public static String content_invalidContentDescriber;
	public static String content_errorReadingContents;
	public static String content_parserConfiguration;
	public static String content_badInitializationData;
	public static String content_missingIdentifier;
	public static String content_missingName;
	public static String content_errorSavingSettings;

	// Product
	public static String provider_invalid_general;
	public static String provider_invalid;
	public static String product_notFound;

	static {
		// load message values from bundle file
		PlatformMessages.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object binding) {
		return PlatformMessages.bind(message, new Object[] {binding});
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object binding1, Object binding2) {
		return PlatformMessages.bind(message, new Object[] {binding1, binding2});
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object[] bindings) {
		return PlatformMessages.bind(message, bindings);
	}
}