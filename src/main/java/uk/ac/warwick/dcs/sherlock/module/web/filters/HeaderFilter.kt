package uk.ac.warwick.dcs.sherlock.module.web.filters

import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException

/**
 * Filters all requests to add the current URL to the http response headers
 */
@WebFilter("/*")
class HeaderFilter : Filter {
    /**
     * Adds the URI of the request as a new "sherlock-url" header variable
     * in the http response.
     *
     * This is used by the JavaScript requests to detect if the response of
     * a form was a redirect (e.g. if you add a workspace, the response is
     * not a page, but a redirect to the new workspace page) so the JavaScript
     * knows to redirect the user.
     *
     * @param request
     * @param response
     * @param chain
     *
     * @throws IOException
     * @throws ServletException
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val httpServletResponse = response as HttpServletResponse
        val httpServletRequest = request as HttpServletRequest

        var url = httpServletRequest.requestURI
        if (httpServletRequest.parameterMap.containsKey("msg")) {
            val strings = httpServletRequest.parameterMap["msg"]?.let { listOf(*it) }
            if (strings != null) {
                if (strings.size == 1) {
                    url += "?msg=" + (strings[0] ?: "")
                }
            }
        }

        httpServletResponse.setHeader("sherlock-url", url)

        chain.doFilter(request, response)
    }
}