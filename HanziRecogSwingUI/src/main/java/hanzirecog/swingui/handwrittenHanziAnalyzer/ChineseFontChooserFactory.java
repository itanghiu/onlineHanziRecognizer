/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-hanzirecog.swingui.uicommon.org
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

package hanzirecog.swingui.handwrittenHanziAnalyzer;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import hanzirecog.swingui.uicommon.ChineseFontFinder;
import hanzirecog.swingui.handwrittenHanziAnalyzer.JFontChooser.FontFilter;

/**
 * Defines some methods for building instances of a JFontChooser that can filter for
 * those Fonts that support Simplified and Traditional chinese character sets.
 * 
 * This is a factory and does not extend JFontChooser since there really is nothing to extend.
 */
public class ChineseFontChooserFactory {

    /**
     * Uses a SimplifiedFontFilter and a TraditionalFontFilter when invoking other methods.
     * 
     * @see JFontChooser#showDialog(Component)
     * @see ChineseFontChooserFactory#showDialog(Component, Font, Font[], int[], String)
     */
    static public Font showDialog(Component owner) {
        // default options
        Font initialFont = owner.getFont();	// initially set to the Font of the owner component
        String defaultPreviewString = "\u6c49  \u6f22";
        int[] defaultSizeOptions = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
        Font[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        return showDialog(owner, initialFont, systemFonts, defaultSizeOptions, defaultPreviewString);
    }
    
    /**
     * Uses a SimplifiedFontFilter and a TraditionalFontFilter when invoking other methods.
     * 
     * @see JFontChooser#showDialog(Component, Font, Font[], int[], FontFilter[], String)
     */
    static public Font showDialog(Component owner,
            					  Font initialFont,
            					  Font[] fontOptions,
            					  int[] sizeOptions,
            					  String previewString) {
        
        FontFilter[] chineseFilters = getChineseFilters(initialFont);
        return JFontChooser.showDialog(owner, initialFont, fontOptions, sizeOptions, chineseFilters, previewString);
    }
    
    /**
     * Gets an instance of a JFontChooser using a SimplifiedFontFilter and a TraditionalFontFilter.
     * 
     * @see JFontChooser#JFontChooser(Font, Font[], int[], FontFilter[], String)
     */
    static JFontChooser getInstance(Font initialFont, Font[] fontOptions, int[] sizeOptions, String previewString) {

        FontFilter[] chineseFilters = getChineseFilters(initialFont);
        return new JFontChooser(initialFont, fontOptions, sizeOptions, chineseFilters, previewString);
    }
    
    /**
     * @param initialFont the initialFont to use
     * @return a prepared set of filters for filter by Simplified and Traditional characters
     */
    static private FontFilter[] getChineseFilters(Font initialFont) {
        return new FontFilter[] {new SimplifiedFontFilter(initialFont), new TraditionalFontFilter(initialFont)};
    }
    
    /**
     * Filters fonts for those that support some common Simplified characters.
     */
    static private class SimplifiedFontFilter implements FontFilter {

        private Font initialFont;
        
        private SimplifiedFontFilter(Font initialFont) {
            this.initialFont = initialFont;
        }
        
        public String getDisplayName() {
            return "Simplified";
        }
        
        public boolean isDefaultOn() {
            return this.shouldInclude(this.initialFont);
        }
        
        public boolean shouldInclude(Font font) {
            return ChineseFontFinder.isSimplifiedFont(font);
        }
    }
    
    /**
     * Filters fonts for those that support some common Traditinal characters.
     */
    static private class TraditionalFontFilter implements FontFilter {

        private Font initialFont;
        
        private TraditionalFontFilter(Font initialFont) {
            this.initialFont = initialFont;
        }
        
        public String getDisplayName() {
            return "Traditional";
        }
        
        public boolean isDefaultOn() {
            return this.shouldInclude(this.initialFont);
        }
        
        public boolean shouldInclude(Font font) {
            return ChineseFontFinder.isTraditionalFont(font);
        }
        
    }
    
}
