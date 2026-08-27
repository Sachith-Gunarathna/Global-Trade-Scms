package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.rest.dto.LoginRequest;
import com.nexcentauri.scms.rest.dto.RegisterRequest;
import com.nexcentauri.scms.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    private AuthService authService;

    @POST
    @Path("/login")
    public Response login(LoginRequest request){

        try {
            SystemUser user = authService.authenticate(request.getEmail(), request.getPassword());

            String jsonResponse = String.format(
                    "{\"success\": true, \"email\": \"%s\",\"role\": \"%s\"}",
                    user.getEmail(),
                    user.getRole()
            );

            return Response.ok(jsonResponse).build();
        }catch (Exception e){
            String errorJson = String.format("{\"success\": false, \"error\": \"%s\"}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorJson).build();
        }

    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request){
        try {
            authService.registerUser(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getMobileNumber(),
                    request.getOrganizationOrCompany(),
                    request.getPrimaryHub(),
                    request.getDepartment(),
                    request.getRole(),
                    request.getPassword()
            );
            return Response.ok("{\"success\": true, \"message\": \"Registration Successful\"}").build();
        }catch (Exception e){
            String errorJson = String.format("{\"success\": false, \"error\": \"%s\"}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorJson).build();
        }
    }

}
