package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Stack;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.NonPluginEntry;
import org.eclipse.update.core.PluginEntry;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse default feature.xml
 */

public class DefaultFeatureParser extends DefaultHandler {

	private SAXParser parser;
	private FeatureModelFactory factory;
	private MultiStatus status;

	private static final int STATE_IGNORED_ELEMENT = -1;
	private static final int STATE_INITIAL = 0;
	private static final int STATE_FEATURE = 1;
	private static final int STATE_HANDLER = 2;
	private static final int STATE_DESCRIPTION = 3;
	private static final int STATE_COPYRIGHT = 4;
	private static final int STATE_LICENSE = 5;
	private static final int STATE_URL = 6;
	private static final int STATE_UPDATE = 7;
	private static final int STATE_DISCOVERY = 8;
	private static final int STATE_REQUIRES = 9;
	private static final int STATE_IMPORT = 10;
	private static final int STATE_PLUGIN = 11;
	private static final int STATE_DATA = 12;
	private static final String PLUGIN_ID = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();

	private static final String FEATURE = "feature";
	private static final String HANDLER = "install-handler";
	private static final String DESCRIPTION = "description";
	private static final String COPYRIGHT = "copyright";
	private static final String LICENSE = "license";
	private static final String URL = "url";
	private static final String UPDATE = "update";
	private static final String DISCOVERY = "discovery";
	private static final String REQUIRES = "requires";
	private static final String IMPORT = "import";
	private static final String PLUGIN = "plugin";
	private static final String DATA = "data";
	// Current State Information
	Stack stateStack = new Stack();

	// Current object stack (used to hold the current object we are
	// populating in this plugin descriptor
	Stack objectStack = new Stack();

	/**
	 * Constructor for DefaultFeatureParser
	 */
	public DefaultFeatureParser(FeatureModelFactory factory) {
		super();
		this.parser = new SAXParser();
		this.parser.setContentHandler(this);
		this.factory = factory;
	}

	/**
	 * @since 2.0
	 */
	public FeatureModel parse(InputStream in) throws SAXException, IOException {
		stateStack.push(new Integer(STATE_INITIAL));		
		parser.parse(new InputSource(in));
		if (objectStack.isEmpty())
			throw new SAXException("Error parsing stream. cannot find Feature tag. Feature not created.");
		else {
			if (objectStack.peek() instanceof FeatureModel) {
				return (FeatureModel) objectStack.pop();
			} else {
				String stack = "";
				Iterator iter = objectStack.iterator();
				while (iter.hasNext()) {
					stack = stack + iter.next().toString() + "\r\n";
				}
				throw new SAXException("Internal Error. Wrong parsing stack.\r\n" + stack);
			}
		}
	}

