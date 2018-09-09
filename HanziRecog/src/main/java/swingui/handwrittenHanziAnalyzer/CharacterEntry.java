/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
 *
 *   Refactorized by I-Tang HIU
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package swingui.handwrittenHanziAnalyzer;

import engine.beans.WrittenCharacter;
import engine.beans.WrittenPoint;
import engine.beans.SubStrokeDescriptor;
import engine.beans.WrittenStroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * 	Refactorized by I-Tang HIU August 2018
 * An app to generate character stroke recognizer for use by HanziDict.
 * You can trace load a character by typing its unicode (i.e. 4e00)
 * in the box and hitting load.  That loads a character in the background
 * that you can use to trace.  Hit analyze to generate the substroke
 * segments.  These are displayed in the box.  Check that each segment
 * is a unique substroke and makes sense.  When done it outlook.
 * The stroke recognizer is spit into the given OutputStream.  It can be copied
 * into a strokes text file.
 */
public class CharacterEntry extends JPanel {
	
	private StrokeEntryCanvas strokeCanvas;
	private JTextField unicodeEntryField;
	private JButton loadCharButton;
	private JButton analyzeButton;
	private JButton outputButton;
	private PrintWriter out;

	public CharacterEntry(Font bgFont, OutputStream out) {

		initUI(bgFont);
		this.out = new PrintWriter(out);
	}
	
	private void initUI(Font bgFont) {

		bgFont = bgFont.deriveFont(250f);
		strokeCanvas = new StrokeEntryCanvas();
		strokeCanvas.setPreferredSize(new Dimension(250, 250));
		strokeCanvas.setForeground(Color.LIGHT_GRAY);
		strokeCanvas.setFont(bgFont);
		strokeCanvas.setHorizontalAlignment(SwingConstants.CENTER);
		unicodeEntryField = new JTextField(4);
		loadCharButton = new JButton("load");
		analyzeButton = new JButton("analyze");
		outputButton = new JButton("output");
		
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object eventSource = e.getSource();
				if(eventSource == CharacterEntry.this.loadCharButton) {
					CharacterEntry.this.loadChar();
				} else if(eventSource == CharacterEntry.this.analyzeButton) {
					CharacterEntry.this.analyzeAndMark();
				} else {
					CharacterEntry.this.output();
				}
			}
		};
		
		loadCharButton.addActionListener(buttonListener);
		analyzeButton.addActionListener(buttonListener);
		outputButton.addActionListener(buttonListener);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(unicodeEntryField);
		buttonPanel.add(loadCharButton);
		buttonPanel.add(analyzeButton);
		buttonPanel.add(outputButton);
	
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, strokeCanvas);
		add(BorderLayout.SOUTH, buttonPanel);
	}
	
	private void loadChar() {

		String unicodeText = unicodeEntryField.getText();
		try {
			strokeCanvas.setText(Character.toString((char)Integer.parseInt(unicodeText, 16)));
		} catch(NumberFormatException nfe) {
			strokeCanvas.setText("");
			Toolkit.getDefaultToolkit().beep();
		}
		strokeCanvas.clear();
		strokeCanvas.repaint();
	}
	
	private void analyzeAndMark() {
		strokeCanvas.getCharacter().analyzeAndMark();
		strokeCanvas.repaint();
	}
	
	private void output() {

		WrittenCharacter character = strokeCanvas.getCharacter();
		DecimalFormat hundredths = new DecimalFormat("0.00");
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(unicodeEntryField.getText());
		for(Iterator strokeIter = character.getStrokes().iterator(); strokeIter.hasNext();) {
			sbuf.append(" | ");
			WrittenStroke stroke = (WrittenStroke)strokeIter.next();
			//TODO fix this getSubStrokes(null)
			Iterator subStrokesIter = stroke.getSubStrokes(null).iterator();
			if(subStrokesIter.hasNext()) {
				while(true) {
					SubStrokeDescriptor subStroke = (SubStrokeDescriptor)subStrokesIter.next();
					String direction = hundredths.format(subStroke.getDirection());
					String length = hundredths.format(subStroke.getLength());
					sbuf.append("(").append(direction).append(", ").append(length).append(")");
					if(subStrokesIter.hasNext()) {
						sbuf.append(" # ");
					} else {
						break;
					}
				}
			}
		}
		out.println(sbuf.toString());
		out.flush();
	}
	
	static private class StrokeEntryCanvas extends CharacterCanvas {
		
		static private final int POINT_RADIUS = 3;
		
		protected void paintStroke(WrittenStroke stroke, Graphics g) {
			super.paintStroke(stroke, g);
			
			for(Iterator pointIter = stroke.getPoints().iterator(); pointIter.hasNext();) {
				WrittenPoint nextPoint = (WrittenPoint)pointIter.next();
				Color previousColor = g.getColor();
				g.setColor(Color.RED);
				if(nextPoint.isPivot()) {
					paintPoint(nextPoint, g);
				}
				g.setColor(previousColor);
			}
		}
		
		private void paintPoint(WrittenPoint point, Graphics g) {

			double x = point.getX();
			double y = point.getY();
			g.fillOval((int)x - POINT_RADIUS, (int)y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
		}
	}
}
