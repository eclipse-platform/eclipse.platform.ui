package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintWriter;

import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IActivity;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.internal.model.ConfigurationActivityModel;
import org.eclipse.update.internal.model.InstallConfigurationParser;

public class ConfigurationActivity extends ConfigurationActivityModel implements IActivity, IWritable {
	

	public ConfigurationActivity(){
	}

	/**
	 * Constructor for ConfigurationActivityModel.
	 */
	public ConfigurationActivity(int action) {
		super();
		setAction(action);
		setStatus(STATUS_NOK); 
	}

	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
		String gap = "";
		for (int i = 0; i < indent; i++)
			gap += " ";
		String increment = "";
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " ";
		
		
		w.print(gap + "<" + InstallConfigurationParser.ACTIVITY + " ");
		w.println("action=\""+getAction()+"\" ");
		if (getLabel()!=null){
			w.println(gap+"label=\""+Writer.xmlSafe(getLabel())+"\" ");
		}
		w.println("date=\"" + getDate().getTime() + "\" ");
		w.print("status=\""+getStatus()+"\"");
		w.println(">");
		w.println("");
		
		
		// end
		w.println(gap+"</"+InstallConfigurationParser.ACTIVITY+">");
		
		
		
	}

	/*
	 * @see IActivity#getInstallConfiguration()
	 */
	public IInstallConfiguration getInstallConfiguration() {
		return (IInstallConfiguration)getInstallConfigurationModel();
	}

}