	/**
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			debug("Start Element: uri:" + uri + " local Name:" + localName + " qName:" + qName);

		String tag = localName.trim();

		int state = ((Integer) stateStack.peek()).intValue();
		switch (state) {
			case STATE_IGNORED_ELEMENT :
				internalErrorUnknownTag("unknown element in ingored state:" + localName);
				break;

			case STATE_INITIAL :
				handleInitialState(localName, attributes);
				break;

			case STATE_FEATURE :
				handleFeatureState(localName, attributes);
				break;

			case STATE_HANDLER :
				handleHandlerState(localName, attributes);
				break;

			case STATE_DESCRIPTION:
				handleDescriptionState(localName, attributes);
				break;
				
			case STATE_COPYRIGHT :
				handleCopyrightState(localName, attributes);
				break;

			case STATE_LICENSE :
				handleLicenseState(localName, attributes);
				break;

			case STATE_URL :
				handleURLState(localName, attributes);
				break;

			case STATE_UPDATE :
				handleUpdateState(localName, attributes);
				break;

			case STATE_DISCOVERY :
				handleDiscoveryState(localName, attributes);
				break;

			case STATE_REQUIRES :
				handleRequiresState(localName, attributes);
				break;

			case STATE_IMPORT :
				handleImportState(localName, attributes);
				break;

			case STATE_PLUGIN :
				handlePluginState(localName, attributes);
				break;

			case STATE_DATA :
				handleDataState(localName, attributes);
				break;

			default :
				internalErrorUnknownTag("Start Element:unknown state:" + state);
				break;
		}

	}

	public void handleInitialState(String elementName, Attributes attributes) throws SAXException {
		if (elementName.equals(FEATURE)) {
			stateStack.push(new Integer(STATE_FEATURE));
			processFeature(attributes);		
		} else		
			internalErrorUnknownTag("unknown root element :" + elementName);
	}
	
	public void handleFeatureState(String elementName, Attributes attributes) throws SAXException {
		if (elementName.equals(HANDLER)) {
			stateStack.push(new Integer(STATE_HANDLER));
			//processHandler(attributes);
		} else if (elementName.equals(DESCRIPTION)) {
			stateStack.push(new Integer(STATE_DESCRIPTION));
			processInfo(attributes);
		} else if (elementName.equals(COPYRIGHT)) {
			stateStack.push(new Integer(STATE_COPYRIGHT));
			processInfo(attributes);
		} else if (elementName.equals(LICENSE)) {
			stateStack.push(new Integer(STATE_LICENSE));
			processInfo(attributes);
		} else if (elementName.equals(URL)) {
			stateStack.push(new Integer(STATE_URL));
			//processURL(attributes);
		} else if (elementName.equals(REQUIRES)) {
			stateStack.push(new Integer(STATE_REQUIRES));
			//processRequire(attributes);
		} else if (elementName.equals(PLUGIN)) {
			stateStack.push(new Integer(STATE_PLUGIN));
			processPlugin(attributes);
		} else if (elementName.equals(DATA)) {
			stateStack.push(new Integer(STATE_DATA));
			processData(attributes);
		} else
			internalErrorUnknownTag("unknown element :" + elementName + " inside feature tag.");
	}
	public void handleHandlerState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside handler tag.");
	}
	public void handleCopyrightState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside copyright tag.");
	}
	public void handleLicenseState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside license tag.");
	}
	public void handleDescriptionState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside description tag.");
	}
	public void handleURLState(String elementName, Attributes attributes) throws SAXException {
		if (elementName.equals(UPDATE)) {
			stateStack.push(new Integer(STATE_UPDATE));
			processURLInfo(attributes);
		} else if (elementName.equals(DISCOVERY)) {
			stateStack.push(new Integer(STATE_DISCOVERY));
			processURLInfo(attributes);
		} else
			internalErrorUnknownTag("unknown element :" + elementName + " inside URL tag.");
	}
	public void handleUpdateState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside update tag.");
	}
	public void handleDiscoveryState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside discovery tag.");
	}
	public void handleRequiresState(String elementName, Attributes attributes) throws SAXException {
		if (elementName.equals(IMPORT)) {
			stateStack.push(new Integer(STATE_IMPORT));
			processImport(attributes);
		} else
			internalErrorUnknownTag("unknown element :" + elementName + " inside require tag.");
	}
	public void handleImportState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside import tag.");
	}
	public void handlePluginState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside plugin tag.");
	}
	public void handleDataState(String elementName, Attributes attributes) throws SAXException {
		internalErrorUnknownTag("unknown element :" + elementName + " inside data tag.");
	}

	/** 
	 * process feature info
	 */
	private void processFeature(Attributes attributes) {

		// identifier and version
		String id = attributes.getValue("id");
		String ver = attributes.getValue("version");

		if (id == null || id.trim().equals("") || ver == null || ver.trim().equals("")) {
			internalError("The id or the version tag of the feature is null or does not exist.");
		} else {
			// create feature model
			FeatureModel feature = factory.createFeatureModel();

			feature.setFeatureIdentifier(id);
			feature.setFeatureVersion(ver);

			// label
			String label = attributes.getValue("label");
			feature.setLabel(label);

			// provider
			String provider = attributes.getValue("provider-name");
			feature.setProvider(provider);

			//image
			String imageURL = attributes.getValue("image");
			feature.setImageURLString(imageURL);

			// OS
			String os = attributes.getValue("os");
			feature.setOS(os);

			// WS
			String ws = attributes.getValue("ws");
			feature.setWS(ws);

			// NL
			String nl = attributes.getValue("nl");
			feature.setNL(nl);

			// application
			String application = attributes.getValue("application");
			feature.setApplication(application);

			objectStack.push(feature);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
				debug("End process DefaultFeature tag: id:" + id + " ver:" + ver + " label:" + label + " provider:" + provider);
				debug("End process DefaultFeature tag: image:" + imageURL);
				debug("End process DefaultFeature tag: ws:" + ws + " os:" + os + " nl:" + nl + " application:" + application);
			}
		}
	}

	/** 
	 * process URL info with element text
	 */
	private void processInfo(Attributes attributes) {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		inf.setURLString(infoURL);

		objectStack.push(inf);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			debug("Processed Info: url:" + infoURL);
	}

	/** 
	 * process URL info with label attribute
	 */
	private void processURLInfo(Attributes attributes) {
		URLEntryModel inf = factory.createURLEntryModel();
		String infoURL = attributes.getValue("url");
		String label = attributes.getValue("label");
		inf.setURLString(infoURL);
		inf.setAnnotation(label);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			debug("Processed URLInfo: url:" + infoURL + " label:" + label);

		objectStack.push(inf);
	}

	/** 
	 * process import info
	 */
	private void processImport(Attributes attributes) {
		String id = attributes.getValue("plugin");
		if (id == null || id.trim().equals(""))
			internalError("Invalid plugin id tag of a import tag. Value is required.");
		else {
			ImportModel imp = factory.createImportModel();			
			String ver = attributes.getValue("version");
			String match = attributes.getValue("match");
			imp.setPluginIdentifier(id);
			imp.setPluginVersion(ver);
			imp.setMatchingRuleName(match);

			objectStack.push(imp);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
				debug("Processed require: id:" + id + " ver:" + ver);
				debug("Processed require: match:" + match);
			}

		}
	}

	/** 
	 * process plugin entry info
	 */
	private void processPlugin(Attributes attributes) {
		String id = attributes.getValue("id");
		String ver = attributes.getValue("version");
		if (id == null || id.trim().equals("") || ver == null || ver.trim().equals("")) {
			internalError("The id or the version tag of the plugin is null or does not exist.");
		} else {
			PluginEntryModel pluginEntry = factory.createPluginEntryModel();
			pluginEntry.setPluginIdentifier(id);
			pluginEntry.setPluginVersion(ver);

			String fragment = attributes.getValue("fragment");
			pluginEntry.isFragment(fragment != null && fragment.trim().equalsIgnoreCase("true"));

			//feature.setOS
			String os = attributes.getValue("os");
			pluginEntry.setOS(os);

			//feature.setWS
			String ws = attributes.getValue("ws");
			pluginEntry.setWS(ws);

			//feature.setNL
			String nl = attributes.getValue("nl");
			pluginEntry.setNL(nl);

			// download size
			long download_size = ContentEntryModel.UNKNOWN_SIZE;
			String download = attributes.getValue("download-size");
			if (download != null && !download.trim().equals("")) {
				try {
					download_size = Long.valueOf(download).longValue();
				} catch (NumberFormatException e) {
					// use UNKNOWN_SIZE
				}
			}
			pluginEntry.setDownloadSize(download_size);

			// install size	
			long install_size = ContentEntryModel.UNKNOWN_SIZE;
			String install = attributes.getValue("install-size");
			if (install != null && !install.trim().equals("")) {
				try {
					install_size = Long.valueOf(install).longValue();
				} catch (NumberFormatException e) {
					// use UNKNOWN_SIZE
				}
			}
			pluginEntry.setInstallSize(install_size);

			objectStack.push(pluginEntry);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
				debug("Processed Plugin: id:" + id + " ver:" + ver + " fragment:" + fragment);
				debug("Processed Plugin: os:" + os + " ws:" + ws + " nl:" + nl);
				debug("Processed Plugin: download size:" + download_size + " install size:" + install_size);
			}

		}
	}

	/** 
	 * process non-plug-in entry info
	 */
	private void processData(Attributes attributes) {
		String id = attributes.getValue("id");
		if (id == null || id.trim().equals("")) {
			internalError("The id tag of the data tag is null or does not exist. Value is required");
		} else {
			NonPluginEntryModel dataEntry = factory.createNonPluginEntryModel();
			dataEntry.setIdentifier(id);

			// download size
			long download_size = ContentEntryModel.UNKNOWN_SIZE;
			String download = attributes.getValue("download-size");
			if (download != null && !download.trim().equals("")) {
				try {
					download_size = Long.valueOf(download).longValue();
				} catch (NumberFormatException e) {
					// use UNKNOWN_SIZE
				}
			}
			dataEntry.setDownloadSize(download_size);

			// install size	
			long install_size = ContentEntryModel.UNKNOWN_SIZE;
			String install = attributes.getValue("install-size");
			if (install != null && !install.trim().equals("")) {
				try {
					install_size = Long.valueOf(install).longValue();
				} catch (NumberFormatException e) {
					// use UNKNOWN_SIZE
				}
			}
			dataEntry.setInstallSize(install_size);

			objectStack.push(dataEntry);

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING){
				debug("Processed Data: id:" + id);
				debug("Processed Data: download size:" + download_size + " install size:" + install_size);
			}

		}
	}

	/**
	 * @see DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String localName, String qName) {

		String tag = localName.trim();
		
		// variables used
		URLEntryModel info = null;
		FeatureModel featureModel=null;
		String text = null;
		int innerState = 0;

		int state = ((Integer) stateStack.peek()).intValue();
		switch (state) {
			case STATE_IGNORED_ELEMENT :
				stateStack.pop();
				break;

			case STATE_INITIAL :
				internalError("Stack back to Initial State, error parsing file");
				break;

			case STATE_FEATURE :
				stateStack.pop();
				if (objectStack.peek() instanceof String) {
					text = (String) objectStack.pop();
					FeatureModel feature = (FeatureModel) objectStack.peek();
					feature.getDescriptionModel().setAnnotation(text);
				}
				//do not pop
				break;

			case STATE_HANDLER :
				stateStack.pop();
				break;
			case STATE_DESCRIPTION :
				stateStack.pop();

				text = null;
				if (objectStack.peek() instanceof String) {
					text = (String) objectStack.pop();
				}

				info = (URLEntryModel) objectStack.pop();
				if (text != null)
					info.setAnnotation(text);

				innerState = ((Integer) stateStack.peek()).intValue();
				switch (innerState) {
					case STATE_FEATURE :
						featureModel = (FeatureModel) objectStack.peek();
						featureModel.setDescriptionModel(info);
						break;

					default :
						internalError("Description declared in wrong place; state:" + state);
						break;

				}
				break;

			case STATE_COPYRIGHT :
				stateStack.pop();
				text = null;
				if (objectStack.peek() instanceof String) {
					text = (String) objectStack.pop();
				}

				info = (URLEntryModel) objectStack.pop();
				if (text != null)
					info.setAnnotation(text);

				innerState = ((Integer) stateStack.peek()).intValue();
				switch (innerState) {
					case STATE_FEATURE :
						featureModel = (FeatureModel) objectStack.peek();
						featureModel.setCopyrightModel(info);
						break;

					default :
						internalError("Copyright declared in wrong place; state:" + state);
						break;

				}
				break;

			case STATE_LICENSE :
				stateStack.pop();

				text = null;
				if (objectStack.peek() instanceof String) {
					text = (String) objectStack.pop();
				}

				info = (URLEntryModel) objectStack.pop();
				if (text != null)
					info.setAnnotation(text);

				innerState = ((Integer) stateStack.peek()).intValue();
				switch (innerState) {
					case STATE_FEATURE :
						featureModel = (FeatureModel) objectStack.peek();
						featureModel.setLicenseModel(info);
						break;

					default :
						internalError("License in wrong place; state:" + state);
						break;

				}
				break;

			case STATE_URL :
				stateStack.pop();
				break;

			case STATE_UPDATE :
				stateStack.pop();
				info = (URLEntryModel) objectStack.pop();
				featureModel = (FeatureModel) objectStack.peek();
				if (featureModel.getUpdateSiteEntryModel() != null) {
					internalError("Update URL already parsed");
				} else {
					featureModel.setUpdateSiteEntryModel(info);
				}
				break;

			case STATE_DISCOVERY :
				stateStack.pop();
				info = (URLEntryModel) objectStack.pop();
				featureModel = (FeatureModel) objectStack.peek();
				featureModel.addDiscoverySiteEntryModel(info);
				break;

			case STATE_REQUIRES :
				stateStack.pop();
				break;

			case STATE_IMPORT :
				stateStack.pop();
				ImportModel importModel = (ImportModel) objectStack.pop();
				featureModel = (FeatureModel) objectStack.peek();
				featureModel.addImportModel(importModel);
				break;

			case STATE_PLUGIN :
				stateStack.pop();
				PluginEntryModel pluginEntry = (PluginEntryModel) objectStack.pop();				
				featureModel = (FeatureModel) objectStack.peek();
				featureModel.addPluginEntryModel(pluginEntry);
				break;

			case STATE_DATA :
				stateStack.pop();
				NonPluginEntryModel nonPluginEntry = (NonPluginEntryModel) objectStack.pop();				
				featureModel = (FeatureModel) objectStack.peek();
				featureModel.addNonPluginEntryModel(nonPluginEntry);
				break;

			default :
				internalErrorUnknownTag("End Element: unknown state:" + state);
				break;

		}

		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			debug("End Element:" + uri + ":" + localName + ":" + qName);
	}

	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		String text = new String(ch, start, length).trim();
		if (!text.equals("")) {
			//only push if not unknown state
			int state = ((Integer) stateStack.peek()).intValue();
			if (state != STATE_IGNORED_ELEMENT && state != STATE_INITIAL)
				objectStack.push(text);
		}
	}

	private void debug(String s) {
		System.out.println("DefaultSiteParser: " + s);
	}

	public void error(SAXParseException ex) {
		logStatus(ex);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		logStatus(ex);
		throw ex;
	}

	private void logStatus(SAXParseException ex) {
		String name = ex.getSystemId();
		if (name == null)
			name = "";
		else
			name = name.substring(1 + name.lastIndexOf("/"));

		String msg;
		if (name.equals(""))
			msg = "Error Parsing";
		else
			msg = "Error:" + name + " line:" + Integer.toString(ex.getLineNumber()) + " column:" + Integer.toString(ex.getColumnNumber()) + " message:" + ex.getMessage();
		error(new Status(IStatus.WARNING, PLUGIN_ID, Platform.PARSE_PROBLEM, msg, ex));
	}

	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void error(IStatus error) {

		getStatus().add(error);
		UpdateManagerPlugin.getPlugin().getLog().log(error);
		if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_PARSING)
			UpdateManagerPlugin.getPlugin().debug(error.toString());
	}
	/**
	 *
	 */
	public void internalErrorUnknownTag(String msg) {
		stateStack.push(new Integer(STATE_IGNORED_ELEMENT));
		internalError(msg);
	}
	/**
	 * Returns all of the status objects logged thus far by this factory.
	 *
	 * @return a multi-status containing all of the logged status objects
	 */
	public MultiStatus getStatus() {
		if (status == null) {
			status = new MultiStatus(PLUGIN_ID, Platform.PARSE_PROBLEM, "Error parsing Feature.xml", null);
		}
		return status;
	}

	private void internalError(String message) {
		error(new Status(IStatus.WARNING, PLUGIN_ID, Platform.PARSE_PROBLEM, message, null));
	}

}