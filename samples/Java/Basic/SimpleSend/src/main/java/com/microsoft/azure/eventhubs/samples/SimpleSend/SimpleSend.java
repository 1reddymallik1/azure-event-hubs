/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.samples.SimpleSend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class SimpleSend {

    public static void main(String[] args)
            throws EventHubException, ExecutionException, InterruptedException, IOException {

        final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName("Your Event Hubs namaspace name") // to target National clouds - use .setEndpoint(URI)
                .setEventHubName("Your event hub")
                .setSasKeyName("Your policy name")
                .setSasKey("Your primary SAS key");

        final Gson gson = new GsonBuilder().create();

        // The Executor handles all the asynchronous tasks and this is passed to the EventHubClient.
        // The gives the user control to segregate their thread pool based on the work load.
        // This pool can then be shared across multiple EventHubClient instances.
        // The below sample uses a single thread executor as there is only on EventHubClient instance,
        // handling different flavors of ingestion to Event Hubs here
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Each EventHubClient instance spins up a new TCP/SSL connection, which is expensive.
        // It is always a best practice to reuse these instances. The following sample shows the same.
        final EventHubClient ehClient = EventHubClient.createSync(connStr.toString(), executorService);;

        try {
            for (int i = 0; i < 100; i++) {

                String payload = "Message " + Integer.toString(i);
                //PayloadEvent payload = new PayloadEvent(i);
                byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
                EventData sendEvent = EventData.create(payloadBytes);

                // Send - not tied to any partition
                // EventHubs service will round-robin the events across all EventHubs partitions.
                // This is the recommended & most reliable way to send to EventHubs.
                ehClient.sendSync(sendEvent);
            }

            System.out.println(Instant.now() + ": Send Complete...");
            System.in.read();
        } finally {
            ehClient.closeSync();
            executorService.shutdown();
        }
    }
}
