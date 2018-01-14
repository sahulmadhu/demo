/*
 * Copyright (C) 2010 JFrog Ltd.
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

package org.jfrog.hudson;

import org.apache.maven.artifact.Artifact;

import java.io.Serializable;

/**
 * Captures information of a maven dependency.
 *
 * @author Yossi Shaul
 */
public class MavenDependency implements Serializable {
    private String id;
    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String scope;
    private String fileName;

    public MavenDependency(Artifact artifact) {
        id = artifact.getId() != null ? artifact.getId().intern() : null;
        groupId = artifact.getGroupId() != null ? artifact.getGroupId().intern() : null;
        artifactId = artifact.getArtifactId() != null ? artifact.getArtifactId().intern() : null;
        version = artifact.getVersion() != null ? artifact.getVersion().intern() : null;
        scope = artifact.getScope() != null ? artifact.getScope().intern() : null;
        fileName = artifact.getFile() != null ? artifact.getFile().getName().intern() : null;
        type = artifact.getType() != null ? artifact.getType().intern() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MavenDependency that = (MavenDependency) o;

        if (!id.equals(that.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public String getFileName() {
        return fileName;
    }
}
