/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.json.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tips.core.ITipManager;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.json.JsonTipProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A helper class to load providers from an internet location.
 *
 */
@SuppressWarnings("restriction")
public class ProviderLoader {

	/**
	 * This method loads provider information from the internet and stores it
	 * locally. This method should not be called in the user interface thread.
	 * 
	 * @param pManager      the tip manager
	 * @param baseURL       the location of the providers.json file
	 * @param stateLocation the place to store state
	 */
	public static void loadProviderData(ITipManager pManager, String baseURL, File stateLocation) {
		loadProviders(pManager, baseURL, stateLocation);
	}

	private static void loadProviders(ITipManager pManager, String pBaseURL, File stateLocation) {
		try {
			URL webFile = new URL(pBaseURL + "index.json");
			File target = new File(stateLocation, "index.json");
			try (InputStream in = webFile.openStream()) {
				Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			pManager.log(LogUtil.info("Internal Providers index file loaded to " + target.toPath()));
			createProviders(pManager, target, pBaseURL, stateLocation);
		} catch (Exception e) {
			String symbolicName = FrameworkUtil.getBundle(ProviderLoader.class).getSymbolicName();
			pManager.log(new Status(IStatus.ERROR, symbolicName, "Error loading provider file", e));
		}
	}

	private static void createProviders(ITipManager pManager, File pTarget, String pBaseURL, File stateLocation)
			throws Exception {
		try (FileReader reader = new FileReader(pTarget)) {
			JsonObject value = (JsonObject) new JsonParser().parse(reader);
			JsonArray providers = value.getAsJsonArray(JsonConstants.P_PROVIDER);
			providers.forEach(provider -> loadProvider(pManager, provider, pBaseURL, stateLocation));
		}
	}

	private static void loadProvider(ITipManager pManager, JsonElement pProvider, String pBaseURL, File userLocation) {
		JsonObject provider = pProvider.getAsJsonObject();
		String version = Util.getValueOrDefault(provider, "version", null);
		String location = Util.getValueOrDefault(provider, "location", null);
		String bundleName = Util.getValueOrDefault(provider, "require-bundle", null);
		if (version == null || bundleName == null) {
			logInvalidProvider(pManager, provider);
			return;
		}
		pManager.log(LogUtil
				.info(String.format("Provider points to location %s. Last fetched version %s. Requires bundle %s",
						location, version, bundleName)));
		Bundle bundle = Platform.getBundle(bundleName);
		if (bundle == null) {
			logInvalidProvider(pManager, provider);
			return;
		}

		try {
			File fileLocation = new File(userLocation, bundleName);
			if (!fileLocation.exists()) {
				fileLocation.mkdirs();
			}

			File versionFile = new File(fileLocation, "version.txt");
			if (!versionFile.exists()) {
				versionFile.createNewFile();
				try (FileOutputStream fos = new FileOutputStream(versionFile)) {
					fos.write(version.getBytes());
				}
			}

			String existingVersion = getFileContent(versionFile);
			File providerFile = new File(fileLocation, "provider.json");
			if (!version.equals(existingVersion) || !providerFile.exists()) {
				pManager.log(LogUtil.info(String.format("Old version: %s. New version %s", existingVersion, version)));
				URL webFile = new URL(pBaseURL + "/" + location);
				try (InputStream in = webFile.openStream()) {
					Files.copy(in, providerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				try (FileOutputStream fos = new FileOutputStream(versionFile)) {
					fos.write(version.getBytes());
				}
			}
			registerProvider(pManager, bundleName, fileLocation);

		} catch (IOException e) {
			String symbolicName = FrameworkUtil.getBundle(ProviderLoader.class).getSymbolicName();
			pManager.log(
					new Status(IStatus.ERROR, symbolicName, "Error loading provider from: " + pProvider.toString(), e));
		}
	}

	private static void registerProvider(ITipManager pManager, String bundleName, File pFileLocation)
			throws IOException, MalformedURLException {
		JsonTipProvider tipProvider = new JsonTipProvider() {

			@Override
			public String getID() {
				return bundleName + ".json.provider";
			}

			@Override
			public synchronized IStatus loadNewTips(IProgressMonitor pMonitor) {
				getManager().log(LogUtil.info(String.format("Load new tips START for %s", getID())));
				IStatus status = super.loadNewTips(pMonitor);
				getManager().log(status);
				getManager().log(LogUtil.info(String.format("Load new tips END   for %s", getID())));
				return status;
			}
		};

		File providerFile = new File(pFileLocation, "provider.json");
		String fileLocation = providerFile.toURI().toURL().toString();
		tipProvider.setJsonUrl(fileLocation);
		pManager.log(LogUtil.info(String.format("Provider file stored at %s", fileLocation)));
		pManager.register(tipProvider);
	}

	public static String getFileContent(File pVersionFile) throws IOException {
		try (FileInputStream fis = new FileInputStream(pVersionFile)) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				return br.readLine();
			}
		}
	}

	private static void logInvalidProvider(ITipManager pManager, JsonObject pProvider) {
		String symbolicName = FrameworkUtil.getBundle(ProviderLoader.class).getSymbolicName();
		pManager.log(new Status(IStatus.ERROR, symbolicName, "Error loading provider from: " + pProvider.toString()));
	}
}