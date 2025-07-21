package com.millo.ilhayoung.auth.oauth2

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import jakarta.servlet.http.HttpServletRequest

/**
 * OAuth2 인증 요청 시 role 파라미터를 세션에 저장하는 커스텀 리졸버
 */
class CustomOAuth2AuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizationRequestBaseUri: String
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        authorizationRequestBaseUri
    )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        // role 파라미터를 세션에 저장
        val role = request.getParameter("role")
        if (role != null) {
            request.session.setAttribute("requestedRole", role)
            println("🔥 OAuth2 인증 요청 - role을 세션에 저장: $role")
        }
        
        return defaultResolver.resolve(request)
    }

    override fun resolve(
        request: HttpServletRequest,
        clientRegistrationId: String
    ): OAuth2AuthorizationRequest? {
        // role 파라미터를 세션에 저장
        val role = request.getParameter("role")
        if (role != null) {
            request.session.setAttribute("requestedRole", role)
            println("🔥 OAuth2 인증 요청 - role을 세션에 저장: $role")
        }
        
        return defaultResolver.resolve(request, clientRegistrationId)
    }
} 