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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *	@author Jordan Kiang
 *	
 *	A Swing component for selecting a font.
 *
 *  Allows selection of Font, font size, and font style.
 * 	Font and font size are selectable through JList select boxes and JTextFields so that the font or size
 *  can be chosen either from selecting from the list or typing the name of the font manually.
 * 	Font style is limited to bold and italic options, selectable through check boxes.
 *
 *	Also supports FontFilters, which allow the set of available fonts to be adjusted dynamically.
 * 	A check box is provided for each of the given filters.  Checking the box applies the filters
 *  and removes those Fonts that are filtered out from the Font list.
 * 
 * 	Can be used as a stand-alone JComponent directly via constructors, or through a dialog via the static methods.
 * 	There are a whole range of possible constructor/static build method arguments, only implementing a couple right now.
 * 	Should be easy to adapt as needed.
 */
public class JFontChooser extends JComponent {

    private JTextField fontChooserField;	// the text field for the font name
    private JList fontChooserList;			// the list with font options
    private JTextListLink fontChooserLink;	// link the two
    private JTextField sizeChooserField;	// the text field for font size
    private JList sizeChooserList;			// the list with font size options
    private JTextListLink sizeChooserLink;	// link the two
    private JCheckBox boldCheckBox;			// checkbox for bold
    private JCheckBox italicCheckBox;		// checkbox for italic
    private JLabel previewLabel;			// preview area
    private Font[] fontOptions;				// array of all accessible fonts
    private Set appliedFilters;				// currently applied filters to determine which fonts are available
    
    ////////////////////
    // static build methods
    
    /**
     *	Static builder for a modal dialog box associated with the given component.
     *	Uses default options and all available fonts.
     * 
     *	@param owner the owner of the modal dialog
     *	@return the Font selected
     */
    static public Font showDialog(Component owner) {

        // default options
        Font initialFont = owner.getFont();	// initially set to the Font of the owner component
        String defaultPreviewString = "The quick brown fox jumps over the lazy dog.  123456790";
        int[] defaultSizeOptions = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
        Font[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        return JFontChooser.showDialog(owner, initialFont, systemFonts, defaultSizeOptions, new FontFilter[0], defaultPreviewString);
    }
    
    /**
     *	Static builder for a modal dialog box associated with the given components.
     *	Uses the provided options.
     * 
     *	@param owner owner the owner of the modal dialog
     *	@param initialFont the initially selected Font
     *	@param fontOptions the Fonts that should be available options
     *	@param sizeOptions the font sizes that should be options
     *  @param fontFilters filters which allow the font set to be dynamically adjusted
     *	@param previewString the text for previewing
     *
     *	@return the Font selected
     */
    static public Font showDialog(Component owner,
            					  Font initialFont,
            					  Font[] fontOptions,
            					  int[] sizeOptions,
            					  FontFilter[] fontFilters,
            					  String previewString) {
        
        JFontChooser fontChooser = new JFontChooser(initialFont, fontOptions, sizeOptions, fontFilters, previewString);
        FontReturner returner = fontChooser.new FontReturner(); // OK button listener
        FontChooserDialog dialog = new FontChooserDialog(owner, "Choose a Font", true, fontChooser, returner, null);        
        dialog.setLocation(owner.getLocationOnScreen());
        dialog.pack();
        dialog.setVisible(true);
        return returner.getFont();
    }
    
    ////////////////////
    
    /**
     *	Constructs a JFontChooser using the given options.
     * 
     *	@param initialFont the initially selected Font
     *	@param fontOptions the Fonts that should be available options
     *	@param sizeOptions the font sizes that should be options
     *	@param previewString the text for previewing
     */
    public JFontChooser(Font initialFont, Font[] fontOptions, int[] sizeOptions, FontFilter[] filters, String previewString) {
        this.init(initialFont, fontOptions, sizeOptions, filters, previewString);
    }
    
    /**
     *	A helper constructor method.  Separate so that multiple constructors could potentially make use of it.
     * 
     *	@param initialFont the initially selected Font
     *	@param fontOptions the Fonts that should be available options
     *	@param sizeOptions the font sizes that should be options
     *	@param previewString the text for previewing
     */
    private void init(Font initialFont, Font[] fontOptions, int[] sizeOptions, FontFilter[] filters, String previewString) {

        this.fontOptions = fontOptions;
        JComponent optionsPanel = this.buildOptionsPanel(initialFont, fontOptions, sizeOptions, filters);
        JComponent previewPanel = this.buildPreviewPanel(initialFont, previewString);
        this.setLayout(new BorderLayout());
        this.add(optionsPanel, BorderLayout.CENTER);
        this.add(previewPanel, BorderLayout.SOUTH);
    }

    /**
     *	Get a Font based on the selected values:
     *	1. The Font name selected in the list (the text in the field may not be a complete Font name).
     *	2. The Size from the size text field (may want a size other than in the list), uses the current preview size if invalid.
     * 	3. Bold if checked.
     * 	4. Italic if checked.
     * 
     *	@return a new Font instance constructed from the selected criteria.
     */
    public Font getSelectedFont() {

        Font font = null;
        // name from the selected value in the list
        String fontName = (String)JFontChooser.this.fontChooserList.getSelectedValue();
 
        // null font if nothing selected, so we return null
        if(null != fontName) {
	        // bitwise OR the style options
	        int style = Font.PLAIN;
	        style |= JFontChooser.this.boldCheckBox.isSelected() ? Font.BOLD : 0;
	        style |= JFontChooser.this.italicCheckBox.isSelected() ? Font.ITALIC : 0;
	        
	        int fontSize = -1;
	        try {
	            // size from the text field
	            fontSize = Integer.parseInt(JFontChooser.this.sizeChooserField.getText());
	        } catch(NumberFormatException nfe) {
	            // fontSize still -1
	        }
	        if(fontSize <= 0) {
	            // if an invalid size, use the size from the font loaded into the preview
	            fontSize = JFontChooser.this.previewLabel.getFont().getSize();
	        }
	        
	        font = new Font(fontName, style, fontSize);
        }
        
        return font;
    }

    /**
     *	Builds a JComponent encapsulating the font selection options.
     *	This method starts a heirarchy of build methods that constructs all the components of the whole selection panel.
     *	It is expected that calling this method will also initialize all the instance variable Swing components (JLists, JTextFields, etc).
     * 
     *	@param initialFont the initially selected Font
     *	@param fontOptions the Fonts that should be available options
     *	@param sizeOptions the font sizes that should be options
     *	@return the JComponent
     */
    private JComponent buildOptionsPanel(Font initialFont, Font[] fontOptions, int[] sizeOptions, FontFilter[] filters) {

        // chooserPanel wraps the Font, font size, and font style selection options.
        JPanel chooserPanel = new JPanel();
        chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.X_AXIS));
        
