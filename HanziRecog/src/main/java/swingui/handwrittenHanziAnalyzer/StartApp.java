package swingui.handwrittenHanziAnalyzer;

import swingui.uicommon.ChineseFontFinder;

import javax.swing.*;
import java.awt.*;

public class StartApp {

    static public void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Font chineseFont = ChineseFontFinder.getChineseFont();
        frame.getContentPane().add(new CharacterEntry(chineseFont, System.out));
        frame.pack();
        frame.setVisible(true);
    }
}
