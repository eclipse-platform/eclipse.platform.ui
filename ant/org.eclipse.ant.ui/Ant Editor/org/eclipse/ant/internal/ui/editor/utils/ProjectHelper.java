/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.utils;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Derived from the original Ant ProjectHelper2 class
 * This class provides parsing for using a String as a source and provides
 * handlers that will continue parsing to completion upon hitting errors.
 */
public class ProjectHelper extends ProjectHelper2 {

	/**
	 * helper for path -> URI and URI -> path conversions.
	 */
	private static FileUtils fu = FileUtils.newFileUtils();
	
	/**
	 * The buildfile that is to be parsed. Must be set if parsing is to
	 * be successful.
	 */
	private File buildFile= null;

	private static AntHandler elementHandler = new ElementHandler();
	private static AntHandler projectHandler = new ProjectHandler();
	private static AntHandler targetHandler = new TargetHandler();
	
	
	public static class ElementHandler extends ProjectHelper2.ElementHandler {
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			return ProjectHelper.elementHandler;
		}
	}
	
	public static class MainHandler extends ProjectHelper2.MainHandler {
		
			/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) throws SAXParseException {
			if (name.equals("project") //$NON-NLS-1$
					&& (uri.equals("") || uri.equals(ANT_CORE_URI))) { //$NON-NLS-1$
				return ProjectHelper.projectHandler;
			} else {
               return super.onStartChild(uri, name, qname, attrs, context);
			}
		}
	}
	
	public static class ProjectHandler extends ProjectHelper2.ProjectHandler {
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) {
			if (name.equals("target") //$NON-NLS-1$
					&& (uri.equals("") || uri.equals(ANT_CORE_URI))) { //$NON-NLS-1$
				return ProjectHelper.targetHandler;
			} else {
				return ProjectHelper.elementHandler;
			}
		}
	}
	
	public static class TargetHandler extends ProjectHelper2.TargetHandler {
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartChild(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs, AntXMLContext context) {
			return ProjectHelper.elementHandler;
		}
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.helper.ProjectHelper2.AntHandler#onStartElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes, org.apache.tools.ant.helper.AntXMLContext)
		 */
		public void onStartElement(String uri, String tag, String qname, Attributes attrs, AntXMLContext context) {
			try {
				super.onStartElement(uri, tag, qname, attrs, context);
			} catch (SAXParseException e) {
				
			} catch (BuildException be) {
				
			}
		}
	}
	
     /**
     * Parses the project file, configuring the project as it goes.
     *
     * @param project the current project
     * @param source  the xml source
     * @param handler the root handler to use (contains the current context)
     * @exception BuildException if the configuration is invalid or cannot
     *                           be read
     */
    public void parse(Project project, Object source, RootHandler handler) throws BuildException {
    	
    	if (!(source instanceof String)) {
    		super.parse(project, source, handler);
    	}
    	
    	AntXMLContext context= (AntXMLContext)project.getReference("ant.parsing.context"); //$NON-NLS-1$
		//switch to using "our" handler so parsing will continue on hitting errors.
    	handler= new RootHandler(context, new ProjectHelper.MainHandler());
    	
        Reader stream= new StringReader((String)source);
             
        InputSource inputSource = null;
        try {
            /**
             * SAX 2 style parser used to parse the given file.
             */
            XMLReader parser = JAXPUtils.getNamespaceXMLReader();

            String uri = null;
            if (buildFile != null) {
                uri = fu.toURI(buildFile.getAbsolutePath());
            }

            inputSource = new InputSource(stream);
            if (uri != null) {
                inputSource.setSystemId(uri);
            }

            context.setBuildFile(buildFile);
            
            parser.setContentHandler(handler);
            parser.setEntityResolver(handler);
            parser.setErrorHandler(handler);
            parser.setDTDHandler(handler);
            parser.parse(inputSource);
        } catch (SAXParseException exc) {
        	//ignore as we will be parsing incomplete source
        } catch (SAXException exc) {
        	//ignore as we will be parsing incomplete source
        } catch (FileNotFoundException exc) {
            throw new BuildException(exc);
        } catch (UnsupportedEncodingException exc) {
              throw new BuildException(exc);
        } catch (IOException exc) {
            throw new BuildException(exc);
        } finally {
            if (stream != null) {
                try {
                	stream.close();
                } catch (IOException ioe) {
                    // ignore this
                }
            }
        }
    }

	/**
	 * Sets the buildfile that is about to be parsed or <code>null</code> if
	 * parsing has completed.
	 * 
	 * @param file The buildfile about to be parsed
	 */
	public void setBuildFile(File file) {
		buildFile= file;
	}
}