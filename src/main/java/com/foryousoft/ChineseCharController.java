package com.foryousoft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

@RestController
public class ChineseCharController {

  private ObjectMapper objectMapper = new ObjectMapper();
  private static Logger logger = Logger.getLogger(ChineseCharController.class.toString());

  @Autowired
  private HanziRecogSrv hanziRecogSrv;

  @RequestMapping(value = "/addCharImage", method = RequestMethod.POST)
  public DeferredResult<ResponseEntity<String>> addChar(@RequestBody String charSignature) throws IOException {

    TypeReference<StrokesDto> mapType = new TypeReference<StrokesDto>() {
    };
    StrokesDto strokeDto = objectMapper.readValue(charSignature, mapType);
    List<SignatureStroke> strokes = strokeDto.getStrokes();
    DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();
    CompletableFuture.supplyAsync(() -> hanziRecogSrv.recognizeHanzi(strokes))
            .whenComplete((charResults, throwable) ->
                    {
                      List<String> results = Arrays.stream(charResults).map((c) -> c + "").collect(Collectors.toList());
                      String candidateChars = String.join(":", results);
                      ResponseEntity r =new ResponseEntity(candidateChars, HttpStatus.OK);
                      deferredResult.setResult(r);
                    }
            );
    return deferredResult;
  }
}
