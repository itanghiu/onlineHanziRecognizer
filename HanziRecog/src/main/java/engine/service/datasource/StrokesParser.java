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

package engine.service.datasource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import engine.beans.CharacterDescriptor;
import engine.enums.CharacterType;
import engine.util.IOUtils;

/**
 * StrokesParser parses an uncompiled strokes recognizer text file InputStream.
 * Once it's read in, it can be spit out it its raw byte form to an OutputStream.
 * <p>
 * StrokesParser can be used one time to generate the raw bytes equivalent
 * to a new text stroke recognizer source file.  The raw byte equivalent can then
 * be written to disk and the raw bytes can be used to load a HanziLookup
 * based app quickly (i.e. no parsing is required).  See the main method
 * in this class for how to generate the byte equivalent.
 * <p>
 * Or it can be used to load a HanziLookup based up by parsing the text
 * source file each time the app is started.  This is slower since the parsing
 * has to take place during the app load.
 * <p>
 * HanziLookup's various constructors determine which method of reading
 * in the recognizer is used.
 *
 * @see BaseParser
 */
public class StrokesParser extends BaseParser {

    private ByteArrayOutputStream[] genericByteStreams;
    private ByteArrayOutputStream[] simplifiedByteStreams;
    private ByteArrayOutputStream[] traditionalByteStreams;
    private DataOutputStream[] genericOutStreams;
    private DataOutputStream[] simplifiedOutStreams;
    private DataOutputStream[] traditionalOutStreams;

    // We need a CharacterTypeRepository to look up types of characters to write into our byte recognizer array.
    private CharacterTypeRepository typeRepository;

    // Below a couple of reusable arrays.  Allocating them once should save a little.

    // Holds the number of substrokes in the stroke for the given order index.
    // (ie int at index 0 will be the number of substrokes in the first stroke)
    private int[] subStrokesPerStroke = new int[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];

    // Instantiate a flat array that we can resuse to hold parsed SubStroke recognizer.
    // This is so we don't reinstantiate a new array for each line.
    // Holds the direction and length of each SubStroke, so it needs twice as many indices as the possible number of SubStrokes.
    private double[] subStrokeDirections = new double[CharacterDescriptor.MAX_CHARACTER_SUB_STROKE_COUNT];
    private double[] subStrokeLengths = new double[CharacterDescriptor.MAX_CHARACTER_SUB_STROKE_COUNT];

    // Store patterns as instance variables so that we can reuse them and don't need to reinstantiate them for every entry.
    // linePattern identifies the unicode code point and allows us to group it apart from the SubStroke recognizer.
    private Pattern linePattern = Pattern.compile("^([a-fA-F0-9]{4})\\s*\\|(.*)$");
    // subStrokePattern groups the direction and length of a SubStroke.
    private Pattern subStrokePattern = Pattern.compile("^\\s*\\((\\d+(\\.\\d{1,10})?)\\s*,\\s*(\\d+(\\.\\d{1,10})?)\\)\\s*$");


    /**
     * Build a new parser.
     *
     * @param strokesIn      strokes recognizer
     * @param typeRepository the CharacterTypeRepository to get type recognizer from
     * @throws IOException
     */
    public StrokesParser(InputStream strokesIn, CharacterTypeRepository typeRepository) throws IOException {

        this.typeRepository = typeRepository;
        initStrokes(strokesIn);
    }

    /**
     * Build a new parser, parsing a new CharacterTypeRepository from the given types InputStream
     *
     * @param strokesIn
     * @param typesIn
     * @throws IOException
     */
    public StrokesParser(InputStream strokesIn, InputStream typesIn) throws IOException {

        CharacterTypeParser typeParser = new CharacterTypeParser(typesIn);
        typeRepository = typeParser.buildCharacterTypeRepository();
        initStrokes(strokesIn);
    }

    private void initStrokes(InputStream strokesIn) throws IOException {

        try {
            prepareStrokeBytes();
            parse(strokesIn);
            strokesIn.close();
        }
        catch (IOException ioe) {
            IOException thrownIOE = new IOException("Error reading character stroke recognizer!");
            thrownIOE.initCause(ioe);
            throw thrownIOE;
        }
    }

