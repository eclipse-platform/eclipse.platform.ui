package org.eclipse.update.internal.transform;

import org.eclipse.update.core.*;
import org.eclipse.update.ui.internal.model.*;
import java.io.*;
import java.net.URL;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class FeatureTransform extends AbstractTransform {
	public static final String KEY_LABEL = "label";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_VERSION = "version";
	public static final String KEY_SIZE = "size";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_INFO_URL = "infoURL";
	public static final String KEY_SCHEDULE_LABEL = "scheduleLabel";
	public static final String KEY_NOW_LABEL = "nowLabel";
	public static final String KEY_MODE_PAR = "modePar";
	public static final int INSTALL = 0;
	public static final int UNINSTALL = 1;
	public static final int CANCEL = 2;
	
	private static final String [] modePars = { "install", "uninstall", "cancel" };
	
	private String defaultImage = getHTMLBase()+"/images/provider.gif";
	
	private int getMode(IFeature feature) {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		if (model.checklistContains(feature))
		   return CANCEL;
		ISite site = feature.getSite();
		if (site instanceof ILocalSite)
		   return UNINSTALL;
		return INSTALL;
	}
	
	private boolean isInstalled(IFeature feature) {
		return feature.getSite() instanceof ILocalSite;
	}
	
	public String getModeParameter(IFeature feature) {
		int mode = getMode(feature);
		return modePars[mode];
	}
		
	
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
		   return feature.getDescription().getText();
		if (key.equals(KEY_SCHEDULE_LABEL)) {
			switch (getMode(feature)) {
				case CANCEL:
		   		if (isInstalled(feature))
			      return "Cancel Uninstall";
			   	else
			      return "Cancel Install";
			    case INSTALL:
			    	return "Schedule Install";
			    case UNINSTALL:
			    	return "Schedule Uninstall";
			}
		}
		if (key.equals(KEY_NOW_LABEL)) {
			int mode = getMode(feature);
			if (mode == UNINSTALL)
			   return "Uninstall Now!";
			else
				return "Install Now!";
		}
		if (key.equals(KEY_MODE_PAR)) {
			return getModeParameter(feature);
		}
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
		writer.print("infoURL=");
		URL infoURL = feature.getInfoURL();
		if (infoURL!=null) {
			writer.println("\""+infoURL.toString()+"\";");
		}
		else
			writer.println("\"\";");
		if (isScheduled(input))
		   writer.println("scheduled=true;");
	}
	
	boolean isScheduled(Object input) {
		if (input instanceof ChecklistJob)
		   return true;
		IFeature feature = (IFeature)input;
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		return model.checklistContains(feature);
	}

	/**
	 * @see AbstractTransform#getObjectTemplate(Object)
	 */
	protected String getObjectTemplate(Object input) {
		return "feature.html";
	}

}

