/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.keycloak.subsystem.extension.authserver;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.keycloak.subsystem.extension.KeycloakAdapterConfigService;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;

/**
 * Remove an auth-server from a realm.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public final class AuthServerRemoveHandler extends AbstractRemoveStepHandler {

    public static AuthServerRemoveHandler INSTANCE = new AuthServerRemoveHandler();

    private AuthServerRemoveHandler() {}

    @Override
    protected void performRemove(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        String deploymentName = AuthServerUtil.getDeploymentName(operation);
        KeycloakAdapterConfigService.getInstance().removeServerDeployment(deploymentName);

        if (requiresRuntime(context)) { // don't do this on a domain controller
            addStepToRemoveAuthServer(context, deploymentName);
        }

        super.performRemove(context, operation, model);
    }

    private void addStepToRemoveAuthServer(OperationContext context, String deploymentName) {
        PathAddress deploymentAddress = PathAddress.pathAddress(PathElement.pathElement(DEPLOYMENT, deploymentName));
        ModelNode op = Util.createOperation(REMOVE, deploymentAddress);
        context.addStep(op, getRemoveHandler(context, deploymentAddress), OperationContext.Stage.MODEL);
    }

    private OperationStepHandler getRemoveHandler(OperationContext context, PathAddress address) {
        ImmutableManagementResourceRegistration rootResourceRegistration = context.getRootResourceRegistration();
        return rootResourceRegistration.getOperationHandler(address, REMOVE);
    }
}
