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

package me.snowdrop.servicecatalog;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.fabric8.maven.enricher.api.BaseEnricher;
import io.fabric8.maven.core.util.Configs;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.maven.enricher.api.MavenEnricherContext;
import io.fabric8.maven.core.util.MavenUtil;
import me.snowdrop.servicecatalog.api.model.ServiceBindingBuilder;
import me.snowdrop.servicecatalog.api.model.ServiceInstanceBuilder;
import me.snowdrop.servicecatalog.visitors.AddEnvVarsFromSecret;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ServiceCatalogEnricher extends BaseEnricher {

    private static final String YES = "yes";
    private static final String NO = "no";
        
    private enum Config implements Configs.Key {
            name,
            broker,
            serviceClass,
            servicePlan,
            parameters,
            bind,
            secret,
            tags;

            public String def() {
                return d;
            }

            protected String d;
    }

    public ServiceCatalogEnricher(MavenEnricherContext context) {
        super(context, "servicecatalog");
    }

    @Override
    public void addMissingResources(KubernetesListBuilder builder) {
        log.info("Checking Service Catalog configuration");
        String name = getConfig(Config.name, MavenUtil.createDefaultResourceName(getContext().getArtifact().getArtifactId()));

        String serviceClass = getConfig(Config.serviceClass);
        String servicePlan = getConfig(Config.servicePlan);

        Map<String, Object> parameters = getMapValue(Config.parameters.name()).orNull();

        if (!Utils.isNullOrEmpty(serviceClass) && !Utils.isNullOrEmpty(servicePlan)) {
            log.info("Creating service instance.");
            builder.addToItems(new ServiceInstanceBuilder()
                               .withNewMetadata()
                               .withName(name)
                               .endMetadata()
                               .withNewSpec()
                               .withClusterServicePlanExternalName(servicePlan)
                               .withClusterServiceClassExternalName(serviceClass)
                               .withParameters(parameters)
                               .endSpec()
                               .build());

            handleBinding(builder);
       } 
    }

    public void handleBinding(KubernetesListBuilder builder) {
        String name = getConfig(Config.name, MavenUtil.createDefaultResourceName(getContext().getArtifact().getArtifactId()));

        boolean bindingRequested = YES.equalsIgnoreCase(getConfig(Config.bind, NO));
        String secret = getConfig(Config.secret, MavenUtil.createDefaultResourceName(name));

            if (bindingRequested) {
                log.info("Creating service binding.");
                builder.addToItems(new ServiceBindingBuilder()
                                   .withNewMetadata()
                                   .withName(name)
                                   .endMetadata()
                                   .withNewSpec()
                                   .withSecretName(secret)
                                   .withNewInstanceRef(name)
                                   .endSpec()
                                   .build());

                if (!Strings.isNullOrEmpty(secret)) {
                    builder.accept(new AddEnvVarsFromSecret(secret));
                }
            }
    }


    //
    // The code below has been copied from:https://github.com/fabric8io/fabric8-maven-plugin/blob/master/enricher/fabric8/src/main/java/io/fabric8/maven/enricher/fabric8/VertxHealthCheckEnricher.java#L339
    //
    private Optional<Map<String, Object>> getMapValue(String attribute) {
        String[] path = new String[]{
                attribute
        };

        Optional<Object> element = getElement(path);
        if (!element.isPresent()) {
            element = getElement(attribute);
        }

        return element.transform(input -> {
            if (input instanceof Map) {
                return (Map<String, Object>) input;
            } else {
                throw new IllegalArgumentException();
            }
        });
    }

    private Optional<Object> getElement(String... path) {
        final Map<String, Object> configuration = getContext().getConfiguration("io.fabric8:fabric8-maven-plugin");
        if (configuration == null || configuration.isEmpty()) {
            return Optional.absent();
        }


        String[] roots = new String[]{"enricher", "config", "servicecatalog"};
        List<String> absolute = new ArrayList<>();
        absolute.addAll(Arrays.asList(roots));
        absolute.addAll(Arrays.asList(path));
        Object root = configuration;
        for (String key : absolute) {

            if (root instanceof Map) {
                Map<String, Object> rootMap = (Map<String, Object>) root;
                root = rootMap.get(key);
                if (root == null) {
                    return Optional.absent();
                }
            }

        }
        return Optional.of(root);
    }

}

