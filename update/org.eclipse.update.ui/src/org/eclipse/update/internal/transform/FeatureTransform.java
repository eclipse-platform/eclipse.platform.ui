package org.eclipse.update.internal.transform;

import org.eclipse.update.core.IFeature;
import org.eclipse.update.ui.internal.model.*;
import java.io.*;
import java.net.URL;

public class FeatureTransform extends AbstractTransform {
	public static final String KEY_LABEL = "label";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_VERSION = "version";
	public static final String KEY_SIZE = "size";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_INFO_URL = "infoURL";
	
	private String defaultImage = getHTMLBase()+"/images/provider.gif";
	
	/**
	 * @see AbstractTransform#getValue(String)
	 */
	protected String getValue(Object input, String key) {
		IFeature feature = getFeature(input);
		if (feature==null) return key;
		if (key.equals(KEY_LABEL)) return feature.getLabel();
		if (key.equals(KEY_PROVIDER)) return feature.getProvider();
		if (key.equals(KEY_VERSION)) 
		   return feature.getIdentifier().getVersion().toString();
		if (key.equals(KEY_SIZE))
		   return "0";
		if (key.equals(KEY_IMAGE))
		   return getFeatureImage(feature);
		if (key.equals(KEY_DESCRIPTION))
		   return feature.getDescription();
		return super.getValue(input, key);
	}
	private String getFeatureImage(IFeature feature) {
		return defaultImage;
	}
	
	IFeature getFeature(Object input) {
		if (input instanceof IFeature) return (IFeature)input;
		if (input instanceof ChecklistJob) {
			ChecklistJob job = (ChecklistJob)input;
			return job.getFeature();
		}
		return null;
	}
	
	protected void writeJavaScriptSection(Object input, PrintWriter writer) {
		IFeature feature = getFeature(input);
		StringWriter swriter = new StringWriter();
		writer.print("var infoURL=");
		URL infoURL = feature.getInfoURL();
		if (infoURL!=null) {
			writer.println("\""+infoURL.toString()+"\";");
		}
		else 
			writer.println("\"\";");
	}

	/**
	 * @see AbstractTransform#getObjectTemplate(Object)
	 */
	protected String getObjectTemplate(Object input) {
		return "feature.html";
	}

}

