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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.mathan.sonar.dependencyupdates.maven.SolrSearch;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class ReleaseDateAgent {

  private final WebTarget webTarget;

  ReleaseDateAgent() {
    this.webTarget = ClientBuilder.newClient().register(JacksonObjectMapperProvider.class).register(JacksonJsonProvider.class).target("http://search.maven.org/solrsearch/select");
  }

  public LocalDate getReleaseDate(Dependency dependency, String version) {
    SolrSearch response = webTarget.queryParam("q", String.format("g: %s AND a: %s AND v:%s", dependency.getGroupId(), dependency.getArtifactId(), version)).queryParam("rows","20").queryParam("core","gav").request().build("GET").invoke(SolrSearch.class);
    if(response!=null&&response.getResponse()!=null&&response.getResponse().getDocs()!=null&&response.getResponse().getDocs().size()>0) {
      Date date = new Date(response.getResponse().getDocs().get(0).getTimestamp());
      return date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
    } else {
      return null;
    }
  }

}
