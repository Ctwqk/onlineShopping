package com.taiwei.reggie.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.taiwei.reggie.support.CatalogueFixtures.category;
import static com.taiwei.reggie.support.CatalogueFixtures.dish;
import static com.taiwei.reggie.support.CatalogueFixtures.setmeal;
import static org.assertj.core.api.Assertions.assertThat;

class DishFlowIT extends AbstractIntegrationTest {

    private static final List<Map<String, String>> SPICE =
            List.of(Map.of("name", "Spice level", "value", "[\"Mild\",\"Hot\"]"));

    @Test
    void adminCreatesADishWithFlavorsAndSeesItInTheAdminListing() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);

        String id = dish(admin, mains, "Kung Pao Chicken", 1888, SPICE);

        JsonNode page = admin.get("/dish/page?page=1&pageSize=10").data();
        assertThat(page.path("total").asInt()).isEqualTo(1);
        JsonNode row = page.path("records").get(0);
        assertThat(row.path("name").asText()).isEqualTo("Kung Pao Chicken");
        assertThat(row.path("categoryName").asText()).isEqualTo("Mains");
        assertThat(row.path("price").asInt()).isEqualTo(1888);

        JsonNode detail = admin.get("/dish/" + id).data();
        assertThat(detail.path("flavors")).hasSize(1);
        assertThat(detail.path("flavors").get(0).path("name").asText()).isEqualTo("Spice level");
    }

    @Test
    void adminPageToleratesDishesWhoseCategoryWasRemoved() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        dish(admin, mains, "Orphan", 500);
        jdbc.update("DELETE FROM category");

        JsonNode row = admin.get("/dish/page?page=1&pageSize=10").assertSuccess().data().path("records").get(0);

        assertThat(row.path("name").asText()).isEqualTo("Orphan");
        assertThat(row.path("categoryName").isNull()).isTrue();
    }

    @Test
    void customerMenuOnlyShowsOnSaleDishesOfTheCategory() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String drinks = category(admin, "Drinks", 1);
        dish(admin, mains, "Mapo Tofu", 1568, SPICE);
        String stopped = dish(admin, mains, "Seasonal Fish", 2488);
        dish(admin, drinks, "Jasmine Tea", 388);
        admin.post("/dish/status/0?ids=" + stopped, Map.of()).assertSuccess();

        ApiClient customer = customerClient("13800138000");
        JsonNode menu = customer.get("/dish/list?categoryId=" + mains + "&status=1").assertSuccess().data();

        assertThat(menu).hasSize(1);
        assertThat(menu.get(0).path("name").asText()).isEqualTo("Mapo Tofu");
        assertThat(menu.get(0).path("flavors")).hasSize(1); // flavours come with the menu
    }

    @Test
    void menuIsCachedInRedisAndEvictedWhenTheAdminChangesTheCatalogue() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String id = dish(admin, mains, "Dan Dan Noodles", 1388);
        ApiClient customer = customerClient("13800138000");

        customer.get("/dish/list?categoryId=" + mains).assertSuccess();
        assertThat(redis.keys("dishCache*")).isNotEmpty(); // served from cache from now on

        // a price edit must show up immediately...
        JsonNode current = admin.get("/dish/" + id).data();
        admin.put("/dish", Map.of("id", id, "name", "Dan Dan Noodles", "categoryId", mains, "price", 1488,
                "image", "seed.jpg", "status", 1, "flavors", List.of())).assertSuccess();
        assertThat(current.path("price").asInt()).isEqualTo(1388);
        assertThat(redis.keys("dishCache*")).isEmpty();
        assertThat(customer.get("/dish/list?categoryId=" + mains).data().get(0).path("price").asInt())
                .isEqualTo(1488);

        // ...and so must stopping the sale, which the original code did not invalidate
        admin.post("/dish/status/0?ids=" + id, Map.of()).assertSuccess();
        assertThat(customer.get("/dish/list?categoryId=" + mains).data()).isEmpty();
    }

    @Test
    void movingADishToAnotherCategoryUpdatesBothMenus() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String soups = category(admin, "Soups", 1);
        String id = dish(admin, mains, "Wonton Soup", 1088);
        ApiClient customer = customerClient("13800138000");
        assertThat(customer.get("/dish/list?categoryId=" + mains).data()).hasSize(1);
        assertThat(customer.get("/dish/list?categoryId=" + soups).data()).isEmpty();

        admin.put("/dish", Map.of("id", id, "name", "Wonton Soup", "categoryId", soups, "price", 1088,
                "image", "seed.jpg", "status", 1, "flavors", List.of())).assertSuccess();

        assertThat(customer.get("/dish/list?categoryId=" + mains).data()).isEmpty();
        assertThat(customer.get("/dish/list?categoryId=" + soups).data()).hasSize(1);
    }

    @Test
    void editingADishReplacesItsFlavors() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String id = dish(admin, mains, "Mapo Tofu", 1568, SPICE);

        admin.put("/dish", Map.of("id", id, "name", "Mapo Tofu", "categoryId", mains, "price", 1568,
                "image", "seed.jpg", "status", 1,
                "flavors", List.of(Map.of("name", "Numbness", "value", "[\"Low\",\"High\"]")))).assertSuccess();

        JsonNode flavors = admin.get("/dish/" + id).data().path("flavors");
        assertThat(flavors).hasSize(1);
        assertThat(flavors.get(0).path("name").asText()).isEqualTo("Numbness");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dish_flavor", Integer.class)).isEqualTo(1);
    }

    @Test
    void batchStatusChangeAcceptsACommaSeparatedIdList() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String a = dish(admin, mains, "Dish A", 100);
        String b = dish(admin, mains, "Dish B", 200);

        admin.post("/dish/status/0?ids=" + a + "," + b, Map.of()).assertSuccess();

        assertThat(jdbc.queryForList("SELECT status FROM dish", Integer.class)).containsOnly(0);
    }

    @Test
    void onlyStoppedDishesOutsideSetmealsCanBeDeleted() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String combos = category(admin, "Combos", 2);
        String onSale = dish(admin, mains, "On Sale", 100);
        String stopped = dish(admin, mains, "Stopped", 100, SPICE);
        String inSetmeal = dish(admin, mains, "In Setmeal", 100);
        setmeal(admin, combos, "Combo", 150, List.of(inSetmeal));
        admin.post("/dish/status/0?ids=" + stopped + "," + inSetmeal, Map.of()).assertSuccess();

        admin.delete("/dish?ids=" + onSale).assertFailure("on sale");
        admin.delete("/dish?ids=" + inSetmeal).assertFailure("setmeal");
        ApiClient.Response mixed = admin.delete("/dish?ids=" + onSale + "," + stopped + "," + inSetmeal);

        mixed.assertSuccess(); // the deletable one went away, the others were kept
        assertThat(jdbc.queryForList("SELECT name FROM dish ORDER BY name", String.class))
                .containsExactly("In Setmeal", "On Sale");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dish_flavor", Integer.class)).isZero();
    }

    @Test
    void duplicateDishNamesAreRejectedWithAReadableMessage() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        dish(admin, mains, "Twin", 100);

        admin.post("/dish", Map.of("name", "Twin", "categoryId", mains, "price", 100, "status", 1,
                        "flavors", List.of()))
                .assertFailure("already exists");
    }

    @Test
    void aCategoryInUseCannotBeDeleted() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String empty = category(admin, "Empty", 1);
        dish(admin, mains, "Anything", 100);

        admin.delete("/category?ids=" + mains).assertFailure("linked");
        admin.delete("/category?ids=" + empty).assertSuccess();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM category", Integer.class)).isEqualTo(1);
    }

    @Test
    void setmealLifecycleListDetailsStatusAndDelete() {
        ApiClient admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String combos = category(admin, "Combos", 2);
        String rice = dish(admin, mains, "Fried Rice", 1288);
        String tea = dish(admin, mains, "Tea", 388);
        String id = setmeal(admin, combos, "Lunch Set", 1500, List.of(rice, tea));
        ApiClient customer = customerClient("13800138000");

        // customer sees it, with its dishes in the "details" dialog
        assertThat(customer.get("/setmeal/list?categoryId=" + combos + "&status=1").data()).hasSize(1);
        JsonNode dishes = customer.get("/setmeal/dish/" + id).assertSuccess().data();
        assertThat(dishes).extracting(n -> n.path("name").asText()).containsExactlyInAnyOrder("Fried Rice", "Tea");
        assertThat(dishes.get(0).path("copies").asInt()).isEqualTo(1);
        assertThat(admin.get("/setmeal/" + id).data().path("setmealDishes")).hasSize(2);

        // stopping the sale hides it (cache evicted) and makes it deletable
        admin.delete("/setmeal?ids=" + id).assertFailure("in sale");
        admin.post("/setmeal/status/0?ids=" + id, Map.of()).assertSuccess();
        assertThat(customer.get("/setmeal/list?categoryId=" + combos + "&status=1").data()).isEmpty();

        admin.delete("/setmeal?ids=" + id).assertSuccess();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM setmeal", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM setmeal_dish", Integer.class)).isZero();
    }
}
