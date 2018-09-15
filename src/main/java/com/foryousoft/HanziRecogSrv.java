package com.foryousoft;

import hanzirecog.engine.beans.CharacterDescriptor;
import hanzirecog.engine.beans.WrittenCharacter;
import hanzirecog.engine.beans.WrittenPoint;
import hanzirecog.engine.beans.WrittenStroke;
import hanzirecog.engine.service.datasource.StrokesDataSource;
import hanzirecog.engine.enums.CharacterType;
import hanzirecog.engine.MatcherThread;
import hanzirecog.engine.StrokesMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * Copyright (C) 2018 I-Tang HIU
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
@Service
public class HanziRecogSrv {

  private double looseness = 0.25;        // the "looseness" of lookup, 0-1, higher == looser, looser more computationally intensive
  private int numResults = 15;
  MatcherThread matcherThread;
  private CharacterType searchType = CharacterType.GENERIC_TYPE;
  private StrokesDataSource strokesDataSource;

  public HanziRecogSrv() {
    matcherThread = new MatcherThread();
  }

  public Character[] recognizeHanzi(List<SignatureStroke> strokes) {

    WrittenCharacter writtenCharacter = convert(strokes);
    if (writtenCharacter.getStrokes().isEmpty())
      return new Character[0];

    CharacterDescriptor inputDescriptor = writtenCharacter.buildCharacterDescriptor();
    boolean searchTraditional = searchType.isGeneric() || searchType.isTraditional();
    boolean searchSimplified = searchType.isGeneric() || searchType.isSimplified();
    StrokesMatcher matcher = new StrokesMatcher(inputDescriptor,
            searchTraditional, searchSimplified, looseness, numResults);
    Character[] results = matcher.doMatching();
    return results;
  }

  private WrittenCharacter convert(List<SignatureStroke> signatureStrokes) {

    WrittenCharacter writtenCharacter = new WrittenCharacter();
    signatureStrokes.forEach((signatureStroke) -> {
      List<Integer> xs = signatureStroke.getX();
      List<Integer> ys = signatureStroke.getY();
      List<WrittenPoint> writtenPoints = new ArrayList();
      for (int i = 0; i < xs.size(); i++) {
        WrittenPoint writtenPoint = new WrittenPoint(xs.get(i), ys.get(i));
        writtenPoints.add(writtenPoint);
      }
      WrittenStroke writtenStroke = new WrittenStroke(writtenPoints);
      writtenCharacter.addStroke(writtenStroke);
    });
    writtenCharacter.analyzeAndMark();
    writtenCharacter.computeBoundingBox();
    return writtenCharacter;
  }

}
