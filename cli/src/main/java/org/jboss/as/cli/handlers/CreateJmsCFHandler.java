/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.cli.handlers;

import java.util.List;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.ParsedArguments;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.DefaultCompleter;
import org.jboss.as.cli.impl.DefaultCompleter.CandidatesProvider;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Alexey Loubyansky
 */
public class CreateJmsCFHandler extends BatchModeCommandHandler {

    private final ArgumentWithValue name;
    private final ArgumentWithValue autoGroup;
    private final ArgumentWithValue entries;
    private final ArgumentWithValue profile;
/*    private final ArgumentWithValue connector;
    private final ArgumentWithValue blockOnAcknowledge;
    private final ArgumentWithValue blockOnDurableSend;
    private final ArgumentWithValue blockOnNonDurableSend;
    private final ArgumentWithValue cacheLargeMessageClient;
    private final ArgumentWithValue callTimeout;
    private final ArgumentWithValue clientFailureCheckPeriod;
*/
//    private final ArgumentWithValue clientId;
/*    private final ArgumentWithValue confirmationWindowSize;
    private final ArgumentWithValue connectionTtl;
    private final ArgumentWithValue consumer;
    private final ArgumentWithValue consumerMaxRate;
    private final ArgumentWithValue consumerWindowSize;
    private final ArgumentWithValue discoveryGroupName;
    private final ArgumentWithValue dupsOkBatchSize;
    private final ArgumentWithValue failoverOnInitialConnection;
    private final ArgumentWithValue failoverOnServerShutdown;
    private final ArgumentWithValue groupId;
    private final ArgumentWithValue maxRetryInterval;
    private final ArgumentWithValue minLargeMessageSize;
    private final ArgumentWithValue oreAcknowledge;
    private final ArgumentWithValue producerMaxRate;
    private final ArgumentWithValue producerWindowSize;
    private final ArgumentWithValue reconnectAttempts;
    private final ArgumentWithValue retryInterval;
    private final ArgumentWithValue retryIntervalMultiplier;
    private final ArgumentWithValue scheduledThreadPoolMaxSize;
    private final ArgumentWithValue threadPoolMaxSize;
    private final ArgumentWithValue transactionBatchSize;
    private final ArgumentWithValue useGlobalPools;
*/
    public CreateJmsCFHandler() {
        super("create-jms-cf", true);

        SimpleArgumentTabCompleter argsCompleter = (SimpleArgumentTabCompleter) this.getArgumentCompleter();

        profile = new ArgumentWithValue(new DefaultCompleter(new CandidatesProvider(){
            @Override
            public List<String> getAllCandidates(CommandContext ctx) {
                return Util.getNodeNames(ctx.getModelControllerClient(), null, "profile");
            }}), "--profile") {
            @Override
            public boolean canAppearNext(CommandContext ctx) {
                if(!ctx.isDomainMode()) {
                    return false;
                }
                return super.canAppearNext(ctx);
            }
        };
        argsCompleter.addArgument(profile);

        name = new ArgumentWithValue(true, /*0, */"--name") {
            @Override
            public boolean canAppearNext(CommandContext ctx) {
                if(ctx.isDomainMode() && !profile.isPresent(ctx.getParsedArguments())) {
                    return false;
                }
                return super.canAppearNext(ctx);
            }
        };
        argsCompleter.addArgument(name);

        autoGroup = new ArgumentWithValue("--auto-group");
        argsCompleter.addArgument(autoGroup);

        entries = new ArgumentWithValue("--entries");
        argsCompleter.addArgument(entries);

/*        clientId = new ArgumentWithValue("--client-id");
        addArgument(clientId);
*/    }

    /* (non-Javadoc)
     * @see org.jboss.as.cli.handlers.CommandHandlerWithHelp#doHandle(org.jboss.as.cli.CommandContext)
     */
    @Override
    protected void doHandle(CommandContext ctx) {

        ModelNode request;
        try {
            request = buildRequest(ctx);
        } catch (OperationFormatException e) {
            ctx.printLine(e.getLocalizedMessage());
            return;
        }

        ModelControllerClient client = ctx.getModelControllerClient();
        final ModelNode result;
        try {
            result = client.execute(request);
        } catch (Exception e) {
            ctx.printLine("Failed to perform operation: " + e.getLocalizedMessage());
            return;
        }

        if (!Util.isSuccess(result)) {
            ctx.printLine(Util.getFailureDescription(result));
            return;
        }
        ctx.printLine("Successfully created connection factory.");
    }

    @Override
    public ModelNode buildRequest(CommandContext ctx)
            throws OperationFormatException {

        DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();

        ParsedArguments args = ctx.getParsedArguments();

        if(ctx.isDomainMode()) {
            String profile = this.profile.getValue(args);
            if(profile == null) {
                throw new OperationFormatException("--profile argument value is missing.");
            }
            builder.addNode("profile",profile);
        }

        builder.addNode("subsystem", "jms");
        builder.setOperationName("add");

        final String name;
        try {
            name = this.name.getValue(args);
        } catch(IllegalArgumentException e) {
            throw new OperationFormatException(e.getLocalizedMessage());
        }

        final String entriesStr = this.entries.getValue(args);

        builder.addNode("connection-factory", name);
        ModelNode entriesNode = builder.getModelNode().get("entries");
        if(entriesStr == null) {
            entriesNode.add(name);
        } else {
            String[] split = entriesStr.split(",");
            for(int i = 0; i < split.length; ++i) {
                String entry = split[i].trim();
                if(!entry.isEmpty()) {
                    entriesNode.add(entry);
                }
            }
        }

        return builder.buildRequest();
    }
}
