package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintWriter;
import java.util.Date;

import org.eclipse.update.core.IActivity;

public class ConfigurationActivity implements IActivity, IWritable {
	
	private String label;
	private String action;
	private Date date;
	private int status;
	

	/**
	 * Constructor for ConfigurationActivity.
	 */
	public ConfigurationActivity(String action) {
		super();
		this.action = action;
		this.status = STATUS_NOK; 
	}

	/*
	 * @see IActivity#getAction()
	 */
	public String getAction() {
		return action;
	}

	/*
	 * @see IActivity#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/*
	 * @see IActivity#getStatus()
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the date.
	 * @param date The date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Sets the status.
	 * @param status The status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/*
	 * @see IActivity#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
		w.println("action=\""+action+"\" ");
		if (label!=null){
			w.println(gap+"label=\""+Writer.xmlSafe(label)+"\" ");
		}
		w.println("date=\"" + date.getTime() + "\" ");
		w.print("status=\""+status+"\"");
		w.println(">");
		w.println("");
		
		
		// end
		w.println(gap+"</"+InstallConfigurationParser.ACTIVITY+">");
		
		
		
	}

}

