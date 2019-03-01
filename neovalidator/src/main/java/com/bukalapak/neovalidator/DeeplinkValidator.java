package com.bukalapak.neovalidator;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeeplinkValidator {
    public static boolean validateDeeplinkConfig(@NotNull String idConfig, @NotNull String configJson) {
        switch (idConfig) {
            case "dynamic-deeplink":
                return checkV1(configJson, null);
            case "dynamic-deeplink-v2":
                return checkV2(configJson, null);
            default:
                return false;
        }
    }

    public static boolean validateDeeplinkConfig(@NotNull String idConfig, @NotNull String configJson, OnDeeplinkCheckListener listener) {
        switch (idConfig) {
            case "dynamic-deeplink":
                return checkV1(configJson, listener);
            case "dynamic-deeplink-v2":
                return checkV2(configJson, listener);
            default:
                invokeInvalidIfListenerNotNull(listener, new IllegalArgumentException("config id not found"));
                return false;
        }
    }

    private static boolean checkV2(String configJson, OnDeeplinkCheckListener listener) {
        try {
            DynamicDeeplink.DynamicDeeplinkV2 result = new Gson().fromJson(configJson, DynamicDeeplink.DynamicDeeplinkV2.class);
            if (result == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: dynamic-deeplink-v2"));
                return false;
            }
            if (result.deeplink == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: deeplink:{"));
                return false;
            }

            List<String> keys = collectKey(result.deeplink);
            if (keys.size() < 1 || keys.size() == 1 && keys.get(0).equals("deeplink")) {
                invokeInvalidIfListenerNotNull(listener, new IllegalArgumentException("IllegalArgumentException: deeplink:{"));
                return false;
            }
            for (String key : keys) {
                DynamicDeeplink.DeeplinkMap map = result.deeplink.get(key).map;
                if (map.expressions == null) {
                    invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: " + key + "map expressions"));
                    return false;
                }

                List<String> keyPaths = collectKey(result.deeplink.get(key).path);
                if (keyPaths != null) {
                    for (String keyPath : keyPaths) {
                        checkValue(keyPath, result.deeplink.get(key).path.get(keyPath).expressions);
                    }
                }
            }
            invokeValidIfListenerNotNull(listener);
            return true;
        } catch (Exception e) {
            invokeInvalidIfListenerNotNull(listener, e);
            return false;
        }
    }

    private static boolean checkV1(String configJson, OnDeeplinkCheckListener listener) {
        try {
            DynamicDeeplink.DynamicDeeplinkV1 result = new Gson().fromJson(configJson, DynamicDeeplink.DynamicDeeplinkV1.class);

            if (result == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: new-dynamic-deeplink"));
                return false;
            }
            if (result.map == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: map:{"));
                return false;
            }
            if (result.premap == null) {
                invokeInvalidIfListenerNotNull(listener, new NullPointerException("NullPointerException: premap:{"));
                return false;
            }

            List<String> keys = collectKey(result.map);
            for (String key : keys) {
                checkValue(key, result.map.get(key).expressions);
            }
            invokeValidIfListenerNotNull(listener);
            return true;
        } catch (Exception e) {
            invokeInvalidIfListenerNotNull(listener, e);
            return false;
        }
    }

    private static List<String> collectKey(Map<String, ?> map) {
        if (map != null) {
            return new ArrayList<>(map.keySet());
        } else {
            return null;
        }
    }

    private static void checkValue(String key, Map<String, String> map) throws NullPointerException, IllegalArgumentException {
        if (map == null) throw new NullPointerException("NullPointerException " + key + " expression");
        List<String> list = new ArrayList<>(map.values());
        for (String aList : list) {
            if (aList.contains("//")) {
                throw new IllegalArgumentException("Illegal expressions " + key + " : " + aList);
            }
        }
    }

    private static void invokeInvalidIfListenerNotNull(OnDeeplinkCheckListener listener, Exception e) {
        if (listener != null) {
            listener.onDeeplinkInvalid(e);
        }
    }

    private static void invokeValidIfListenerNotNull(OnDeeplinkCheckListener listener) {
        if (listener != null) {
            listener.onDeeplinkValid();
        }
    }
}