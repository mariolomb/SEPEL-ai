package it.sepel.ai.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class IpWhiteListFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpWhiteListFilter.class);
    private static final List<String> ALLOWED_IPS = List.of("127.0.0.1", "85.235.137.222", "217.59.150.93");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        log.info("Richiesta da IP: " + clientIp);

        if (!ALLOWED_IPS.contains(clientIp)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Accesso negato: IP non autorizzato");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
