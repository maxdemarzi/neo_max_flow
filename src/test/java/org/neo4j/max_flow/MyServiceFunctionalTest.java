package org.neo4j.max_flow;

import com.sun.jersey.api.client.Client;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.ServerBuilder;
import org.neo4j.server.rest.JaxRsResponse;
import org.neo4j.server.rest.RestRequest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MyServiceFunctionalTest {

    public static final Client CLIENT = Client.create();
    public static final String MOUNT_POINT = "/ext";
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");

    @Test
    public void shouldReturnConnectedComponentCount() throws IOException {
        NeoServer server = ServerBuilder.server()
                .withThirdPartyJaxRsPackage("org.neo4j.max_flow", MOUNT_POINT)
                .build();
        server.start();
        populateDb(server.getDatabase().getGraph());
        RestRequest restRequest = new RestRequest(server.baseUri().resolve(MOUNT_POINT), CLIENT);
        JaxRsResponse response = restRequest.get("service/max_flow/1/7");
        assertEquals("5", response.getEntity());
        server.stop();

    }

    private void populateDb(GraphDatabaseService db) {
        Transaction tx = db.beginTx();
        try
        {
            Node personA = createPerson(db, "A");
            Node personAA = createPerson(db, "AA");
            Node personAB = createPerson(db, "AB");
            Node personAC = createPerson(db, "AC");
            Node personBA = createPerson(db, "BA");
            Node personBB = createPerson(db, "BB");
            Node personCA = createPerson(db, "CA");

            connectPerson(db, personA, personAA, KNOWS, 1);
            connectPerson(db, personA, personAB, KNOWS, 3);
            connectPerson(db, personA, personAC, KNOWS, 1);
            connectPerson(db, personAA, personBA, KNOWS, 1);
            connectPerson(db, personAB, personBA, KNOWS, 1);
            connectPerson(db, personAB, personBB, KNOWS, 2);
            connectPerson(db, personAC, personBB, KNOWS, 1);
            connectPerson(db, personBA, personCA, KNOWS, 2);
            connectPerson(db, personBB, personCA, KNOWS, 3);

            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private Node createPerson(GraphDatabaseService db, String name) {
        Node node = db.createNode();
        node.setProperty("name", name);
        return node;
    }

    private void connectPerson(GraphDatabaseService db, Node from, Node to, RelationshipType type, Integer weight) {
        Relationship r = from.createRelationshipTo(to, type);
        r.setProperty("weight", weight);
    }

}
