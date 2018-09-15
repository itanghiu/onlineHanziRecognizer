package hanzirecog.swingui;

import javax.swing.*;

/*
 * Copyright (C) August 2018 I-Tang HIU
 * itang_hiu@hotmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
public class StartApp {

    /**
     * Run the app.
     * Need a main method so that it can be run stand-alone and not just as an Applet.
     * @param args
     */
    static public void main(String[] args) {

        OnlineHanziRecog inputApp = new OnlineHanziRecog();
        inputApp.init();	// init as if it were an applet
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(inputApp);
        frame.pack();
        frame.setVisible(true);
    }
}
