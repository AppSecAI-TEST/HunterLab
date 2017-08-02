/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */

package edu.uchsc.ccp.knowtator.textsource.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import edu.uchsc.ccp.knowtator.textsource.DefaultTextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class FileTextSource extends DefaultTextSource {

	public static final String CLS_NAME = "file text source";

	protected File file;

	protected Charset charset;

	/**
	 * The name of the file with the rootPath prefix removed is the name of the
	 * FileTextSource. For example, if file.getPath is
	 * "/home/pogren/my_text.txt" and rootPath is "/home/pogren", then the name
	 * of the text source is my_text.txt. Typically file.getPath() == rootPath +
	 * "/"+ name.
	 */

	public FileTextSource(File file, String rootPath, Charset charset) {
		this.file = file;
		this.charset = charset;

		name = file.getPath();
		if (rootPath.endsWith("" + File.separatorChar) || rootPath.equals("")) {
			name = name.substring(rootPath.length());
		} else
			name = name.substring(rootPath.length() + 1);
		protegeClsName = CLS_NAME;
	}

	public File getFile() {
		return file;
	}

	public String getText() throws TextSourceAccessException {
		try {
			return readFile(new FileInputStream(file), charset);
		} catch (IOException ioe) {
			throw new TextSourceAccessException(ioe);
		}
	}

	public static String readFile(InputStream inputStream, Charset charset) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
		StringBuffer contents = new StringBuffer();

		String line = new String();
		while ((line = reader.readLine()) != null) {
			contents.append(line + "\n");
		}
		return contents.toString();
	}

}
