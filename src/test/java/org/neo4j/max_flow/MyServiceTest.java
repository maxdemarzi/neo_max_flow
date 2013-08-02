package org.neo4j.max_flow;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MyServiceTest {

    private GraphDatabaseService db;
    private MyService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
    private static final RelationshipType HATES = DynamicRelationshipType.withName("HATES");

    @Before
    public void setUp() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        dropRootNode(db);
        populateDb(db);
        service = new MyService();
    }

    private void dropRootNode(GraphDatabaseService db){
        Transaction tx = db.beginTx();
        try
        {
            Node root = db.getNodeById(0);
            root.delete();
            tx.success();
        }
        finally
        {
            tx.finish();
        }

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


    @After
    public void tearDown() throws Exception {
        db.shutdown();

    }

    @Test
    public void shouldGetConnectedComponentsCount() throws IOException {
        assertEquals("5", service.getMaxFlow(1L, 7L, db));
    }


    public GraphDatabaseService graphdb() {
        return db;
    }
}
