/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mike Evans      - Fix for Bug 283136
 *******************************************************************************/

package org.eclipse.ui.intro.contentproviders;

import java.io.PrintWriter;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.intro.impl.Messages;
import org.eclipse.ui.internal.intro.impl.util.ReopenUtil;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.PlatformUI;
/**
 *
 * Class which contributes a checkbox to an intro page which allows welcome to show 
 * on startup. If the checkbox is checked the home page of intro will be shown the next 
 * time the Eclipse application starts up. This class may be subclassed to override 
 * the text for the checkbox label.
 * 
 * Implements the IIntroContentProvider to create the checkbox ui, and the
 * org.eclipse.ui.intro.config.IIntroAction interface for handling checkbox click events.
 * 
 * @since 3.3
 */
public class AlwaysWelcomeCheckbox implements IIntroContentProvider,IIntroAction {

	public final static String ALWAYS_SHOW_INTRO = "alwaysShowIntro"; //$NON-NLS-1$
	
	private boolean disposed = false;
	
	/**
	 * Override this method to change the default text used for the checkbox
	 * 
	 * @return String label for the checkbox 
	 * 
	 * @since 3.3
	 * 
	 */
	protected String getText()
	{
		// Default text
		return Messages.AlwaysWelcomeCheckbox_Text;
	}
	

	public void createContent(String id, PrintWriter out) {

		boolean alwaysShowIntro = getAlwaysShowIntroPref();

		// Use an IIntroAction url that points back to this class - 
		// particularly invoking run().
		// This url is 'activated' using the onClick event.
		out.print("<div id=\""+id+"\"><input type=\"checkbox\" "+  	//$NON-NLS-1$//$NON-NLS-2$
				"onClick=window.location="+ 						//$NON-NLS-1$
				"\"http://org.eclipse.ui.intro/runAction?"+ 		//$NON-NLS-1$
				"pluginId=org.eclipse.ui.intro&"+ 					//$NON-NLS-1$
				"class="+this.getClass().getName()+"\" ");			//$NON-NLS-1$ //$NON-NLS-2$
		
		if (alwaysShowIntro)
		{
			out.print(" checked=\"checked\""); //$NON-NLS-1$
			
	        PlatformUI.getPreferenceStore().setValue(
	        		IWorkbenchPreferenceConstants.SHOW_INTRO, alwaysShowIntro);	
		}
		
		out.println("/>"+getText()+"</div>");  //$NON-NLS-1$//$NON-NLS-2$
	}


	public void createContent(String id, Composite parent, FormToolkit toolkit) {
		if (disposed)
			return;

        boolean alwaysShowIntro = getAlwaysShowIntroPref();

		Button checkBox = new Button(parent,SWT.CHECK);
		toolkit.adapt((Control)checkBox,false,false);
		
		checkBox.setText(getText());
		checkBox.setSelection(alwaysShowIntro);
		checkBox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				reverseShowIntroState();
			}
		});
		
		if (alwaysShowIntro)
			PlatformUI.getPreferenceStore().setValue(
        		IWorkbenchPreferenceConstants.SHOW_INTRO, alwaysShowIntro);
	}

	public void dispose() {
		disposed = true;
	}

	public void init(IIntroContentProviderSite site) {
	}

	/**
	 * Method called when box is clicked in html (swt is handled with a
	 * SelectionAdapter - both methods call reverseShowIntroState())
	 */
	public void run(IIntroSite site, Properties params) {
		reverseShowIntroState();
	}
	
	/**
	 * Method reverses preference ALWAYS_SHOW_INTRO due to checkbox selection change
	 * 
	 */
	private void reverseShowIntroState()
	{
		// Retrieve current state of IUniversalIntroConst.ALWAYS_SHOW_INTRO, change it, and save it back
		// to both ALWAYS_SHOW_INTRO and SHOW_INTRO
        boolean alwaysShowIntro = !getAlwaysShowIntroPref();
        
        // local preference store
        setAlwaysShowIntroPref(alwaysShowIntro);
        
        // workbench preference store
        PlatformUI.getPreferenceStore().setValue(
        		IWorkbenchPreferenceConstants.SHOW_INTRO, alwaysShowIntro);	

	}
	
	public boolean getAlwaysShowIntroPref()
	{
		// If uninitialized, we will default to true (box will be checked)
		if (!ReopenUtil.isReopenPreferenceInitialized()) {
			setAlwaysShowIntroPref(true);
		}
		return ReopenUtil.isReopenPreference();
	}
	
	public void setAlwaysShowIntroPref(boolean val)
	{
		ReopenUtil.setReopenPreference(val);
	}
}
