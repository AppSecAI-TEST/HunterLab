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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class SwatchIcon implements Icon {

	Color color;

	int width;

	int height;

	public SwatchIcon(Color c) {
		this(16, 16, c);
	}

	public SwatchIcon(int w, int h, Color c) {
		width = w;
		height = h;
		color = c;
	}

	public int getIconWidth() {
		return width;
	}

	public int getIconHeight() {
		return height;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.setColor(c.getForeground());
		g.drawRect(x, y, width, height);
		return;
	}

	public static void main(String[] args) {
		javax.swing.JFrame frame = new javax.swing.JFrame("test");
		frame.getContentPane().setLayout(new java.awt.FlowLayout());
		frame.getContentPane().add(new javax.swing.JButton("Red", new SwatchIcon(10, 10, Color.red)));
		frame.getContentPane().add(new javax.swing.JButton("Blue", new SwatchIcon(10, 10, Color.blue)));
		frame.pack();
		frame.setVisible(true);
	}

}
