package com.jugger.afc;

public final class ApiConstants {
    public static final String API_V1 = "/api/v1";

    public static final String AUTH = API_V1 + "/auth";
    public static final String USERS = API_V1 + "/users";

    public static final String ADMIN = API_V1 + "/admin";
    public static final String ADMIN_EVENTS = ADMIN + "/events";
    public static final String ADMIN_VENUES = ADMIN + "/venues";

    public static final String EVENTS = API_V1 + "/events";
    public static final String VENUES = API_V1 + "/venues";
    public static final String GROUPS = API_V1 + "/groups";

    private ApiConstants() {
    }
}
