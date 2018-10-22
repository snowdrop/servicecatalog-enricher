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

import io.fabric8.maven.enricher.api.BaseEnricher;
import io.fabric8.maven.core.util.Configs;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.maven.enricher.api.MavenEnricherContext;
import me.snowdrop.servicecatalog.api.model.ServiceInstanceBuilder;
import io.fabric8.maven.core.util.MavenUtil;
import me.snowdrop.servicecatalog.api.model.ServiceBindingBuilder;


public class ServiceCatalogEnricher extends BaseEnricher {

    private static final String YES = "yes";
    private static final String NO = "no";
        
    private enum Config implements Configs.Key {
            name,
            broker,
            serviceClass,
            servicePlan,
            bind,
            secret,
            tags;

            public String def() {
                return d;
            }

            protected String d;
    }
    
    public ServiceCatalogEnricher(MavenEnricherContext context) {
        super(context, "servicecatalog-enricher");
    }

    @Override
    public void addMissingResources(KubernetesListBuilder builder) {
        log.info("Checking Service Catalog configuration");
        String serviceClass = getConfig(Config.serviceClass);
        String servicePlan = getConfig(Config.servicePlan);

        String name = getConfig(Config.name, MavenUtil.createDefaultResourceName(getContext().getRootArtifactId()));

        boolean bindingRequested = YES.equalsIgnoreCase(getConfig(Config.bind, NO));
        String secret = getConfig(Config.secret, MavenUtil.createDefaultResourceName(getName()));

        if (!Utils.isNullOrEmpty(serviceClass) && !Utils.isNullOrEmpty(servicePlan)) {
            log.info("Creating service instance.");
            builder.addToItems(new ServiceInstanceBuilder()
                               .withNewMetadata()
                               .withName(name)
                               .endMetadata()
                               .withNewSpec()
                               .withClusterServicePlanExternalName(servicePlan)
                               .withClusterServiceClassExternalName(serviceClass)
                               .endSpec()
                               .build());

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
            }
        } 
    }
}

