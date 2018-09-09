/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
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
package swingui.uicommon;

import engine.util.HanziLookupBundleKeys;
import engine.enums.CharacterType;
import swingui.handwrittenHanziAnalyzer.ChineseFontChooserFactory;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * @author jkiang
 * <p>
 * The lookup panel component can be integrated into other apps.
 * This class offers some static methods required or useful that those apps
 * can call to build various associated UI elements (menus, option dialogs, etc).
 */
public class HanziLookupUIBuilder {

    /**
     * Builds a menu item for settings.  Includes:
     * 1. a character type submenu (traditional, simplified, both).
     * 2. an item for opening a lookup options dialog box
     * 3. an item for choosing the font
     *
     * @param lookup
     * @param containers
     * @return
     */
    static public JMenu buildOptionsMenu(HanziLookup lookup, Collection<Container> containers, ResourceBundle bundle) {

        JMenu optionsMenu = new JMenu(bundle.getString(HanziLookupBundleKeys.SETTINGS_BUNDLE_KEY));
        JMenu modeSubMenu = buildCharacterModeMenu(lookup, bundle);
        optionsMenu.add(modeSubMenu);

        JMenuItem lookupOptionItem = buildLookupOptionMenuItem(lookup, bundle);
        optionsMenu.add(lookupOptionItem);

        JMenuItem fontItem = buildFontMenuItem(lookup, containers, bundle);
        optionsMenu.add(fontItem);

        return optionsMenu;
    }

    /**
     * Builds a JMenu that has options for choosing the character search type
     * of the given lookupComponent (simplified characters, traditional characters, or both).
     *
     * @param lookup the lookup component
     * @return the JMenu
     */
    static public JMenu buildCharacterModeMenu(final HanziLookup lookup, ResourceBundle bundle) {

        CharacterType searchType = lookup.getSearchType();

        // Need to declare as final so we can use them in the the anonymous listener definition.
        final JRadioButtonMenuItem simplifiedButton = new JRadioButtonMenuItem(bundle.getString(HanziLookupBundleKeys.SIMPLIFIED_TYPE_BUNDLE_KEY), searchType.isSimplified());
        final JRadioButtonMenuItem traditionalButton = new JRadioButtonMenuItem(bundle.getString(HanziLookupBundleKeys.TRADITIONAL_TYPE_BUNDLE_KEY), searchType.isTraditional());
        final JRadioButtonMenuItem bothButton = new JRadioButtonMenuItem(bundle.getString(HanziLookupBundleKeys.BOTH_TYPES_BUNDLE_KEY), searchType.isGeneric());

        simplifiedButton.addActionListener((e) -> lookup.setSearchType(CharacterType.SIMPLIFIED_TYPE));
        traditionalButton.addActionListener((e) -> lookup.setSearchType(CharacterType.TRADITIONAL_TYPE));
        bothButton.addActionListener((e) -> lookup.setSearchType(CharacterType.GENERIC_TYPE));

        JMenu charModeMenu = new JMenu(bundle.getString(HanziLookupBundleKeys.CHARACTER_TYPE_BUNDLE_KEY));
        charModeMenu.add(simplifiedButton);
        charModeMenu.add(traditionalButton);
        charModeMenu.add(bothButton);

        // ButtonGroup links buttons so toggling one will untoggle the others.
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(simplifiedButton);
        modeGroup.add(traditionalButton);
        modeGroup.add(bothButton);

        return charModeMenu;
    }

    /**
     * Builds a menu item for opening a lookup options dialog box.
     *
     * @param lookup the lookup component that the dialog box modifies
     * @return the menu item
     */
    static public JMenuItem buildLookupOptionMenuItem(final HanziLookup lookup, final ResourceBundle bundle) {

        JMenuItem lookupOptionsMenu = new JMenuItem(bundle.getString(HanziLookupBundleKeys.LOOKUP_OPTIONS_BUNDLE_KEY));
        lookupOptionsMenu.addActionListener((e) -> {
            JDialog optionsDialog = buildLookupOptionDialog(lookup, bundle);
            optionsDialog.setVisible(true);
        });
        return lookupOptionsMenu;
    }

    /**
     * Builds a menu item for selecting the Font.
     *
     * @param lookup     the lookup component whose Font is assigned
     * @param containers any other Containers that should have their font set, can be null
     * @return the menu item
     */
    static public JMenuItem buildFontMenuItem(final HanziLookup lookup, final Collection<Container> containers, ResourceBundle bundle) {

        JMenuItem fontItem = new JMenuItem(bundle.getString(HanziLookupBundleKeys.CHOOSE_FONT_BUNDLE_KEY));
        fontItem.addActionListener((e) -> {
            Font font = ChineseFontChooserFactory.showDialog(lookup);
            // null is returned if nothing was selected or cancel hit
            if (font == null) return;
            lookup.setFont(font);
            if (null == containers) return;
            containers.forEach((container) -> container.setFont(font));
        });
        return fontItem;
    }

