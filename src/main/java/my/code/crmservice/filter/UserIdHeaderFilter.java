package my.code.crmservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter: Spring гарантує що filter виконується рівно один раз
// на запит (не повторно при forward/include)
@Slf4j
@Component
public class UserIdHeaderFilter extends OncePerRequestFilter {

    // Хедер який gateway інжектує після валідації JWT
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);

                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                // principal = userId (Long) — дістаємо в контролері через authentication.getPrincipal()
                // credentials = null — не потрібні (токен вже перевірений gateway)
                // authorities = [] — crm-service не використовує role-based access
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (NumberFormatException e) {
                // Невалідний X-User-Id хедер — логуємо і продовжуємо без authentication.
                // SecurityConfig відхилить запит як unauthenticated (401)
                log.warn("Invalid X-User-Id header value: '{}'", userIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}
