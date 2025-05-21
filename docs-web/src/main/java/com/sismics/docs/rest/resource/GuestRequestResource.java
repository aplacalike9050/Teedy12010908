package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.GuestRequestDao;
import com.sismics.docs.core.model.jpa.GuestRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Path("/api/guest-requests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GuestRequestResource {

    @Inject
    private GuestRequestDao dao;

    @Context
    private SecurityContext securityContext;

    private String getCurrentUserId() {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "system";
    }

    /**
     * 提交来宾注册申请
     */
    @POST
    public Response submit(GuestRequest req) {
        req.setRequestedAt(LocalDateTime.now());
        req.setStatus("PENDING");
        String userId = getCurrentUserId();
        Long newId = dao.create(req, userId);
        Optional<GuestRequest> saved = dao.findById(newId);
        return saved.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * 列出所有申请
     */
    @GET
    public Response listAll() {
        List<GuestRequest> list = dao.listAll();
        return Response.ok(list).build();
    }

    /**
     * 管理员审批：accept=true 则批准，否则拒绝
     */
    @POST
    @Path("{id}/decision")
    public Response decide(@PathParam("id") Long id,
                           @QueryParam("accept") boolean accept) {
        String userId = getCurrentUserId();
        Optional<GuestRequest> updated = dao.decide(id, accept, userId);
        return updated.map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}