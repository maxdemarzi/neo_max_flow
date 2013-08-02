package org.neo4j.max_flow;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.graphalgo.*;
import org.neo4j.graphalgo.PathFinder;

import static org.neo4j.graphalgo.GraphAlgoFactory.allPaths;
import static org.neo4j.kernel.Traversal.expanderForAllTypes;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/service")
public class MyService {

    private int getFlow(GraphDatabaseService db, Node source_node, Node sinc_node) {
      int maxDepth = 10000;
      int flow = 0;
      int accumulator = 0;

      List<Integer> flows = new ArrayList<Integer>();

      Transaction tx = db.beginTx();
      try {
          for (org.neo4j.graphdb.Path p : allPaths( expanderForAllTypes(Direction.OUTGOING), maxDepth ).findAllPaths(source_node, sinc_node)) {

              for (Relationship r : p.relationships())
              {
                  flows.add((Integer)r.getProperty("weight"));
              }

              flow = Collections.min(flows);
              flows.clear();
              accumulator = accumulator + flow;

              for (Relationship r : p.relationships())
              {
                  r.setProperty("weight", (Integer)r.getProperty("weight") - flow );
              }

          }
          tx.success();
      }
      catch ( Exception e )
      {
          tx.failure();
      }
      finally
      {
          tx.finish();
      }

      return accumulator;
    }

    @GET
    @Path("/max_flow/{source}/{sink}")
    public String getMaxFlow(@PathParam("source") Long source_id, @PathParam("sink") Long sink_id, @Context GraphDatabaseService db) throws IOException {
        Node source = db.getNodeById(source_id);
        Node sink = db.getNodeById(sink_id);
        return String.valueOf(getFlow(db, source, sink));
    }
}