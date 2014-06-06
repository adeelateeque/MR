package com.codurs.meetroulette.core;

/**
 * MeetRoulette constants
 */
public class Constants
{

    public static class Auth
    {
        private Auth()
        {
        }

        /**
         * Account type id
         */
        public static final String MOOWE_ACCOUNT_TYPE = "com.codurs.meetroulette";

        /**
         * Account name
         */
        public static final String MOOWE_ACCOUNT_NAME = "codurs.meetroulette";

        /**
         * Provider id
         */
        public static final String MOOWE_PROVIDER_AUTHORITY = "com.codurs.meetroulette.sync";

        /**
         * Auth token type
         */
        public static final String AUTHTOKEN_TYPE = MOOWE_ACCOUNT_TYPE;

        /**
         * PARAM_CONFIRMCREDENTIALS
         */
        public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";

        /**
         * PARAM_AUTHTOKEN_TYPE
         */
        public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    }

    /**
     * All HTTP is done through a REST style API
     */
    public static class Http
    {
        private Http()
        {
        }

        /**
         * Base URL for all requests
         */
        public static final String URL_BASE = "http://54.251.190.11";

        /**
         * Authentication URL
         */
        public static final String URL_AUTH = URL_BASE + "/login";

        public static final String URL_PUSHER = URL_BASE + "/pusher";

        public static final String URL_PUSHER_AUTH = URL_PUSHER + "/auth";

        /**
         * User URL
         */
        public static final String URL_USER = URL_BASE + "/user";

        /**
         * Movie URL
         */
        public static final String URL_MOVIE = URL_BASE + "/movie";

        /**
         * Theater URL
         */
        public static final String URL_THEATER = URL_BASE + "/theater";

        public static final String URL_THEATER_NEAR = URL_THEATER + "/search/near";

        /**
         * Showtime URL
         */
        public static final String URL_SHOWTIME = URL_BASE + "/showtime";

        public static final String URL_SHOWTIME_NEAR = URL_SHOWTIME + "/search/near";

        public static final String URL_SHOWTIME_SEARCH_THEATER = URL_SHOWTIME +  "/search/findCurrentShowtimesByTheater?theater=";

        /**
         * Checkin URL
         */
        public static final String URL_CHECKIN = URL_BASE + "/checkin";

        /**
         * Plan URL
         */
        public static final String URL_PLAN = URL_BASE + "/plan";


        /**
         * Checkin URL
         */
        public static final String URL_REMINDER = URL_BASE + "/reminder";

        public static final String NEXT_REL = "next";
        public static final String PREVIOUS_REL = "previous";

        public static final String APP_ID = "zHb2bVia6kgilYRWWdmTiEJooYA17NnkBSUVsr4H";
        public static final String REST_API_KEY = "N2kCY1T3t3Jfhf9zpJ5MCURn3b25UpACILhnf5u9";
        public static final String PUSHER_API_KEY = "0de53bba370787e82241";
        public static final String HEADER_REST_API_KEY = "X-Parse-REST-API-Key";
        public static final String HEADER_APP_ID = "X-Parse-Application-Id";
        public static final String PARAM_USERNAME = "username";
        public static final String PARAM_PASSWORD = "password";
        public static final String SESSION_TOKEN = "sessionToken";

        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String CONTENT_TYPE_TEXT_URI_LSIT = "text/uri-list";
        /** The server successfully processed the request and does not have any content to return */
        public static final int NO_CONTENT_RESPONSE = 204;
    }

    public static class Extra
    {
        private Extra()
        {
        }

        public static final String NEWS_ITEM = "news_item";

        public static final String USER = "user";

    }

    public static class Intent
    {
        private Intent()
        {
        }

        /**
         * Action prefix for all intents created
         */
        public static final String INTENT_PREFIX = "com.codurs.meetroulette.";
    }

    public static class Notification
    {
        private Notification()
        {
        }

        public static final int TIMER_NOTIFICATION_ID = 1000;
    }

}