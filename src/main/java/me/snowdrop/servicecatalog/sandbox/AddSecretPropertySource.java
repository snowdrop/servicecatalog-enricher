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

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;


public class AddSecretPropertySource extends TypedVisitor<ContainerBuilder> {

    private static final String JAVA_OPTS = "JAVA_OPTS";
    private static final String SPRING_CONFIG_LOCATION = "-Dspring.config.location=file:";
    private static final EnvVar EMPTY_JAVA_OPTS = new EnvVarBuilder().withName(JAVA_OPTS).withValue("").build();

    private final String secret;
    private final String path;

    public AddSecretPropertySource(String secret, String path) {
        this.secret = secret; 
        this.path = path;
    }

	@Override
	public void visit(ContainerBuilder builder) {
        //Create the JAVA_OPTS EnvVar if missing
        if (!hasEnvVar(builder, JAVA_OPTS)) {
            builder.addToEnv(EMPTY_JAVA_OPTS);
        }
        //Visit and set the right value.
        builder.accept(new AppendToEnvVar(JAVA_OPTS, SPRING_CONFIG_LOCATION + path));
	}

    public boolean hasEnvVar(ContainerBuilder builder, String name) {
     return builder.buildEnv().stream()
            .filter(e -> name.equals(e.getName()))
            .count() > 0;
    }
}
