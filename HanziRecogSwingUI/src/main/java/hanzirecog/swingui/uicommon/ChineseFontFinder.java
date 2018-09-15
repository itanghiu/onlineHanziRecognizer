/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
 *
 *  Refactorized by I-Tang HIU
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
package hanzirecog.swingui.uicommon;

import org.apache.log4j.Logger;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

/**
 * Some static methods for finding system fonts on the system that appear
 * capable of displaying Chinese characters.
 */
public class ChineseFontFinder {

    private static Logger logger = Logger.getLogger(ChineseFontFinder.class.toString());

    // Preferred, known Fonts.
    static private final String PREFERRED_FONTS[] = {"SimSun", "STHeiti", "Bitstream Cyberbit"};
    static private final String SIMPLIFIED_CHARACTERS = "\u8fd9\u6765\u56fd\u4e2a\u6c49";
    static private final String TRADITIONAL_CHARACTERS = "\u9019\u4f86\u570b\u500b\u6f22";
    // The default initial font size.
    static public final int FONT_SIZE = 24;

    /**
     * Scans through all the Fonts on the local system and returns the
     * best one it can find for displaying Chinese characters.
     *
     * @return the Font
     */
    static public Font getChineseFont() {

        Font currentChoice = null;
        boolean currentIsSimplified = false;
        boolean currentIsTraditional = false;

        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

        // Scan through all fonts
        for (Font candidateFont :  allFonts) {
            boolean useCandidate = false;
            boolean candidateIsSimplified = isSimplifiedFont(candidateFont);
            boolean candidateIsTraditional = isTraditionalFont(candidateFont);

            if (isPreferredFont(candidateFont)) {
                // Use the first "preferred" font found.
                currentChoice = candidateFont;
                break;
            }
            else if (null == currentChoice) {
                // If we haven't found any fonts yet and the candidate
                // either supports traditional or simplified then save
                // it but keep looking.
                if (candidateIsSimplified || candidateIsTraditional)
                    useCandidate = true;
            }
            else if (candidateIsTraditional && candidateIsSimplified &&
                    (!currentIsTraditional || !currentIsSimplified)) {
                // If the current choice doesn't support both simplified
                // and traditional, and the candidate does, then save the
                // candidate but keep looking.
                useCandidate = true;
            }

            if (useCandidate) {
                // Found a possible candidate, save it, but keep looking.
                currentChoice = candidateFont;
                currentIsSimplified = candidateIsSimplified;
                currentIsTraditional = candidateIsTraditional;
            }
        }
        if (currentChoice != null)
            currentChoice = currentChoice.deriveFont(Font.PLAIN, FONT_SIZE);
        return currentChoice;
    }

    /**
     * Scans all the Fonts on the local sytem and returns those that
     * appear to be capable of displaying Chinese characters.
     *
     * @return chinese Fonts
     */
    static public Font[] getAllChineseFonts() {

        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        List<Font> chineseFontList = new ArrayList();
        for (Font font :  allFonts)
            if (isTraditionalFont(font) || isSimplifiedFont(font))
                chineseFontList.add(font);
        Font[] chineseFonts = new Font[chineseFontList.size()];
        return chineseFontList.toArray(chineseFonts);
    }

    /**
     * Checks if the given Font is capable of displaying some Chinese simplified characters.
     *
     * @param font the font to check
     * @return true if the font is a simplified Font
     */
    static public boolean isSimplifiedFont(Font font) {
        return canDisplay(font, SIMPLIFIED_CHARACTERS);
    }

    /**
     * Checks if the given Font is capable of displaying some Chinese traditional characters.
     *
     * @param font the font to check
     * @return true if the font is a traditional Font
     */
    static public boolean isTraditionalFont(Font font) {
        return canDisplay(font, TRADITIONAL_CHARACTERS);
    }

    static private boolean canDisplay(Font font, String testString) {

        try {
            return -1 == font.canDisplayUpTo(testString);
        }
        catch (Exception e) {
            logger.info("JDK error with " + font.getName());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the given Font is a known "preferred" Font.
     *
     * @param font the font
     * @return true if the Font is a "preferred" font
     */
    static private boolean isPreferredFont(Font font) {

        for (String fontName : PREFERRED_FONTS) {
            // Checks the name against known names, and then checks that the
            // font is actually capable of displaying Chinese in case some
            // non-chinese Font had a "preferred" name.
            if (fontName.equals(font.getFontName()) && isSimplifiedFont(font) && isTraditionalFont(font))
                return true;
        }
        return false;
    }
}
