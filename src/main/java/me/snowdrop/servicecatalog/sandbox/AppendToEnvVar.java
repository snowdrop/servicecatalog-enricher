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
import io.fabric8.kubernetes.api.model.EnvVarBuilder;

public class AppendToEnvVar extends TypedVisitor<EnvVarBuilder> {

    private static String key;
    private static String value;

    public AppendToEnvVar (String key, String value) {
        this.key=key;
        this.value=value;
    }

    @Override
    public void visit(EnvVarBuilder builder) {
        if (!key.equals(builder.getName()))  {
            return;
        }

        String original = builder.getValue();
        String newValue = value;
        
        if (original != null && !original.isEmpty()) {
            newValue = original + " " + value;
        }

        builder.withValue(newValue);
    }
}
