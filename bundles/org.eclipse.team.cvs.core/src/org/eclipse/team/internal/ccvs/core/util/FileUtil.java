package org.eclipse.team.internal.ccvs.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.zip.CRC32;

import javax.swing.plaf.FileChooserUI;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class FileUtil {

	public static final String PLATFORM_NEWLINE = System.getProperty("line.separator");
	
	
	public static void writeLines(File file, String[] content) throws CVSException {
		
		BufferedWriter fileWriter;

		try {
			fileWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i<content.length; i++) {
				fileWriter.write(content[i]);
				fileWriter.newLine();
			}
			fileWriter.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public static String[] readLines(File file) throws CVSException {
		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		String line;
		
		try {
			fileReader = new BufferedReader(new FileReader(file));
			while ((line = fileReader.readLine()) != null) {
				fileContentStore.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
			
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
	}
	
	public static void deepDelete(File resource) {
		if (resource.isDirectory()) {
			File[] fileList = resource.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				deepDelete(fileList[i]);
			}
		}
		resource.delete();
	}
}