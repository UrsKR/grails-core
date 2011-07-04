/* Copyright 2004-2005 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.pages;

import grails.util.PluginBuildSettings;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.support.StaticResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * A StaticResourceLoader that loads GSPs from a local grails-app folder instead of from WEB-INF in
 * development mode.
 *
 * @see org.codehaus.groovy.grails.support.StaticResourceLoader
 *
 * @author Graeme Rocher
 * @since 0.5
 */
public class GroovyPageResourceLoader extends StaticResourceLoader {

    /**
     * The id of the instance of this bean to be used in the Spring context
     */
    public static final String BEAN_ID = "groovyPageResourceLoader";

    private static final Log LOG = LogFactory.getLog(GroovyPageResourceLoader.class);
    private static final String PLUGINS_PATH = "/plugins/";

    private Resource localBaseResource;
    private PluginBuildSettings pluginSettings;

    @Override
    public void setBaseResource(Resource baseResource) {
        localBaseResource = baseResource;
        super.setBaseResource(baseResource);
    }

    public void setPluginSettings(PluginBuildSettings settings) {
        pluginSettings = settings;
    }

    @Override
    public Resource getResource(String location) {
        Assert.hasLength(location, "Argument [location] cannot be null or blank");

        // deal with plug-in resolving
        if (location.startsWith(PLUGINS_PATH)) {
            if (pluginSettings == null) throw new RuntimeException("'pluginsettings' has not been initialised.");
            List<String> pluginBaseDirectories = pluginSettings.getPluginBaseDirectories();
            String path = location.substring(PLUGINS_PATH.length(), location.length());

            for (String pluginBaseDirectory : pluginBaseDirectories) {
                String pathToResource = pluginBaseDirectory + File.separatorChar + path;
                Resource r = super.getResource("file:" + pathToResource);
                if (r.exists()) return r;
            }

            Resource[] inlinePluginDirectories = pluginSettings.getInlinePluginDirectories();
            String pathRelativeToPlugin = path.substring(path.indexOf("/"), path.length());
            for (Resource inlinePluginDirectory : inlinePluginDirectories) {
                try {
                    File dirFile = inlinePluginDirectory.getFile();
                    File pageFile = new File(dirFile, pathRelativeToPlugin);
                    if (pageFile.exists()) {
                        return new FileSystemResource(pageFile);
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        Resource resource = super.getResource(location);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved GSP location [" + location + "] to resource [" + resource +
                    "] (exists? [" + resource.exists() + "]) using base resource [" + localBaseResource + "]");
        }
        return resource;
    }
}
