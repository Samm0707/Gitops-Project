package com.XAMMER.HRMS.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ChaosFilter implements Filter {

    @Value("${CHAOS_ERROR_RATE:0}")
    private double errorRate;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;

        boolean isActuator = httpReq.getRequestURI().startsWith("/actuator");

        if (!isActuator && errorRate > 0 && ThreadLocalRandom.current().nextDouble() < errorRate) {
            ((HttpServletResponse) res).sendError(500, "Chaos-injected failure for canary demo");
            return;
        }

        chain.doFilter(req, res);
    }
}
