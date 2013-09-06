// Copyright 2009 Jon Vincent
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlecode.vcsupdate;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.SVcsRoot;
import jetbrains.buildServer.vcs.VcsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jon Vincent
 */
public final class TeamCityController extends AbstractController {

    private static final String ID_PARAM = "id";
    private static final String PASS_PARAM = "pass";
    private static final String BUILD_PARAM = "build";

    private final WebControllerManager controllerManager;
    private final AuthorizationInterceptor interceptor;
    private final VcsManager vcsManager;
    private final PluginDescriptor descriptor;

    private String viewName = null;
    private String doneViewName = null;
    private String authPassword = null;

    public TeamCityController(WebControllerManager controllerManager, AuthorizationInterceptor interceptor,
            VcsManager vcsManager, PluginDescriptor descriptor) {
        this.controllerManager = controllerManager;
        this.interceptor = interceptor;
        this.vcsManager = vcsManager;
        this.descriptor = descriptor;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        try {
            return getModelAndView(request, response);
        } catch (Exception e) {
            log("Error while running plugin: " + e);
            throw new RuntimeException(e);
        }

    }

    private ModelAndView getModelAndView(HttpServletRequest request, HttpServletResponse response) {
        if (authPassword != null && authPassword.length() > 0) {
            // Look at password
            String[] passwords = request.getParameterValues(PASS_PARAM);
            if (passwords == null || !authPassword.equals(passwords[0])) {
                log("Invalid password sent: " + passwords[0]);
                return new ModelAndView(viewName);
            }
        }

        // Get all of the VCS roots specified in the request
        Set<SVcsRoot> roots = new LinkedHashSet<SVcsRoot>();

        // Look for any root IDs
        String[] ids = request.getParameterValues(ID_PARAM);
        if (ids != null) {
            for (String id : ids) {
                try {
                    SVcsRoot root = vcsManager.findRootByExternalId(id);
                    if (root != null) roots.add(root);
                } catch (NumberFormatException e) {
                    // just move on to the next ID
                }
            }
        }

        // Look for any build IDs
        String[] buildIDs = request.getParameterValues(BUILD_PARAM);

        // Empty roots so bail
        if (roots.isEmpty()) {
            log("No roots specified!");
            return new ModelAndView(viewName);
        }

        List<String> forcedBuildNames = new ArrayList<String>();
        // Iterate through the roots
        for (SVcsRoot root : roots) {
            // Find the matching configurations
            List<SBuildType> builds = vcsManager.getAllConfigurationUsages(root);
            if (builds == null) continue;

            // We're going to temporarily set the modification to 5 seconds and then trigger an update, and then set it back to what it was
            int interval = (root.isUseDefaultModificationCheckInterval() ? -1 : root.getModificationCheckInterval());
            root.setModificationCheckInterval(5);

            boolean foundBuild = false;
            for (SBuildType build : builds) {
                if (build.isPaused()) {
                    continue;
                }
                if (buildIDs != null) {
                    boolean matched = false;
                    //loop through buildIDs to see if this build matches any
                    for (String buildID : buildIDs) {
                        if (buildID.equals(build.getExternalId())) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        continue;
                    }
                }
                build.forceCheckingForChanges();
                forcedBuildNames.add(build.getFullName());
                log("Forcing check for " + build.getFullName());
                foundBuild = true;
            }

            if (!foundBuild) {
                log("Couldn't find a matching build for " + root.getName());
            }

            if (interval < 0) {
                root.restoreDefaultModificationCheckInterval();
            } else {
                root.setModificationCheckInterval(interval);
            }
        }

        // Redirect to the done page
        return new ModelAndView(doneViewName, "updatedVCSBuilds", forcedBuildNames.toString());
    }

    public void setControllerUri(String controllerUri) {
        controllerManager.registerController(controllerUri, this);
        interceptor.addPathNotRequiringAuth(controllerUri);
    }

    public void setViewName(String viewName) {
        this.viewName = descriptor.getPluginResourcesPath(viewName);
    }

    public void setDoneViewName(String doneViewName) {
        this.doneViewName = descriptor.getPluginResourcesPath(doneViewName);
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    private void log(String message) {
        System.out.println("VCSUPDATEPLUGIN: " + message);
    }

}