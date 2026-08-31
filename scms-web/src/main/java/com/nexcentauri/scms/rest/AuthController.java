package com.nexcentauri.scms.rest;

import com.nexcentauri.scms.rest.dto.LoginRequest;
import com.nexcentauri.scms.rest.dto.RegisterRequest;
import com.nexcentauri.scms.service.AuthService;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @EJB
    private AuthService authService;

    @Inject
    private SecurityContext securityContext;


    @POST
    @Path("/login")
    public Response login(
            LoginRequest loginRequest,
            @Context HttpServletRequest request,
            @Context HttpServletResponse servletResponse
    ) {

        if (loginRequest == null) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            Json.createObjectBuilder()
                                    .add("success", false)
                                    .add("error", "Login details are required.")
                                    .build()
                    )
                    .build();
        }

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            Json.createObjectBuilder()
                                    .add("success", false)
                                    .add(
                                            "error",
                                            "Email and password are required."
                                    )
                                    .build()
                    )
                    .build();
        }

        try {


            AuthenticationStatus status =
                    securityContext.authenticate(
                            request,
                            servletResponse,
                            AuthenticationParameters
                                    .withParams()
                                    .newAuthentication(true)
                                    .credential(
                                            new UsernamePasswordCredential(
                                                    email.trim(),
                                                    password
                                            )
                                    )
                    );

            if (status != AuthenticationStatus.SUCCESS) {

                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(
                                Json.createObjectBuilder()
                                        .add("success", false)
                                        .add(
                                                "error",
                                                "Invalid email or password."
                                        )
                                        .build()
                        )
                        .build();
            }

            String authenticatedEmail =
                    securityContext.getCallerPrincipal() != null
                            ? securityContext
                            .getCallerPrincipal()
                            .getName()
                            : email.trim();

            String role = getCurrentUserRole();


            JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("email", authenticatedEmail)
                    .add("role", role)
                    .build();

            return Response.ok(response).build();

        } catch (Exception e) {

            JsonObject errorResponse =
                    Json.createObjectBuilder()
                            .add("success", false)
                            .add(
                                    "error",
                                    "Authentication failed."
                            )
                            .build();

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorResponse)
                    .build();
        }
    }


    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {

        if (request == null) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            Json.createObjectBuilder()
                                    .add("success", false)
                                    .add(
                                            "error",
                                            "Registration details are required."
                                    )
                                    .build()
                    )
                    .build();
        }

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

            JsonObject response =
                    Json.createObjectBuilder()
                            .add("success", true)
                            .add(
                                    "message",
                                    "Registration successful."
                            )
                            .build();

            return Response.ok(response).build();

        } catch (Exception e) {

            String message =
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Registration failed.";

            JsonObject errorResponse =
                    Json.createObjectBuilder()
                            .add("success", false)
                            .add("error", message)
                            .build();

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }


    @GET
    @Path("/verify")
    public Response verifySession() {

        try {

            if (securityContext.getCallerPrincipal() == null) {

                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(
                                Json.createObjectBuilder()
                                        .add("success", false)
                                        .add(
                                                "error",
                                                "No active authenticated session."
                                        )
                                        .build()
                        )
                        .build();
            }

            String email =
                    securityContext
                            .getCallerPrincipal()
                            .getName();

            String role = getCurrentUserRole();

            JsonObject response =
                    Json.createObjectBuilder()
                            .add("success", true)
                            .add("email", email)
                            .add("role", role)
                            .build();

            return Response.ok(response).build();

        } catch (Exception e) {

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(
                            Json.createObjectBuilder()
                                    .add("success", false)
                                    .add(
                                            "error",
                                            "Session verification failed."
                                    )
                                    .build()
                    )
                    .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(
            @Context HttpServletRequest request
    ) {

        try {

            HttpSession session =
                    request.getSession(false);

            request.logout();


            if (session != null) {
                session.invalidate();
            }

            JsonObject response =
                    Json.createObjectBuilder()
                            .add("success", true)
                            .add(
                                    "message",
                                    "Logged out successfully."
                            )
                            .build();

            return Response.ok(response).build();

        } catch (ServletException e) {

            JsonObject errorResponse =
                    Json.createObjectBuilder()
                            .add("success", false)
                            .add(
                                    "error",
                                    "Logout failed."
                            )
                            .build();

            return Response.serverError()
                    .entity(errorResponse)
                    .build();
        }
    }


    private String getCurrentUserRole() {

        if (securityContext.isCallerInRole(
                "LOGISTICS_PERSONNEL"
        )) {
            return "LOGISTICS_PERSONNEL";
        }

        if (securityContext.isCallerInRole(
                "CUSTOMS_OFFICIAL"
        )) {
            return "CUSTOMS_OFFICIAL";
        }

        if (securityContext.isCallerInRole(
                "VENDOR"
        )) {
            return "VENDOR";
        }

        return "UNKNOWN";
    }
}