        // three subpanels, one for font selection, one for size selection, one for style selection
        JComponent fontChooser = this.buildFontChooserPanel(initialFont, fontOptions);
        JComponent sizeChooser = this.buildSizeChooserPanel(initialFont, sizeOptions);
        JComponent styleChooser = this.buildStyleChooserPanel(initialFont);

        // lay out three sub panels side by side
        chooserPanel.add(fontChooser);
        chooserPanel.add(sizeChooser);
        chooserPanel.add(styleChooser);
        
        // If there are filters, then we want to also include an extra panel with the filter options.
        // We create a new JPanel to wrap the chooserPanel and another sub panel that contains the filters.
        // If there are no filters then we don't need to bother and can just return the chooserPanel.
        
        JComponent returnComponent = null;
        if(filters.length > 0) {
            JComponent filterChooser = this.buildFilterChooser(filters);
            filterChooser.setBorder(BorderFactory.createTitledBorder("Filters"));
            JPanel optionsWrapperPanel = new JPanel(new BorderLayout());
            optionsWrapperPanel.add(chooserPanel, BorderLayout.CENTER);
            optionsWrapperPanel.add(filterChooser, BorderLayout.SOUTH);   
            returnComponent = optionsWrapperPanel;
        } else {
            returnComponent = chooserPanel;
        }
        this.loadFilteredFonts(initialFont);
        this.setupFontSelectionListener();
        return returnComponent;
    }
    
    /**
     * Sets up the listeners to tie the preview label to the font selections.
     * An action on one of the font options will set the preview label font to the selected settings.
     */
    private void setupFontSelectionListener() {

        // One listener registered with all the components.
        FontSelectionListener fontSelectionListener = new FontSelectionListener();
        this.fontChooserField.getDocument().addDocumentListener(fontSelectionListener);
        this.fontChooserList.addListSelectionListener(fontSelectionListener);
        this.sizeChooserField.getDocument().addDocumentListener(fontSelectionListener);
        this.sizeChooserList.addListSelectionListener(fontSelectionListener);
        this.boldCheckBox.addActionListener(fontSelectionListener);
        this.italicCheckBox.addActionListener(fontSelectionListener);
    }
    
    /**
     *	Sets up a JComponent encapsulating the Font name options (text field with list)
     * 
     *	@param initialFont the initially selected Font
     *	@param fontOptions the Fonts that should be available options
     *	@return the JComponent
     */
    private JComponent buildFontChooserPanel(Font initialFont, Font[] fontOptions) {

        this.fontChooserField = new JTextField(initialFont.getName());
        JComponent fontChooserListScrollPane = this.buildFontChooserListPane();
        // link up the contents of the JList to the JTextField
        this.fontChooserLink = new JTextListLink(this.fontChooserField, this.fontChooserList, true);
        JPanel fontChooserPanel = new JPanel(new BorderLayout());
        fontChooserPanel.add(this.fontChooserField, BorderLayout.NORTH);
        fontChooserPanel.add(fontChooserListScrollPane, BorderLayout.CENTER);
        fontChooserPanel.setBorder(BorderFactory.createTitledBorder("Font"));
        return fontChooserPanel;
    }
    
    /**
     * 	Sets up a component encapsulating the font options JList
     *	@return the a JScrollPane wrapping the JList
     */
    private JComponent buildFontChooserListPane() {

        this.fontChooserList = new JList();
        this.fontChooserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // wrap JList in a JScrollPane
        JScrollPane fontChooserListScrollPane = new JScrollPane(this.fontChooserList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return fontChooserListScrollPane;
    }
    
    /**
     *  Inspects all the fonts to see which ones pass all the filters.
     *  Those fonts are then loaded into the Font selection JList.
     *  This method needs to be called after the JList is instantiated.
     */
    private void loadFilteredFonts(Font initialFont) {

        List filteredFonts = new ArrayList();
        // check which Fonts are included by all filters
        for(int i = 0; i < this.fontOptions.length; i++) {
            boolean shouldInclude = true;
            for(Iterator filterIter = this.appliedFilters.iterator(); filterIter.hasNext();) {
                FontFilter filter = (FontFilter)filterIter.next();
                if(!filter.shouldInclude(this.fontOptions[i])) {
                    shouldInclude = false;
                    break;
                }
            }
            if(shouldInclude) {
                filteredFonts.add(this.fontOptions[i]);
            }
        }
        
        String initialFontName = null != initialFont ? initialFont.getName() : null;
        
        // copy the font names into a String array
        String[] fontNames = new String[filteredFonts.size()];
        Iterator filteredIter = filteredFonts.iterator();
        boolean initialFontInList = false;
        for(int i = 0; filteredIter.hasNext(); i++) {
            Font font = (Font)filteredIter.next();
            fontNames[i] = font.getName();
            if(fontNames[i].equals(initialFontName)) {
                initialFontInList = true;
            }
        }
        
        // sort alphabetically ignoring case
        Arrays.sort(fontNames, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        
        // load the filtered fonts into the JList
        this.fontChooserList.setListData(fontNames);
    
        if(initialFontInList) {
            this.fontChooserList.setSelectedValue(initialFontName, true);
        } else {
            this.fontChooserField.setText("");
        }
    }
    
    /**
     *	Sets up a JComponent encapsulating the size options (text field with list)
     *	
     *	@param initialFont the initially selected Font
     *	@param sizeOptions the font sizes that should be options
     *	@return the JComponent
     */
    private JComponent buildSizeChooserPanel(Font initialFont, int[] sizeOptions) {
        int initialSize = initialFont.getSize();
        this.sizeChooserField = new JTextField(Integer.toString(initialSize));
        JComponent sizeChooserListScrollPane = this.buildSizeChooserList(initialSize, sizeOptions);
        
        // link up the contents of the JList to the JTextField
        this.sizeChooserLink = new JTextListLink(this.sizeChooserField, this.sizeChooserList, true);
        
        JPanel sizeChooserPanel = new JPanel(new BorderLayout());
        sizeChooserPanel.add(this.sizeChooserField, BorderLayout.NORTH);
        sizeChooserPanel.add(sizeChooserListScrollPane, BorderLayout.CENTER);
        sizeChooserPanel.setBorder(BorderFactory.createTitledBorder("Size"));
        
        return sizeChooserPanel;
    }

    /**
     * 	Sets up a component encapsulating the size options JList	
     * 
     *	@param initialFont the initially selected Font
     *	@param sizeOptions the font sizes that should be options
     *	@return the a JScrollPane wrapping the JList
     */
    private JComponent buildSizeChooserList(int initialSize, int[] sizeOptions) {
        // Use string representations of the sizes.  Facilitates comparison of the text field to the list.
        String[] sizeOptionStrings = new String[sizeOptions.length];
        for(int i = 0; i < sizeOptions.length; i++) {
            sizeOptionStrings[i] = Integer.toString(sizeOptions[i]);
        }
        
        this.sizeChooserList = new JList(sizeOptionStrings);
        this.sizeChooserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // wrap JList in a JScrollPane
        JScrollPane sizeChooserListScrollPane = new JScrollPane(this.sizeChooserList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return sizeChooserListScrollPane;
    }
    
    /**
     *	Builds a JComponent encapsulating the style options (checkboxes for bold and italic).
     * 
     *	@param initialFont the initially selected Font
     *	@return the JComponent
     */
    private JComponent buildStyleChooserPanel(Font initialFont) {
        this.boldCheckBox = new JCheckBox("Bold", initialFont.isBold());
        this.italicCheckBox = new JCheckBox("Italics", initialFont.isItalic());
    
        JPanel styleChooserPanel = new JPanel();
        styleChooserPanel.setLayout(new BoxLayout(styleChooserPanel, BoxLayout.Y_AXIS));
        
        styleChooserPanel.add(this.boldCheckBox);
        styleChooserPanel.add(this.italicCheckBox);
        styleChooserPanel.setBorder(BorderFactory.createTitledBorder("Style"));
        
        return styleChooserPanel;
    }
    
    /**
     * Builds a JComponent that encapsulates the Font filters.
     * Each filter has a check box for applying/unapplying that filter.
     * 
     * @param filters the filters
     * @return the JComponent
     */
    private JComponent buildFilterChooser(FontFilter[] filters) {
        this.appliedFilters = new HashSet();
        
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());

        // for each filter, make a check box and add it to the panel
        for(int i = 0; i < filters.length; i++) {
            if(filters[i].isDefaultOn()) {
                this.appliedFilters.add(filters[i]);
            }
            
            JCheckBox filterCheckBox = this.buildFilterCheckBox(filters[i]);
            filterPanel.add(filterCheckBox);
        }
        
        return filterPanel;
    }
    
    /**
     * Build a JCheckBox for applying the given FontFilter
     * @param filter the filter
     * @return the JCheckBox
     */
    private JCheckBox buildFilterCheckBox(final FontFilter filter) {
        
        // each check box gets its own anonymous ActionListener
        ActionListener checkBoxListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox source = (JCheckBox)e.getSource();
                
                // depending on if the check box is selected/deselected,
                // we add or remove the filter from the applied filters
                if(source.isSelected()) {
                    JFontChooser.this.appliedFilters.add(filter);
                } else {
                    JFontChooser.this.appliedFilters.remove(filter);
                }
                
                // need to reload for the changes to take affect
                JFontChooser.this.loadFilteredFonts(JFontChooser.this.getSelectedFont());
            }
        };
        
        JCheckBox filterCheckBox = new JCheckBox(filter.getDisplayName(), filter.isDefaultOn());
        filterCheckBox.addActionListener(checkBoxListener);
        
        return filterCheckBox;
    }

    /**
     *	Build a JComponent for previewing the Font with the current settings.
     * 
     *	@param initialFont the initially selected Font
     *	@param previewString the text to preview Fonts with
     *	@return the JComponent
     */
    private JComponent buildPreviewPanel(Font initialFont, String previewString) {
        this.previewLabel = new JLabel(previewString);
        
        previewLabel.setFont(initialFont);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createTitledBorder("Preview"));
        
        // wrap the JLabel in a JScrollPane
        JScrollPane previewScrollPane = new JScrollPane(this.previewLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        return previewScrollPane;
    }
    
    ////////////////////
    // helper internal classes
    
    /**
     *	A listener to be registered with all of the selection components.
     *	An event on anyone of them will cause the preview label to set its font to the selected settings.
     */
    private class FontSelectionListener implements DocumentListener, ListSelectionListener, ActionListener {
        
        /**
         *	A common action for all events.  Update the preview Font.
         */
        public void updatePreviewFont() {
            JFontChooser.this.previewLabel.setFont(JFontChooser.this.getSelectedFont());
        }
       
        /**
         * The Font or size JTextField's contents changed.
         * @see DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e) {
            this.updatePreviewFont();
        }

        /**
         * The Font or size JTextField's contents changed.
         * @see DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
            this.updatePreviewFont();
        }

        /**
         * The Font or size JTextField's contents changed.
         * @see DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {
            this.updatePreviewFont();
        }
        
        /**
         * The Font or size JList's selection changed.
         * @see ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent);
         */
        public void valueChanged(ListSelectionEvent e) {
            this.updatePreviewFont();
        }
        
        /**
         * One of the style checkboxes was clicked.
         * @see ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            this.updatePreviewFont();
        }
    }
    
    /**
     *	A Dialog class for use in the static showDialog methods.
     *	Builds a Dialog containing a JFontChooser, an OK button, and a cancel button.
     */
    static private class FontChooserDialog extends JDialog {
        
        /**
         * Create a new Dialog with the given settings.
         * 
         * @param owner the parent of this dialog
         * @param title the title to use for the dialog
         * @param modal true if the dialog is modal, false otherwise
         * @param fontChooser the JFontChooser to load into the Dialog
         * @param okListener a listener to register with the OK button
         * @param cancelListener a listener to register with the cancel button
         */
        private FontChooserDialog(Component owner, String title, boolean modal, JFontChooser fontChooser, ActionListener okListener, ActionListener cancelListener) {
            super(JOptionPane.getFrameForComponent(owner), title, modal);
            
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");
            
            ActionListener hideListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                   FontChooserDialog.this.setVisible(false);
                }
            };
            
            // close the dialog when either button is pressed
            okButton.addActionListener(hideListener);
            cancelButton.addActionListener(hideListener);
            
            // if external listeners were supplied, register them as well
            if(null != okListener) {
                okButton.addActionListener(okListener);
            }
            if(null != cancelListener) {
                cancelButton.addActionListener(cancelListener);
            }
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            Container contentPane = this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(fontChooser, BorderLayout.CENTER);
            contentPane.add(buttonPanel, BorderLayout.SOUTH);
        }
    }
    
    /**
     *	A listener that is called when OK button is pressed on the FontChooserDialog.
     *	When the button is pressed, this font stores the selected Font.
     *	Then the modal dialog closes, and the static showDialog method returns the Font set in this listener.
     */
    private class FontReturner implements ActionListener {
        private Font font;
        
        /**
         * The OK button was pressed.
         * @see ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            this.font = JFontChooser.this.getSelectedFont();
        }
        
        /**
         * @return the selected Font
         */
        public Font getFont() {
            return this.font;
        }
    }
    
    ///////////////////
    
    /**
     *  FontFilters allow the Fonts available in the JFontChooser to be dynamically changed.
     *  For example you could have a filters that filtered out fonts that didn't support
     *  a particular unicode character range, and then allow the user to filter for such fonts.
     */
    static public interface FontFilter {
        
        /**
         * @return the name of the filter, shown next to its check box
         */
        public String getDisplayName();
        
        /**
         * @return true if the filter should be enabled by default
         */
        public boolean isDefaultOn();
        
        /**
         * The filtering method.
         * Only fonts for which this method returns true are included if this filter is applied.
         * 
         * @param font the Font
         * @return true if this font should be included, false otherwise
         */
        public boolean shouldInclude(Font font);
    }
    
    static public void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.pack();
        frame.setVisible(true);
        
        Font font = new Font("Simsun", Font.PLAIN, 36);
        int[] defaultSizeOptions = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
        String defaultPreviewString = "\u6c49  \u6f22";
        
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        ArrayList chineseFonts = new ArrayList();
        for(int i = 0; i < fonts.length; i++) {
            if(fonts[i].canDisplay('\u6c49') || fonts[i].canDisplay('\u6f22')) {
                chineseFonts.add(fonts[i]);
            }
        }
        Font[] chinFonts = new Font[chineseFonts.size()];
        Iterator fontIter = chineseFonts.iterator();
        for(int i = 0; fontIter.hasNext(); i++) {
            chinFonts[i] = (Font)fontIter.next();
        }
        
        FontFilter testFilter1 = new FontFilter() {
            public boolean isDefaultOn() {
                return true;
            }
            
            public boolean shouldInclude(Font font) {
                return font.canDisplay('\u1000');
                
                //return font.canDisplay('\u6c49');
            }
            
            public String getDisplayName() {
                return "Test1";
            }
        };
        
        FontFilter testFilter2 = new FontFilter() {
            public boolean isDefaultOn() {
                return false;
            }
            
            public boolean shouldInclude(Font font) {
                return font.canDisplay('\u6f22');
            }
            
            public String getDisplayName() {
                return "Test2";
            }
        };
        
        //font = JFontChooser.showDialog(frame, font, fonts, defaultSizeOptions, defaultPreviewString, new FontFilter[0]);
        
        font = JFontChooser.showDialog(frame, font, fonts, defaultSizeOptions, new FontFilter[] {testFilter1, testFilter2}, defaultPreviewString);
        return;
    }
}