    /**
     * Builds a dialog for setting the options on the given lookup component.
     *
     * @param lookup the lookup component to be set
     * @return the dialog
     */
    static public JDialog buildLookupOptionDialog(final HanziLookup lookup, ResourceBundle bundle) {

        final JDialog optionsDialog = new JDialog();
        optionsDialog.setTitle(bundle.getString(HanziLookupBundleKeys.OPTIONS_BUNDLE_KEY));
        optionsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container contentPane = optionsDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Check box to decide of there is auto lookup after each stroke.
        JCheckBox autoLookupCheckBox = buildAutoLookupCheckBox(lookup, bundle);
        autoLookupCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        autoLookupCheckBox.setSelected(lookup.getAutoLookup());
        contentPane.add(autoLookupCheckBox);

        // spacing
        contentPane.add(Box.createVerticalStrut(20));

        // label and slider to set "looseness" of the lookup
        JLabel loosenessLabel = new JLabel(bundle.getString(HanziLookupBundleKeys.LOOKUP_LOOSENESS_BUNDLE_KEY));
        loosenessLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(loosenessLabel);

        JSlider loosenessSlider = buildLoosenessSlider(lookup);
        loosenessSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(loosenessSlider);

        // spacing
        contentPane.add(Box.createVerticalStrut(20));

        // label and spinner to set how many characters are returned for each match
        JLabel matchCountLabel = new JLabel(bundle.getString(HanziLookupBundleKeys.MATCH_COUNT_BUNDLE_KEY));
        matchCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(matchCountLabel);

        final JSpinner resultsSpinner = new JSpinner(new SpinnerNumberModel(lookup.getNumResults(), 1, 100, 1));
        resultsSpinner.addChangeListener((e) -> {
            Integer numResults = (Integer) resultsSpinner.getValue();
            lookup.setNumResults(numResults.intValue());
        });
        resultsSpinner.setMaximumSize(new Dimension(50, 25));
        contentPane.add(resultsSpinner);

        // spacing
        contentPane.add(Box.createVerticalStrut(20));

        KeyStroke lookupMacro = lookup.getLookupMacro();
        KeyStroke clearMacro = lookup.getClearMacro();
        KeyStroke undoMacro = lookup.getUndoMacro();

        String typeMacro = bundle.getString(HanziLookupBundleKeys.TYPE_MACRO_BUNDLE_KEY);
        String lookupMacroText = null != lookupMacro ? getKeyStrokeText(lookupMacro.getKeyCode(), lookupMacro.getModifiers()) : typeMacro;
        String clearMacroText = null != clearMacro ? getKeyStrokeText(clearMacro.getKeyCode(), clearMacro.getModifiers()) : typeMacro;
        String undoMacroText = null != undoMacro ? getKeyStrokeText(undoMacro.getKeyCode(), undoMacro.getModifiers()) : typeMacro;

        JTextField lookupMacroField = new JTextField(lookupMacroText, 10);
        JTextField clearMacroField = new JTextField(clearMacroText, 10);
        JTextField undoMacroField = new JTextField(undoMacroText, 10);

        MacroKeyListener macroKeyListener = new MacroKeyListener(lookupMacroField, clearMacroField, undoMacroField, lookup);
        lookupMacroField.addKeyListener(macroKeyListener);
        clearMacroField.addKeyListener(macroKeyListener);
        undoMacroField.addKeyListener(macroKeyListener);

        JLabel lookupMacroLabel = new JLabel(bundle.getString(HanziLookupBundleKeys.LOOKUP_MACRO_BUNDLE_KEY));
        JLabel clearMacroLabel = new JLabel(bundle.getString(HanziLookupBundleKeys.CLEAR_MACRO_BUNDLE_KEY));
        JLabel undoMacroLabel = new JLabel(bundle.getString(HanziLookupBundleKeys.UNDO_MACRO_BUNDLE_KEY));

        lookupMacroLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clearMacroLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        undoMacroLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel macroPanel = new JPanel(new GridLayout(3, 2));
        macroPanel.add(lookupMacroLabel);
        macroPanel.add(lookupMacroField);
        macroPanel.add(clearMacroLabel);
        macroPanel.add(clearMacroField);
        macroPanel.add(undoMacroLabel);
        macroPanel.add(undoMacroField);

        contentPane.add(macroPanel);

        // spacing
        contentPane.add(Box.createVerticalStrut(20));

        JButton okButton = new JButton(bundle.getString(HanziLookupBundleKeys.OK_BUNDLE_KEY));
        okButton.setAlignmentX(JDialog.CENTER_ALIGNMENT);
        okButton.addActionListener((e) -> optionsDialog.dispose());
        contentPane.add(okButton);
        optionsDialog.pack();

        setChildComponentPosition(lookup, optionsDialog);
        return optionsDialog;
    }

