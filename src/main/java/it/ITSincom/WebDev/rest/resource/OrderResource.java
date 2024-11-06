package it.ITSincom.WebDev.rest.resource;

import it.ITSincom.WebDev.persistence.OrderRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    @Inject
    OrderRepository orderRepository;

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @GET
    @Path("/test-connection")
    public Response testMongoDBConnection() {
        try {
            // Prova a ottenere il numero di ordini per verificare la connessione
            long count = orderRepository.count();
            LOG.info("Numero di ordini: " + count);
            return Response.ok("Connessione a MongoDB riuscita. Numero di ordini: " + count).build();
        } catch (Exception e) {
            LOG.error("Errore nella connessione a MongoDB: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nella connessione a MongoDB: " + e.getMessage())
                    .build();
        }
    }
}
