package com.nexcentauri.scms.rest; // ඔබගේ නිවැරදි Package නාමය මෙහි යොදන්න

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            Response.ResponseBuilder builder = Response.ok();

            String origin = requestContext.getHeaderString("Origin");
            if (origin != null && isAllowedOrigin(origin)) {
                builder.header("Access-Control-Allow-Origin", origin);
            } else {
                builder.header("Access-Control-Allow-Origin", "http://localhost:3000"); // Fallback
            }

            builder.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            builder.header("Access-Control-Allow-Credentials", "true");
            builder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

            requestContext.abortWith(builder.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        String origin = requestContext.getHeaderString("Origin");
        if (origin != null && isAllowedOrigin(origin)) {

            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Vary", "Origin");
        } else {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "http://localhost:3000");
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }

    private boolean isAllowedOrigin(String origin) {
        return "http://localhost:3000".equals(origin) || "http://127.0.0.1:3000".equals(origin);
    }
}