    static public void setChildComponentPosition(Component parentWindow, Component childWindow) {

        // set the location over the existing lookup component
        Point parentLocation = parentWindow.getLocationOnScreen();
        int dialogX = (int) ((parentLocation.getX() + (parentWindow.getWidth() / 2)) - (childWindow.getWidth() / 2));
        int dialogY = (int) ((parentLocation.getY() + (parentWindow.getHeight() / 2)) - (childWindow.getHeight() / 2));
        childWindow.setLocation(dialogX, dialogY);
    }

    static public JMenuItem buildSaveOptionsMenuItem(final HanziLookup lookup, ResourceBundle bundle) {

        JMenuItem fontItem = new JMenuItem(bundle.getString(HanziLookupBundleKeys.SAVE_SETTINGS_BUNDLE_KEY));
        fontItem.addActionListener((e) -> { });
        return fontItem;
    }

    static private String getKeyStrokeText(int keyCode, int modifiers) {

        String keyStrokeText = modifiers != 0 ? KeyEvent.getKeyModifiersText(modifiers) + " " : "";
        keyStrokeText += KeyEvent.getKeyText(keyCode);
        return keyStrokeText;
    }

    /**
     * Builds a check box for determining whether automatic lookup occurs after each stroke
     *
     * @param lookup the lookup component
     * @return the check box
     */
    static public JCheckBox buildAutoLookupCheckBox(final HanziLookup lookup, ResourceBundle bundle) {

        final JCheckBox autoLookupCheckBox = new JCheckBox(bundle.getString(HanziLookupBundleKeys.AUTO_LOOKUP_BUNDLE_KEY));
        autoLookupCheckBox.addActionListener((e) -> lookup.setAutoLookup(autoLookupCheckBox.isSelected()));
        return autoLookupCheckBox;
    }

    /**
     * Builds a slider for setting the "looseness" of a lookup.
     *
     * @param lookup the lookup component
     * @return the slider
     */
    static public JSlider buildLoosenessSlider(final HanziLookup lookup) {

        final JSlider loosenessSlider = new JSlider(0, 20);
        int initialValue = (int) (loosenessSlider.getMaximum() * lookup.getLooseness());
        loosenessSlider.setValue(initialValue);
        loosenessSlider.addChangeListener((e) -> {
                int sliderMax = loosenessSlider.getMaximum();
                int sliderValue = loosenessSlider.getValue();
                double looseness = (double) sliderValue / sliderMax;
                lookup.setLooseness(looseness);
        });
        return loosenessSlider;
    }

    /**
     * Attach this listener to the macro option text fields.
     * They'll listen to the key inputs and register them with as the appropriate macro.
     */
    static private class MacroKeyListener implements KeyListener {

        private JTextField lookupField;
        private JTextField clearField;
        private JTextField undoField;

        private HanziLookup hanziLookup;

        private MacroKeyListener(JTextField lookupField, JTextField clearField, JTextField undoField, HanziLookup hanziLookup) {

            this.lookupField = lookupField;
            this.clearField = clearField;
            this.undoField = undoField;
            this.hanziLookup = hanziLookup;
        }

        public void keyPressed(KeyEvent ke) {

            int keyCode = ke.getKeyCode();
            int modifiers = ke.getModifiers();

            if (KeyEvent.VK_UNDEFINED != keyCode && !this.isModifier(keyCode)) {
                Object eventSource = ke.getSource();
                String keyText = getKeyStrokeText(keyCode, modifiers);
                KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);

                if (eventSource == this.lookupField) {
                    this.hanziLookup.registerLookupMacro(keyStroke);
                    this.lookupField.setText(keyText);
                }
                else if (eventSource == this.clearField) {
                    this.hanziLookup.registerClearMacro(keyStroke);
                    this.clearField.setText(keyText);
                }
                else {
                    this.hanziLookup.registerUndoMacro(keyStroke);
                    this.undoField.setText(keyText);
                }
            }
            ke.consume();
        }

        public void keyReleased(KeyEvent ke) {
            ke.consume();
        }

        public void keyTyped(KeyEvent ke) {
            ke.consume();
        }

        private boolean isModifier(int keyCode) {

            return KeyEvent.VK_CONTROL == keyCode ||
                    KeyEvent.VK_ALT == keyCode ||
                    KeyEvent.VK_META == keyCode ||
                    KeyEvent.VK_SHIFT == keyCode;
        }
    }
}
