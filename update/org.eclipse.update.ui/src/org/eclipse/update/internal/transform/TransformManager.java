package org.eclipse.update.internal.transform;

import java.util.*;
import java.io.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.ui.*;
import java.net.*;

public class TransformManager {
	private Vector tempFiles=new Vector();
	
	public TransformManager() {
	}
	
	public String getTransformedURL(Object input) {
		IAdapterManager af = Platform.getAdapterManager();
		ITransform transform  = (ITransform)af.getAdapter(input, ITransform.class);
		if (transform == null) 
		   return null;
		String fileName = transform.getTemplateFileName(input);
		String template = loadTemplate(fileName);
		String outputContent = transform.transform(input, template);
		String outputFileName = createFileName(input);
		saveFile(outputContent, outputFileName);
		if (!tempFiles.contains(outputFileName))
		   tempFiles.add(outputFileName);
		return getURL(outputFileName);
	}
	
	String loadTemplate(String fileName) {
		try {
			URL url = new URL(fileName);
			InputStream source = url.openStream();
			ByteArrayOutputStream target = new ByteArrayOutputStream();
			copyStreams(source, target);
			return new String(target.toByteArray(), "UTF8");
		}
		catch (IOException e) {
			return "<html></html>";
		}
	}
	
	private void copyStreams(InputStream source, OutputStream target) 
	throws IOException {
		byte[] buffer = new byte[8192];
		while (true) {
			int bytesRead = source.read(buffer);
			if (bytesRead == -1)
				break;
			target.write(buffer, 0, bytesRead);
		}
		source.close();
		target.flush();
		target.close();
	}
	
	String createFileName(Object input) {
		IPath stateLoc = UpdateUIPlugin.getDefault().getStateLocation();
		String localName = input.getClass().getName()+"_"+input.hashCode()+".html";
		IPath tmpFile = stateLoc.append(localName);
		return tmpFile.toOSString();
	}
	
	void saveFile(String content, String fileName) {
		try {
			ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes("UTF8"));
			FileOutputStream target = new FileOutputStream(fileName);

		   	copyStreams(source, target);
		}
		catch (IOException e) {
			System.out.println(e);
		}
		
	}
	
	public void shutdown() {
		for (int i=0; i<tempFiles.size(); i++) {
			File file = new File(tempFiles.elementAt(i).toString());
			if (file.exists()) {
				file.delete();
			}
		}
		tempFiles.clear();
	}
	
	String getURL(String fileName) {
		return "file:///"+fileName;
	}
}