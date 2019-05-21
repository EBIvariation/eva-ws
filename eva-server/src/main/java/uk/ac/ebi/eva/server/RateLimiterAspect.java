/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.eva.server;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Aspect
@Component
public class RateLimiterAspect {

    private static final String RATE_LIMIT_PRECONDITION_FAIL = "Context HttpServletRequest object " +
            "must be passed in as the last parameter in the relevant methods to use the @RateLimit support";

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final int RATE_LIMIT_ACQUIRE_TIMEOUT_IN_SECONDS = 10;

    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Before("@annotation(limit)")
    public void rateLimit(JoinPoint jp, RateLimit limit) throws RateLimitException {
        RateLimiter limiter = limiters.computeIfAbsent(getIPAddress(jp), createLimiter(limit));
        boolean acquired = limiter.tryAcquire(RATE_LIMIT_ACQUIRE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        if (!acquired) {
            logger.error("Could not acquire rate limit permission");
            throw new RateLimitException(String.format("Rate limit exceeded. Please limit rate to %d requests/second.",
                                                       limit.value()));
        }
        logger.debug("Acquired rate limit permission");
    }

    private Function<String, RateLimiter> createLimiter(RateLimit limit) {
        return name -> RateLimiter.create(limit.value());
    }

    private String getIPAddress(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length <= 0) {
            throw new IllegalArgumentException(RATE_LIMIT_PRECONDITION_FAIL);
        }
        Object lastParam = args[args.length - 1];
        // Get client IP address
        // To account for clients which are behind a proxy server or a load balancer,
        // use the client IP address via the HTTP request header X-Forwarded-For (XFF).
        if (lastParam instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) lastParam;
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            return ipAddress == null ? request.getRemoteAddr() : ipAddress;
        } else {
            throw new IllegalArgumentException(RATE_LIMIT_PRECONDITION_FAIL);
        }
    }
}
