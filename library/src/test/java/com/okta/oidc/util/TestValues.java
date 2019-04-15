/*
 * Copyright (c) 2019, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License,
 * Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.okta.oidc.util;

import android.net.Uri;
import android.text.TextUtils;

import com.okta.oidc.AuthenticationPayload;
import com.okta.oidc.OIDCAccount;
import com.okta.oidc.net.HttpConnection;
import com.okta.oidc.net.request.HttpRequest;
import com.okta.oidc.net.request.HttpRequestBuilder;
import com.okta.oidc.net.request.IntrospectRequest;
import com.okta.oidc.net.request.NativeAuthorizeRequest;
import com.okta.oidc.net.request.ProviderConfiguration;
import com.okta.oidc.net.request.RefreshTokenRequest;
import com.okta.oidc.net.request.RevokeTokenRequest;
import com.okta.oidc.net.request.TokenRequest;
import com.okta.oidc.net.request.web.AuthorizeRequest;
import com.okta.oidc.net.response.TokenResponse;
import com.okta.oidc.net.response.web.AuthorizeResponse;
import com.okta.oidc.net.response.web.LogoutResponse;

import org.robolectric.util.Pair;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class TestValues {
    public static final String CUSTOM_URL = "https://com.okta.test/";
    public static final String CUSTOM_STATE = "CUSTOM_STATE";
    public static final String CUSTOM_NONCE = "CUSTOM_NONCE";
    public static final String CUSTOM_CODE = "CUSTOM_CODE";
    public static final String LOGIN_HINT = "LOGIN_HINT";
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String ID_TOKEN = "ID_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String SESSION_TOKEN = "SESSION_TOKEN";
    public static final String EXCHANGE_CODE = "EXCHANGE_CODE";
    public static final String CUSTOM_USER_AGENT = "CUSTOM_USER_AGENT";
    public static int VALID_EXPIRES_IN = 3600;
    public static String INVALID_EXPIRES_IN = "INVALID_EXPIRES_IN";
    public static String[] VALID_SCOPES = new String[]{"openid", "profile", "offline_access"};

    public static final String REDIRECT_URI = CUSTOM_URL + "callback";
    public static final String END_SESSION_URI = CUSTOM_URL + "logout";
    public static final String[] SCOPES = {"openid", "profile", "offline_access"};

    public static final String REVOCATION_ENDPOINT = "revoke";
    public static final String AUTHORIZATION_ENDPOINT = "authorize";
    public static final String TOKEN_ENDPOINT = "token";
    public static final String INTROSPECT_ENDPOINT = "introspect";
    public static final String REGISTRATION_ENDPOINT = "registration";
    public static final String END_SESSION_ENDPOINT = "logout";
    public static final String USERINFO_ENDPOINT = "userinfo";
    public static final String JWKS_ENDPOINT = "keys";

    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";

    public static final String PROMPT = "none";

    //Token response
    public static final String TYPE_BEARER = "Bearer";
    public static final String EXPIRES_IN = "3600";

    public static OIDCAccount getAccountWithUrl(String url) {
        return new OIDCAccount.Builder()
                .clientId(CLIENT_ID)
                .redirectUri(REDIRECT_URI)
                .endSessionRedirectUri(END_SESSION_URI)
                .scopes(SCOPES)
                .discoveryUri(url)
                .create();
    }

    public static ProviderConfiguration getProviderConfiguration(String url) {
        ProviderConfiguration configuration = new ProviderConfiguration();
        configuration.issuer = url;
        configuration.revocation_endpoint = url + REVOCATION_ENDPOINT;
        configuration.authorization_endpoint = url + AUTHORIZATION_ENDPOINT;
        configuration.token_endpoint = url + TOKEN_ENDPOINT;
        configuration.introspection_endpoint = url + INTROSPECT_ENDPOINT;
        configuration.jwks_uri = url + JWKS_ENDPOINT;
        configuration.registration_endpoint = url + REGISTRATION_ENDPOINT;
        configuration.end_session_endpoint = url + END_SESSION_ENDPOINT;
        configuration.userinfo_endpoint = url + USERINFO_ENDPOINT;
        return configuration;
    }

    public static String getJwt(String issuer, String nonce, String... audience) {
        return getJwt(issuer, nonce, DateUtil.getTomorrow(), DateUtil.getNow(), audience);
    }

    public static String getExpiredJwt(String issuer, String nonce, String... audience) {
        return getJwt(issuer, nonce, DateUtil.getYesterday(), DateUtil.getNow(), audience);
    }

    public static String getJwtIssuedAtTimeout(String issuer, String nonce, String... audience) {
        return getJwt(issuer, nonce, DateUtil.getExpiredFromTomorrow(), DateUtil.getTomorrow(), audience);
    }

    public static String getJwt(String issuer, String nonce, Date expiredDate, Date issuedAt,
                                String... audience) {
        JwtBuilder builder = Jwts.builder();
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        Map<String, Object> map = new HashMap<>();
        map.put(Claims.AUDIENCE, Arrays.asList(audience));

        return builder
                .addClaims(map)
                .claim("nonce", nonce)
                .setIssuer(issuer)
                .setSubject("sub")
                .setExpiration(expiredDate)
                .setIssuedAt(issuedAt)
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public static AuthorizeRequest getAuthorizeRequest(OIDCAccount account, String verifier) {
        return new AuthorizeRequest.Builder().codeVerifier(verifier)
                .authorizeEndpoint(account.getDiscoveryUri().toString())
                .redirectUri(account.getRedirectUri().toString())
                .scope(SCOPES)
                .nonce(CUSTOM_NONCE)
                .authenticationPayload(new AuthenticationPayload.Builder()
                        .setState(CUSTOM_STATE).build())
                .create();
    }

    public static AuthorizeResponse getAuthorizeResponse(String state, String code) {
        String uri = String.format("com.okta.test:/callback?code=%s&state=%s", code, state);
        return AuthorizeResponse.fromUri(Uri.parse(uri));
    }

    public static AuthorizeResponse getInvalidAuthorizeResponse(String error, String desc) {
        String uri = String.format("com.okta.test:/callback?error=%s&error_description=%s"
                , error, desc);
        return AuthorizeResponse.fromUri(Uri.parse(uri));
    }

    public static LogoutResponse getLogoutResponse(String state) {
        String uri = String.format("com.okta.test:/logout?state=%s", state);
        return LogoutResponse.fromUri(Uri.parse(uri));
    }

    public static TokenRequest getTokenRequest(OIDCAccount account, AuthorizeRequest request,
                                               AuthorizeResponse response, ProviderConfiguration
                                                       configuration) {
        return (TokenRequest) HttpRequestBuilder.newRequest()
                .request(HttpRequest.Type.TOKEN_EXCHANGE)
                .authRequest(request)
                .authResponse(response)
                .account(account)
                .providerConfiguration(configuration)
                .createRequest();
    }

    public static RefreshTokenRequest getRefreshRequest(OIDCAccount account, TokenResponse response,
                                                        ProviderConfiguration configuration) {
        return (RefreshTokenRequest) HttpRequestBuilder.newRequest()
                .request(HttpRequest.Type.REFRESH_TOKEN)
                .tokenResponse(response)
                .account(account)
                .providerConfiguration(configuration)
                .createRequest();
    }

    public static RevokeTokenRequest getRevokeTokenRequest(OIDCAccount account, String tokenToRevoke,
                                                           ProviderConfiguration configuration) {
        return (RevokeTokenRequest) HttpRequestBuilder.newRequest()
                .request(HttpRequest.Type.REVOKE_TOKEN)
                .tokenToRevoke(tokenToRevoke)
                .providerConfiguration(configuration)
                .account(account)
                .createRequest();
    }

    public static IntrospectRequest getIntrospectTokenRequest(OIDCAccount account, String token, String tokenType,
                                                              ProviderConfiguration configuration) {
        return (IntrospectRequest) HttpRequestBuilder.newRequest()
                .request(HttpRequest.Type.INTROSPECT)
                .introspect(token, tokenType)
                .providerConfiguration(configuration)
                .account(account)
                .createRequest();
    }

    public static NativeAuthorizeRequest getNativeLogInRequest(OIDCAccount account, String token,
                                                               ProviderConfiguration configuration) {
        return new AuthorizeRequest.Builder()
                .account(account)
                .providerConfiguration(configuration)
                .sessionToken(token)
                .nonce(CUSTOM_NONCE)
                .authenticationPayload(null)
                .createNativeRequest(new HttpConnection.DefaultConnectionFactory());
    }

    public static String getAuthorizationExceptionError() {
        return "{\"" + AuthorizationException.KEY_TYPE + "\"=" + AuthorizationException.TYPE_GENERAL_ERROR + ", " +
                "\"" + AuthorizationException.KEY_CODE + "\"= 1, " +
                "\"" + AuthorizationException.KEY_ERROR + "\"=\"" + "key_error" + "\", " +
                "\"" + AuthorizationException.KEY_ERROR_DESCRIPTION + "\"=\"" + "key_error_description" + "\", " +
                "\"" + AuthorizationException.KEY_ERROR_URI + "\"=\"" + "https://some_uri" + "\" }";

    }

    public static TokenResponse getTokenResponse() {
        return TokenResponse.RESTORE.restore(
                generatePayloadTokenResponse(
                        ACCESS_TOKEN,
                        ID_TOKEN,
                        REFRESH_TOKEN,
                        Integer.toString(VALID_EXPIRES_IN),
                        TextUtils.join(" ", VALID_SCOPES))
        );
    }

    public static AuthenticationPayload getAuthenticationPayload(Pair<String, String> parameter) {
        return new AuthenticationPayload.Builder()
                .setLoginHint(LOGIN_HINT)
                .setState(CUSTOM_STATE)
                .addParameter(parameter.first, parameter.second)
                .build();
    }

    public static PersistableMock getNotEncryptedPersistable() {
        return new PersistableMock("data");
    }

    public static EncryptedPersistableMock getEncryptedPersistable() {
        return new EncryptedPersistableMock("data");
    }

    public static String generatePayloadTokenResponse(String accessToken, String idToken, String refreshToken, String expiresIn, String scope) {
        return "{\"access_token\"=\"" + accessToken + "\", " +
                "\"id_token\"=\"" + idToken + "\", " +
                "\"refresh_token\"=\"" + refreshToken + "\", " +
                "\"expires_in\"=\"" + expiresIn + "\", " +
                "\"scope\"=\"" + scope + "\"}";
    }
}