    /**
     * Write the byte recognizer in this StrokesRepository out to the given output stream.
     * Nothing should have already have been written to the stream, and it will
     * be closed once this method returns.  The recognizer can subsequently be read
     * in using the InputStream constructor.
     */
    public void writeCompiledOutput(OutputStream out) throws IOException {

        byte[][] genericBytes = new byte[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT][];
        byte[][] simplifiedBytes = new byte[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT][];
        byte[][] traditionalBytes = new byte[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT][];

        for (int i = 0; i < CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT; i++) {
            genericBytes[i] = genericByteStreams[i].toByteArray();
            simplifiedBytes[i] = simplifiedByteStreams[i].toByteArray();
            traditionalBytes[i] = traditionalByteStreams[i].toByteArray();
        }

        DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));

        // write out each of the recognizer series one after the other.
        writeStrokes(genericBytes, dataOut);
        writeStrokes(simplifiedBytes, dataOut);
        writeStrokes(traditionalBytes, dataOut);
        dataOut.close();
    }

    private void writeStrokes(byte[][] bytesForSeries, DataOutputStream dataOut) throws IOException {

        for (int strokeCount = 0; strokeCount < CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT; strokeCount++) {
            // first write the number of bytes for this stroke count.
            // this is so when reading in we know how many bytes belong to each series.
            int bytesForStrokeCount = bytesForSeries[strokeCount].length;
            dataOut.writeInt(bytesForStrokeCount);
            // now actually write out the recognizer
            dataOut.write(bytesForSeries[strokeCount]);
        }
    }

    private void prepareStrokeBytes() {

        genericByteStreams = new ByteArrayOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];
        genericOutStreams = new DataOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];
        simplifiedByteStreams = new ByteArrayOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];
        simplifiedOutStreams = new DataOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];
        traditionalByteStreams = new ByteArrayOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];
        traditionalOutStreams = new DataOutputStream[CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT];

        for (int i = 0; i < CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT; i++) {
            genericByteStreams[i] = new ByteArrayOutputStream();
            genericOutStreams[i] = new DataOutputStream(genericByteStreams[i]);
            simplifiedByteStreams[i] = new ByteArrayOutputStream();
            simplifiedOutStreams[i] = new DataOutputStream(simplifiedByteStreams[i]);
            traditionalByteStreams[i] = new ByteArrayOutputStream();
            traditionalOutStreams[i] = new DataOutputStream(traditionalByteStreams[i]);
        }
    }

    /**
     * Parses a line of text.  Each line should contain the SubStroke recognizer for a character.
     * <p>
     * The format of a line should be as follows:
     * <p>
     * Each line is the recognizer for a single character represented by the unicode code point.
     * Strokes follow, separated by "|" characters.
     * Strokes can be divided into SubStrokes, SubStrokes are defined by (direction, length).
     * SubStrokes separated by "#" characters.
     * Direction is in radians, 0 to the right, PI/2 up, etc... length is from 0-1.
     */
    protected boolean parseLine(int lineNum, String line) {

        Matcher lineMatcher = linePattern.matcher(line);
        boolean parsedOk = true;
        int subStrokeIndex = 0;    // Need to count the total number of SubStrokes so we can write that out.
        if (lineMatcher.matches()) {
            // Separate out the unicode code point in the first group from the substroke recognizer in the second group.
            String unicodeString = lineMatcher.group(1);
            Character character = new Character((char) Integer.parseInt(unicodeString, 16));
            String lineRemainder = lineMatcher.group(2);

            // Strokes are separated by "|" characters, separate them.
            int strokeCount = 0;
            for (StringTokenizer strokeTokenizer = new StringTokenizer(lineRemainder, "|"); strokeTokenizer.hasMoreTokens(); strokeCount++) {
                if (strokeCount >= CharacterDescriptor.MAX_CHARACTER_STROKE_COUNT) {
                    // Exceeded maximum number of allowable strokes, would result in IndexOutOfBoundsException.
                    parsedOk = false;
                    break;
                }

                // Parse each stroke separately, keep track of SubStroke total.
                // We need to pass the SubStroke index so that the helper parse methods know where
                // they should write the SubStroke recognizer in the SubStrokes recognizer array.
                String nextStroke = strokeTokenizer.nextToken();
                int subStrokes = parseStroke(nextStroke, strokeCount, subStrokeIndex);
                if (subStrokes > 0)
                    subStrokeIndex += subStrokes;
                else
                    // Every stroke should have at least one SubStroke, if not the line is incorrectly formatted or something.
                    parsedOk = false;
            }

            if (parsedOk) {
                // Get the type of the character from the CharacterTypeRepository.
                // Type is used to filter when only traditional or only simplified characters are wanted.
                CharacterType type = typeRepository.getType(character);
                if (type.isNotFound()) {
                    // If type == -1, then the type wasn't found for this character in the type repository.
                    // We just set it so that the character can be found by either a simplified or traditional search.
                    // TODO Will want to add all characters to the type file, or find a better already existing source for this recognizer.
                    type = CharacterType.GENERIC_TYPE;
                }

                DataOutputStream dataOut;
                if (type.isTraditional())
                    dataOut = traditionalOutStreams[strokeCount - 1];
                else if (type.isSimplified())
                    dataOut = simplifiedOutStreams[strokeCount - 1];
                else
                    dataOut = genericOutStreams[strokeCount - 1];

                // Write the parsed recognizer out to the byte array, return true if the writing was successful.
                writeStrokeData(dataOut, character, type, strokeCount, subStrokeIndex);
                return true;
            }
        }
        // Line didn't match the expected format.
        return false;
    }

    /**
     * Parse a Stroke.
     * A Stroke should be composed of one or more SubStrokes separated by "#" characters.
     *
     * @param strokeText         the text of the Stroke
     * @param strokeIndex        the index of the current stroke (first stroke is stroke 0)
     * @param baseSubStrokeIndex the index of the first substroke of the substrokes in this stroke
     * @return the number of substrokes int this stroke, -1 to signal a parse problem
     */
    private int parseStroke(String strokeText, int strokeIndex, int baseSubStrokeIndex) {

        int subStrokeCount = 0;
        for (StringTokenizer subStrokeTokenizer = new StringTokenizer(strokeText, "#"); subStrokeTokenizer.hasMoreTokens(); subStrokeCount++) {
            // We add subStrokeCount * 2 because there are two entries for each SubStroke (direction, length)
            if (subStrokeCount >= CharacterDescriptor.MAX_CHARACTER_SUB_STROKE_COUNT ||
                    !parseSubStroke(subStrokeTokenizer.nextToken(), baseSubStrokeIndex + subStrokeCount)) {
                // If there isn't room in the array (too many substrokes), or not parsed successfully...
                // then we return -1 to signal error.
                return -1;
            }
        }

        // store the number of substrokes in this stroke
        subStrokesPerStroke[strokeIndex] = subStrokeCount;
        // SubStroke parsing was apprently successful, return the number of SubStrokes parsed.
        // The number parsed should just be the number of
        return subStrokeCount;
    }

    /**
     * Parses a SubStroke.  Gets the direction and length, and writes them into the SubStroke recognizer array.
     *
     * @param subStrokeText       the text of the SubStroke
     * @param subStrokeIndex the index to write recognizer into the reusable instance substroke recognizer array.
     * @return true if parsing successful, false otherwise
     */
    private boolean parseSubStroke(String subStrokeText, int subStrokeIndex) {

        // the pattern of a substroke (direction in radians, length 0-1)
        Matcher subStrokeMatcher = subStrokePattern.matcher(subStrokeText);
        if (subStrokeMatcher.matches()) {
            double direction = Double.parseDouble(subStrokeMatcher.group(1));
            double length = Double.parseDouble(subStrokeMatcher.group(3));
            subStrokeDirections[subStrokeIndex] = direction;
            subStrokeLengths[subStrokeIndex] = length;
            return true;
        }
        return false;
    }

    /**
     * Writes the entry into the strokes byte array.
     * Entries are written one after another.  There are no delimiting tokens.
     * The format of an entry in the byte array is as follows:
     * <p>
     * 2 bytes for the character
     * 1 byte for the type (generic, traditional, simplified)
     * <p>
     * 1 byte for the number of Strokes
     * 1 byte for the number of SubStrokes
     * Because of the above, maximum number of Strokes/SubStrokes is 2^7 - 1 = 127.
     * This should definitely be enough for Strokes, probably enough for SubStrokes.
     * In any case, this limitation is less than the limitation imposed by the defined constants currently.
     * <p>
     * Then for each Stroke:
     * 1 byte for the number of SubStrokes in the Stroke
     * <p>
     * Then for each SubStroke:
     * 2 bytes for direction
     * 2 bytes for length
     * <p>
     * Could probably get by with 1 byte for number of Strokes and SubStrokes if needed.
     * Any change to this method will need to be matched by changes to StrokesRepository#compareToNextInStream.
     *
     * @param character      the Character that this entry is for
     * @param type           the type of the Character (generic, traditiona, simplified, should be one of the constants)
     * @param strokeCount    the number of Strokes in this Character entry
     * @param subStrokeCount the number of SubStrokes in this Character entry.
     */
    private void writeStrokeData(DataOutputStream dataOut, Character character, CharacterType type, int strokeCount, int subStrokeCount) {

        try {
            // Write out the non-SubStroke recognizer.
            IOUtils.writeCharacter(character.charValue(), dataOut);
            IOUtils.writeCharacterType(type, dataOut);
            IOUtils.writeStrokeCount(strokeCount, dataOut);

            int subStrokeArrayIndex = 0;
            for (int strokes = 0; strokes < strokeCount; strokes++) {
                int numSubStrokeForStroke = subStrokesPerStroke[strokes];

                //  Write out the number of SubStrokes in this Stroke.
                IOUtils.writeSubStrokeCount(numSubStrokeForStroke, dataOut);

                for (int substrokes = 0; substrokes < numSubStrokeForStroke; substrokes++) {
                    IOUtils.writeDirection(subStrokeDirections[subStrokeArrayIndex], dataOut);
                    IOUtils.writeLength(subStrokeLengths[subStrokeArrayIndex], dataOut);
                    subStrokeArrayIndex++;
                }
            }

        }
        catch (IOException ioe) {
            // writing to a ByteArrayOutputStream, shouldn't be any chance for an IOException
            ioe.printStackTrace();
        }
    }

    static public byte[] getStrokeBytes(InputStream strokesIn, InputStream typesIn) throws IOException {

        StrokesParser strokesParser = new StrokesParser(strokesIn, typesIn);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        strokesParser.writeCompiledOutput(bytes);
        return bytes.toByteArray();
    }

    /**
     * Use this to output a compiled version of strokes recognizer.
     * We can use a pre-compiled file to load much quicker than if we
     * had to parse the recognizer on load.
     */
    static public void main(String[] args) {

        if (args.length != 3) {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append("Takes three arguments:\n");
            sbuf.append("1: the plain-text strokes recognizer file\n");
            sbuf.append("2: the plain-text types recognizer file\n");
            sbuf.append("3: the file to output the compiled recognizer file to");
            System.err.println(sbuf);
        }
        else {
            try {
                FileInputStream strokesIn = new FileInputStream(args[0]);
                FileInputStream typesIn = new FileInputStream(args[1]);
                FileOutputStream compiledOut = new FileOutputStream(args[2]);

                CharacterTypeParser typeParser = new CharacterTypeParser(typesIn);
                CharacterTypeRepository typeRepository = typeParser.buildCharacterTypeRepository();

                StrokesParser strokesParser = new StrokesParser(strokesIn, typeRepository);
                strokesParser.writeCompiledOutput(compiledOut);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
