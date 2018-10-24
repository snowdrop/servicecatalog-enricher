/**
 * Copyright (C) 2018 Red Hat inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 **/
package me.snowdrop.servicecatalog.sandbox;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.builder.TypedVisitor;


public class CreateVolume extends TypedVisitor<PodSpecBuilder> {

    private final String secret;

    public CreateVolume (String secret) {
        this.secret=secret;
    }

    public void visit(PodSpecBuilder builder) {
        builder.addNewVolume()
            .withName(secret)
            .withNewSecret()
            .withSecretName(secret)
            .endSecret()
            .endVolume();
    }
}
