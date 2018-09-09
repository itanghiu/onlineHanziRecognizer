/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-swingui.uicommon.org
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

package engine.service.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import engine.beans.TypeDescriptor;
import engine.enums.CharacterType;

/**
 * @author Jordan Kiang
 *
 *  Refactorized by I-Tang HIU August 2018
 *
 * (c) 2005
 * <p>
 * CharacterTypeParser parses an input stream (file) defining types and relationships between Chinese characters.
 * The result of parsing is a Map that maps from a Character to a CharacterTypeRepository.TypeDescriptor.
 * <p>
 * After parsing, buildCharacterTypeRepository can be called.
 * It instantiates and returns a new CharacterTypeRepository using the parsed recognizer.
 * Once the CharacterTypeRepository has been retrieved, the CharacterTypeParser is no longer needed.
 * @see BaseParser
 * @see CharacterTypeRepository
 */
public class CharacterTypeParser extends BaseParser {

    // Data stuffed into the typeMap, can be retreived via getTypeMap after parsing.
    private Map<Character, TypeDescriptor> typeMap = new HashMap();

    // Regular expression line pattern we expect the line to conform to.  Used to verify the format of the line and to retrieve groups.
    // We store this as an instance variable so we don't instantiate a new Pattern for each parsed line.
    private Pattern linePattern = Pattern.compile("^([a-f0-9]{4})\\s*\\|\\s*(\\d)(\\s*\\|\\s*([a-f0-9]{4}))?\\s*$");

    /**
     * Builds and parses type information by reading from the given typeStream.
     * The given input stream does not need to be buffered, as it will be wrapped
     * in a BufferedInputStream in the super class parse implementation.
     * Note that this the stream is closed once parsing is finished.
     *
     * @param typeStreamIn the InputStream to read the type recognizer from
     * @see BaseParser#parse(InputStream)
     */
    public CharacterTypeParser(InputStream typeStreamIn) throws IOException {

        try {
            parse(typeStreamIn);
            typeStreamIn.close();
        }
        catch (IOException ioe) {
            IOException thrownIOE = new IOException("Error reading character type recognizer!");
            thrownIOE.initCause(ioe);
            throw thrownIOE;
        }
    }

    /**
     * Builds a CharacterTypeRepository using the map that was parsed.
     * Calling this method before parsing takes place, or after a failed parse will give undefined results.
     *
     * @return a new CharacterTypeRepository
     */
    public CharacterTypeRepository buildCharacterTypeRepository() {

        CharacterTypeRepository typeRepository = new CharacterTypeRepository(typeMap);
        return typeRepository;
    }

    /**
     * Parses a line of type recognizer.
     * Each line should correspond to one CharacterTypeRepository.TypeDescriptor.
     * If the parsing is successful, the map will be updated with the newly parsed TypeDescriptor.
     * <p>
     * Each line of the input file should define one type/relationship.  The format of a line is:
     * [unicode] | [type] (| [altunicode])?
     * <p>
     * [unicode] and [altunicode] should be unicode code points.
     * <p>
     * [type] should be one of the following:
     * [type] 0 indicates that the unicode is generic to both the simplified and traditional character sets.
     * example: 4e00 | 0
     * <p>
     * [type] 1 indicates that the [unicode] on the left is a simplified form of the [altunicode] traditional form on the right.
     * example: 6c49 | 1 | 6f22
     * <p>
     * [type] 2 indicates that the [unicode] on the left is a traditional form of the [altunicode] simplified form on the right.
     * example: 6f22 | 2 | 6c49
     * <p>
     * [type] 3 indicates that the unicode is an equivalent form to the [altunicode] on the right.
     * example: 8aac | 3 | 8aaa
     *
     * @param line    the line to parse
     * @param lineNum the line number
     * @return true if parsing successful, false otherwise
     */
    protected boolean parseLine(int lineNum, String line) {

        boolean parseSuccessful = false;
        Matcher lineMatcher = linePattern.matcher(line);
        if (lineMatcher.matches()) {

            String unicodeString = lineMatcher.group(1); // unicode code point occupies the first group
            String typeString = lineMatcher.group(2);     // type occupies the second group

            // Since the strings matched the pattern, we don't have to worry about NumberFormatExceptions.
            Character unicode = new Character((char) Integer.parseInt(unicodeString, 16));    // parses the 4 character code point string to a Character
            int type = Integer.parseInt(typeString);
            CharacterType charType = CharacterType.get(type);
            Character alternateUnicode = null;

            if (charType.isGeneric()) {
                // Don't need to do anything, there is no alternateUnicode for a unified type.
                parseSuccessful = true;
            }
            else if (charType.isSimplified() || charType.isTraditional() || charType.isEquivalent()) {
                // We do the same thing for the three other types:
                // We need to additionally read in the alternate unicode code point that defines the relationship.
                String altUnicodeString = lineMatcher.group(4);
                if (null != altUnicodeString) {
                    alternateUnicode = new Character((char) Integer.parseInt(altUnicodeString, 16));
                    parseSuccessful = true;
                }
            }
            if (parseSuccessful) {
                // If parsing was successful, we can use the parsed recognizer to instantiate a new TypeDescriptor.
                TypeDescriptor typeDescriptor = new TypeDescriptor(charType, unicode, alternateUnicode);
                typeMap.put(unicode, typeDescriptor);
                return true;
            }
        }
        return false;// Line wasn't of the correct form and/or wasn't parsed correctly.
    }
}
