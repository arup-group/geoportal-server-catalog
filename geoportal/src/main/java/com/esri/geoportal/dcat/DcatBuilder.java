/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.dcat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCAT builder
 */
public class DcatBuilder {
  /**
   * Logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DcatBuilder.class);
  
  /** The script engines. */
  private static final Map<String,ScriptEngine> ENGINES = Collections.synchronizedMap(new HashMap<String,ScriptEngine>());
  private static final ObjectMapper MAPPER = new ObjectMapper();
  
  static {
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
  
  /** Instance variables. */
  private String javascriptFile = "gs/context/nashorn/execute.js";
  private final DcatCache dcatCache;
  
  public DcatBuilder(DcatCache dcatCache) {
    this.dcatCache = dcatCache;
  }
  
  public void execute() {
    DcatCacheOutputStream outputStream = null;
    
    try {
      DcatRequest request = new DcatRequest(getSelfInfo(), getCachedEngine(javascriptFile)) {
        private boolean open;
        
        @Override
        public void onRec(String rec) {
          if (!open) {
            // TODO open stream and write header
            
            open = true;
          }
          
        }

        @Override
        public void onEnd() {
          // TODO write final characters
          
          if (outputStream!=null) {
            try {
              outputStream.close();
            } catch (IOException ex) {
              LOGGER.warn(String.format("Error closing DCAT stream."), ex);
            }
          }
        }
      };
      
      request.execute();
    } catch(Exception ex) {
      LOGGER.error(String.format("Error generating DCAT."), ex);
      if (outputStream!=null) {
        outputStream.abort();
      }
    }
  }
  
  private ScriptEngine getCachedEngine(String javascriptFile) 
      throws URISyntaxException, IOException, ScriptException {
    ScriptEngine engine = null;
    synchronized(ENGINES) {
      engine = ENGINES.get(javascriptFile);
      if (engine == null) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(javascriptFile);
        URI uri = url.toURI();
        String script = new String(Files.readAllBytes(Paths.get(uri)),"UTF-8");
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");
        engine.eval(script);
        ENGINES.put(javascriptFile,engine);
      }
    }
    return engine;
  }
  
  private JsonObjectBuilder getSelfInfo() {
    JsonObjectBuilder info = Json.createObjectBuilder();
    JsonObjectBuilder elastic = Json.createObjectBuilder();
    String node = null;
    String scheme = "http://";
    int port = 9200;
    try {
      node = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getNextNode();
      port = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getHttpPort();
      if (com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getUseHttps()) {
        scheme = "https://";
      }
      String username = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getXpackUsername();
      String password = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getXpackPassword();
      if (username != null && username.length() > 0 && password != null && password.length() > 0) {
        elastic.add("username",username);
        elastic.add("password",password);
      }
    } catch (Throwable t) {
      LOGGER.warn(String.format("Warning getting self info."), t);
    }
    try {
      JsonObjectBuilder access = Json.createObjectBuilder();
      access.add("supportsApprovalStatus",com.esri.geoportal.context.GeoportalContext.getInstance().getSupportsApprovalStatus());
      access.add("supportsGroupBasedAccess",com.esri.geoportal.context.GeoportalContext.getInstance().getSupportsGroupBasedAccess());    
      com.esri.geoportal.context.AppUser user = null;
      if (user != null && user.getUsername() != null) {
        access.add("username",user.getUsername());
        access.add("isAdmin",user.isAdmin());
        if (com.esri.geoportal.context.GeoportalContext.getInstance().getSupportsGroupBasedAccess()) {
          JsonArrayBuilder jsaGroups = Json.createArrayBuilder();
          List<com.esri.geoportal.base.security.Group> groups = user.getGroups();
          if (groups != null) {
            for (com.esri.geoportal.base.security.Group group: groups) {
              jsaGroups.add(group.id);
            }         
          }
          access.add("groups",jsaGroups);
        }
      }
      elastic.add("access",access);
    } catch (Throwable t) {
      LOGGER.warn(String.format("Warning getting self info."), t);
    }
    if ((node != null) && (node.length() > 0)) {
      String idxName = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getIndexName();
      String itmType = com.esri.geoportal.context.GeoportalContext.getInstance().getElasticContext().getItemIndexType();       
      String url = scheme+node+":"+port+"/"+idxName+"/"+itmType+"/_search";
      elastic.add("searchUrl",url);
      info.add("elastic",elastic);
      return info;
    }
    return null;
  }
  
}
