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
 * The Original Code is the WordFreak annotation tool.
 *
 * The Initial Developer of the Original Code is Thomas S. Morton
 * Copyright (C) 2002.  All Rights Reserved.
 * 
 * Contributor(s):
 *   Thomas S. Morton <tsmorton@cis.upenn.edu> (Original Author)
 *   Jeremy LaCivita <lacivita@linc.cis.upenn.edu>
 */
package org.annotation.gui;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Provides static utilites for icons.
 * 
 * @author Tom Morton
 * 
 */
public class Icons {

	public static ImageIcon getDefaultIcon() {
		return new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY));
	}

	public static ImageIcon getIcon(String path) {
		URL imageURL = ClassLoader.getSystemClassLoader().getResource(path);
		if (imageURL != null) {
			return getIcon(imageURL);
		}
		System.err.println("Failed to load icon: " + path);
		return getDefaultIcon();
	}

	public static ImageIcon getIcon(URL imageURL) {
		// Toolkit tk = Toolkit.getDefaultToolkit();
		// Image image = tk.getImage(imageURL);
		return new ImageIcon(imageURL);
	}

	public static ImageIcon getIcon(String path, Object o) {
		URL imageURL = o.getClass().getResource(path);
		if (imageURL != null) {
			return getIcon(imageURL);
		}
		System.err.println("Failed to load icon: " + path);
		return getDefaultIcon();
	}
}
