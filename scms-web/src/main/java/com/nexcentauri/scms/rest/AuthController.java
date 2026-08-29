package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.entity.SystemUser;
import com.nexcentauri.scms.rest.dto.LoginRequest;
import com.nexcentauri.scms.rest.dto.RegisterRequest;
import com.nexcentauri.scms.dto.UserProfileDTO;
import com.nexcentauri.scms.service.AuthService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
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

            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Json.createObjectBuilder()
                                .add("success", false)
                                .add("error", "Login request is required.")
                                .build())
                        .build();
            }

            SystemUser user = authService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            String fName = user.getFirstName() != null ? user.getFirstName() : "";
            String lName = user.getLastName() != null ? user.getLastName() : "";
            String phone = user.getMobileNumber() != null ? user.getMobileNumber() : "";
            String dept = user.getDepartment() != null ? user.getDepartment() : "";
            String hub = user.getPrimaryHub() != null ? user.getPrimaryHub() : "";

            String jsonResponse = String.format(
                    "{\"success\": true, \"email\": \"%s\", \"role\": \"%s\", \"firstName\": \"%s\"," +
                            " \"lastName\": \"%s\", \"phone\": \"%s\", \"department\": \"%s\", \"hub\": \"%s\"}",
                    user.getEmail(), user.getRole(), fName, lName, phone, dept, hub
            );

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {

            JsonObject errorJson = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Authentication failed.")
                    .build();

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorJson)
                    .build();
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

            JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Registration successful.")
                    .build();

            return Response.ok(response).build();

        } catch (Exception e) {

            JsonObject error = Json.createObjectBuilder()
                    .add("success", false)
                    .add("error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Registration failed.")
                    .build();

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
    }

    @PUT
    @Path("/profile")
    public Response updateProfile(UserProfileDTO request) {
        try {
            authService.updateUserProfile(request);
            return Response.ok("{\"success\": true, \"message\": \"Profile Updated\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

}
