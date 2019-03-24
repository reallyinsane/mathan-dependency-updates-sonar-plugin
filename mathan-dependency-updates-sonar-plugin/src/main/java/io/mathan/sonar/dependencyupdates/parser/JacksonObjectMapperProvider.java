/*
 * mathan-dependency-updates-sonar-plugin
 * Copyright (c) 2019 Matthias Hanisch
 * matthias@mathan.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mathan.sonar.dependencyupdates.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.ws.rs.ext.ContextResolver;

public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
 
  private final ObjectMapper objectMapper;
 
  public JacksonObjectMapperProvider() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // write timestamp in correct format
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
 
  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}