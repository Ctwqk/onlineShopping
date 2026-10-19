package com.taiwei.reggie.support;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Creates categories, dishes and setmeals through the admin API, the way the admin console does.
 */
public final class CatalogueFixtures {

    private CatalogueFixtures() {
    }

    /** @return id of the new category (type 1 = dish category, 2 = setmeal category) */
    public static String category(ApiClient admin, String name, int type) {
        admin.post("/category", Map.of("name", name, "type", type, "sort", 1)).assertSuccess();
        JsonNode categories = admin.get("/category/list?type=" + type).data();
        for (JsonNode category : categories) {
            if (name.equals(category.path("name").asText())) {
                return category.path("id").asText();
            }
        }
        throw new IllegalStateException("category not found: " + name);
    }

    /** Creates an on-sale dish priced in cents and returns its id. */
    public static String dish(ApiClient admin, String categoryId, String name, int priceInCents,
                              List<Map<String, String>> flavors) {
        admin.post("/dish", Map.of(
                "name", name,
                "categoryId", categoryId,
                "price", priceInCents,
                "code", name.toLowerCase().replace(' ', '-'),
                "image", "seed.jpg",
                "description", name + " description",
                "status", 1,
                "flavors", flavors)).assertSuccess();
        JsonNode page = admin.get("/dish/page?page=1&pageSize=100&name=" + name.replace(" ", "%20")).data()
                .path("records");
        return page.get(0).path("id").asText();
    }

    public static String dish(ApiClient admin, String categoryId, String name, int priceInCents) {
        return dish(admin, categoryId, name, priceInCents, List.of());
    }

    /** Creates an on-sale setmeal containing the given dish ids (one portion each). */
    public static String setmeal(ApiClient admin, String categoryId, String name, int priceInCents,
                                 List<String> dishIds) {
        List<Map<String, Object>> dishes = dishIds.stream()
                .map(id -> Map.<String, Object>of("dishId", id, "name", "dish " + id, "price", 100, "copies", 1))
                .toList();
        admin.post("/setmeal", Map.of(
                "name", name,
                "categoryId", categoryId,
                "price", priceInCents,
                "code", name.toLowerCase().replace(' ', '-'),
                "image", "seed.jpg",
                "description", name,
                "status", 1,
                "setmealDishes", dishes)).assertSuccess();
        JsonNode page = admin.get("/setmeal/page?page=1&pageSize=100&name=" + name.replace(" ", "%20")).data()
                .path("records");
        return page.get(0).path("id").asText();
    }
}
