package com.taiwei.reggie.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.entity.Orders;
import com.taiwei.reggie.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.taiwei.reggie.support.CatalogueFixtures.category;
import static com.taiwei.reggie.support.CatalogueFixtures.dish;
import static com.taiwei.reggie.support.CatalogueFixtures.setmeal;
import static org.assertj.core.api.Assertions.assertThat;

class CartAndOrderFlowIT extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    private ApiClient admin;
    private ApiClient customer;
    private String noodles;   // 13.88
    private String tea;       // 3.99  (a price with cents that integer arithmetic would truncate)
    private String combo;     // 20.05

    @BeforeEach
    void menu() {
        admin = adminClient();
        String mains = category(admin, "Mains", 1);
        String combos = category(admin, "Combos", 2);
        noodles = dish(admin, mains, "Dan Dan Noodles", 1388,
                List.of(Map.of("name", "Spice level", "value", "[\"Mild\",\"Hot\"]")));
        tea = dish(admin, mains, "Jasmine Tea", 399);
        combo = setmeal(admin, combos, "Lunch Set", 2005, List.of(noodles, tea));
        customer = customerClient("13800138000");
    }

    private String addAddress(ApiClient client) {
        return client.post("/addressBook", Map.of("consignee", "Demo Customer", "phone", "13800138000",
                        "provinceName", "Zhejiang", "cityName", "Hangzhou", "districtName", "Xihu",
                        "detail", "88 Wensan Road", "sex", "1", "label", "Home"))
                .assertSuccess().data().path("id").asText();
    }

    // ---------------------------------------------------------------- cart

    @Test
    void cartPriceAndNameComeFromTheCatalogueNotFromTheRequest() {
        JsonNode row = customer.post("/shoppingCart/add",
                Map.of("dishId", noodles, "dishFlavor", "Hot", "amount", 0.01, "name", "free food",
                        "image", "evil.jpg")).assertSuccess().data();

        assertThat(row.path("name").asText()).isEqualTo("Dan Dan Noodles");
        assertThat(row.path("amount").decimalValue()).isEqualByComparingTo("13.88");
        assertThat(row.path("image").asText()).isEqualTo("seed.jpg");
        assertThat(row.path("number").asInt()).isEqualTo(1);
    }

    @Test
    void addingTheSameDishAgainIncrementsInsteadOfDuplicating() {
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        JsonNode second = customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess().data();

        assertThat(second.path("number").asInt()).isEqualTo(2);
        JsonNode cart = customer.get("/shoppingCart/list").data();
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).path("number").asInt()).isEqualTo(2);
    }

    @Test
    void sameDishWithDifferentFlavorsAreSeparateRowsAndDoNotMixWithOtherDishes() {
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Mild")).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Hot")).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        // the original code matched on user id twice, so this third add would have merged into the first row
        JsonNode cart = customer.get("/shoppingCart/list").data();

        assertThat(cart).hasSize(3);
        assertThat(cart).extracting(n -> n.path("number").asInt()).containsOnly(1);
        // the menu badge shows the dish total across flavors
        JsonNode third = customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Hot"))
                .assertSuccess().data();
        assertThat(third.path("number").asInt()).isEqualTo(3);
    }

    @Test
    void subtractingRemovesOneUnitAndDeletesTheRowAtZero() {
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();

        assertThat(customer.post("/shoppingCart/sub", Map.of("dishId", tea)).assertSuccess().data()
                .path("number").asInt()).isEqualTo(1);
        assertThat(customer.post("/shoppingCart/sub", Map.of("dishId", tea)).assertSuccess().data()
                .path("number").asInt()).isZero();
        assertThat(customer.get("/shoppingCart/list").data()).isEmpty();
        // subtracting something that is not in the cart is harmless
        assertThat(customer.post("/shoppingCart/sub", Map.of("dishId", tea)).assertSuccess().data()
                .path("number").asInt()).isZero();
    }

    @Test
    void cartsAreIsolatedBetweenCustomers() {
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        ApiClient other = customerClient("13900139000");

        assertThat(other.get("/shoppingCart/list").data()).isEmpty();
        other.delete("/shoppingCart/clean").assertSuccess();
        assertThat(customer.get("/shoppingCart/list").data()).hasSize(1);
    }

    @Test
    void unavailableOrUnknownItemsCannotBeAdded() {
        admin.post("/dish/status/0?ids=" + tea, Map.of()).assertSuccess();

        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertFailure("not available");
        customer.post("/shoppingCart/add", Map.of("dishId", "999")).assertFailure("not available");
        customer.post("/shoppingCart/add", Map.of("name", "no product")).assertFailure("either a dish or a setmeal");
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "setmealId", combo))
                .assertFailure("either a dish or a setmeal");
    }

    // ---------------------------------------------------------------- address book

    @Test
    void withdrawnProductsAlreadyInTheCartCannotBeIncremented() {
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("setmealId", combo)).assertSuccess();
        admin.post("/dish/status/0?ids=" + tea, Map.of()).assertSuccess();
        admin.post("/setmeal/status/0?ids=" + combo, Map.of()).assertSuccess();

        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertFailure("not available");
        customer.post("/shoppingCart/add", Map.of("setmealId", combo)).assertFailure("not available");
        assertThat(customer.get("/shoppingCart/list").data())
                .extracting(row -> row.path("number").asInt()).containsExactly(1, 1);
    }

    @Test
    void firstAddressBecomesDefaultAndDefaultCanBeMoved() {
        String home = addAddress(customer);
        String office = addAddress(customer);

        assertThat(customer.get("/addressBook/default").data().path("id").asText()).isEqualTo(home);

        customer.put("/addressBook/default", Map.of("id", office)).assertSuccess();
        assertThat(customer.get("/addressBook/default").data().path("id").asText()).isEqualTo(office);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM address_book WHERE is_default = 1", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void addressesCanBeEditedAndDeletedAndADefaultIsAlwaysPromoted() {
        String home = addAddress(customer);
        String office = addAddress(customer);

        customer.put("/addressBook", Map.of("id", office, "consignee", "Renamed", "phone", "13800138000",
                "detail", "new street")).assertSuccess();
        assertThat(customer.get("/addressBook/" + office).data().path("consignee").asText()).isEqualTo("Renamed");

        customer.delete("/addressBook?ids=" + home).assertSuccess(); // home was the default
        assertThat(customer.get("/addressBook/list").data()).hasSize(1);
        assertThat(customer.get("/addressBook/default").assertSuccess().data().path("id").asText())
                .isEqualTo(office);
    }

    @Test
    void customersCannotReadChangeOrDeleteEachOthersAddresses() {
        String mine = addAddress(customer);
        ApiClient other = customerClient("13900139000");

        other.get("/addressBook/" + mine).assertFailure("not found");
        other.put("/addressBook", Map.of("id", mine, "consignee", "Hijacked")).assertFailure("not found");
        other.put("/addressBook/default", Map.of("id", mine)).assertFailure("not found");
        other.delete("/addressBook?ids=" + mine).assertSuccess(); // silently scoped to the caller's own rows

        assertThat(customer.get("/addressBook/" + mine).data().path("consignee").asText())
                .isEqualTo("Demo Customer");
    }

    // ---------------------------------------------------------------- orders

    @Test
    void submittingAnOrderPricesItExactlyAndLinksTheLineItems() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Hot")).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("setmealId", combo)).assertSuccess();

        customer.post("/order/submit", Map.of("addressBookId", address, "payMethod", 1, "remark", "no onions"))
                .assertSuccess();

        // 13.88 + 2 * 3.99 + 20.05 = 41.91 (the original code truncated every line to an int and stored 13 + 7 + 20 = 40)
        Map<String, Object> order = jdbc.queryForMap("SELECT * FROM orders");
        assertThat((BigDecimal) order.get("amount")).isEqualByComparingTo("41.91");
        assertThat(order.get("status")).isEqualTo(2);
        assertThat(order.get("number")).isEqualTo(String.valueOf(order.get("id")));
        assertThat(order.get("consignee")).isEqualTo("Demo Customer");
        assertThat(order.get("address")).isEqualTo("ZhejiangHangzhouXihu88 Wensan Road");
        assertThat(order.get("remark")).isEqualTo("no onions");

        // every line item points at the ORDER (the original code stored the cart row id there)
        List<Map<String, Object>> lines = jdbc.queryForList("SELECT * FROM order_detail ORDER BY name");
        assertThat(lines).hasSize(3);
        assertThat(lines).extracting(l -> l.get("order_id")).containsOnly(order.get("id"));
        assertThat(lines).extracting(l -> l.get("name"))
                .containsExactly("Dan Dan Noodles", "Jasmine Tea", "Lunch Set");
        assertThat(lines.get(0).get("dish_flavor")).isEqualTo("Hot");

        // the cart is empty afterwards
        assertThat(customer.get("/shoppingCart/list").data()).isEmpty();
    }

    @Test
    void orderUsesThePriceAtCheckoutNotAtAddToCartTime() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        JsonNode edit = admin.get("/dish/" + tea).data();
        admin.put("/dish", Map.of("id", tea, "name", "Jasmine Tea", "categoryId", edit.path("categoryId").asText(),
                "price", 500, "image", "seed.jpg", "status", 1, "flavors", List.of())).assertSuccess();

        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();

        assertThat(jdbc.queryForObject("SELECT amount FROM orders", BigDecimal.class)).isEqualByComparingTo("5.00");
    }

    @Test
    void concurrentCheckoutSubmitsTheCartOnlyOnce() throws Exception {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        Long userId = jdbc.queryForObject("SELECT id FROM `user` WHERE phone = '13800138000'", Long.class);
        var pool = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("checkout workers did not start");
                    }
                    Orders request = new Orders();
                    request.setAddressBookId(Long.valueOf(address));
                    try {
                        orderService.submit(userId, request);
                        return true;
                    } catch (CustomException emptyCart) {
                        assertThat(emptyCart.getMessage()).contains("cart is empty");
                        return false;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int submitted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(15, TimeUnit.SECONDS)) {
                    submitted++;
                }
            }
            assertThat(submitted).as("concurrent retries must not duplicate the order").isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM order_detail", Integer.class)).isEqualTo(1);
            assertThat(customer.get("/shoppingCart/list").data()).isEmpty();
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void submitIsRejectedForEmptyCartsForeignAddressesAndWithdrawnItems() {
        String address = addAddress(customer);
        customer.post("/order/submit", Map.of("addressBookId", address)).assertFailure("cart is empty");

        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        ApiClient other = customerClient("13900139000");
        String othersAddress = addAddress(other);
        customer.post("/order/submit", Map.of("addressBookId", othersAddress))
                .assertFailure("no address information");

        admin.post("/dish/status/0?ids=" + tea, Map.of()).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertFailure("no longer available");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isZero();
        assertThat(customer.get("/shoppingCart/list").data()).hasSize(1); // failed checkout keeps the cart
    }

    @Test
    void customerSeesOwnOrdersNewestFirstWithLineItemsAndCanOrderAgain() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Mild")).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Mild")).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        // make "ordering" deterministic: the second order is the most recent
        jdbc.update("UPDATE orders SET order_time = order_time - INTERVAL 1 HOUR WHERE amount = 3.99");

        JsonNode page = customer.get("/order/userPage?page=1&pageSize=10").assertSuccess().data();

        assertThat(page.path("total").asInt()).isEqualTo(2);
        JsonNode newest = page.path("records").get(0);
        assertThat(newest.path("amount").decimalValue()).isEqualByComparingTo("27.76");
        assertThat(newest.path("orderDetails")).hasSize(1);
        assertThat(newest.path("orderDetails").get(0).path("number").asInt()).isEqualTo(2);

        // other customers see nothing of it
        assertThat(customerClient("13900139000").get("/order/userPage?page=1&pageSize=10").data()
                .path("total").asInt()).isZero();

        customer.post("/order/again", Map.of("id", newest.path("id").asText())).assertSuccess();
        JsonNode cart = customer.get("/shoppingCart/list").data();
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).path("number").asInt()).isEqualTo(2);
        assertThat(cart.get(0).path("dishFlavor").asText()).isEqualTo("Mild");
    }

    @Test
    void orderAgainSkipsWithdrawnItemsAndRefusesForeignOrders() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/shoppingCart/add", Map.of("dishId", noodles, "dishFlavor", "Hot")).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        String orderId = jdbc.queryForObject("SELECT id FROM orders", String.class);
        admin.post("/dish/status/0?ids=" + tea, Map.of()).assertSuccess();

        customer.post("/order/again", Map.of("id", orderId)).assertSuccess();
        assertThat(customer.get("/shoppingCart/list").data()).hasSize(1);

        customerClient("13900139000").post("/order/again", Map.of("id", orderId)).assertFailure("order not found");

        admin.post("/dish/status/0?ids=" + noodles, Map.of()).assertSuccess();
        customer.delete("/shoppingCart/clean").assertSuccess();
        customer.post("/order/again", Map.of("id", orderId)).assertFailure("none of the items");
    }

    @Test
    void adminFiltersOrdersAndWalksThemThroughDispatchAndCompletion() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        String id = jdbc.queryForObject("SELECT id FROM orders", String.class);
        String number = jdbc.queryForObject("SELECT number FROM orders", String.class);

        // listing and filtering
        assertThat(admin.get("/order/page?page=1&pageSize=10").data().path("total").asInt()).isEqualTo(1);
        assertThat(admin.get("/order/page?page=1&pageSize=10&number=" + number.substring(5, 12)).data()
                .path("total").asInt()).isEqualTo(1);
        assertThat(admin.get("/order/page?page=1&pageSize=10&number=zzz").data().path("total").asInt()).isZero();
        assertThat(admin.get("/order/page?page=1&pageSize=10&beginTime=2000-01-01%2000:00:00"
                + "&endTime=2000-12-31%2023:59:59").data().path("total").asInt()).isZero();
        assertThat(admin.get("/order/page?page=1&pageSize=10&beginTime=2000-01-01%2000:00:00"
                + "&endTime=2999-12-31%2023:59:59").data().path("total").asInt()).isEqualTo(1);

        // life cycle: 2 awaiting dispatch -> 3 dispatched -> 4 completed
        admin.put("/order", Map.of("id", id, "status", 4)).assertFailure("can not move");   // cannot skip a step
        admin.put("/order", Map.of("id", id, "status", 3)).assertSuccess();
        admin.put("/order", Map.of("id", id, "status", 2)).assertFailure("can not move");   // cannot go back
        admin.put("/order", Map.of("id", id, "status", 4)).assertSuccess();
        admin.put("/order", Map.of("id", id, "status", 5)).assertFailure("can not move");   // completed is final
        assertThat(jdbc.queryForObject("SELECT status FROM orders", Integer.class)).isEqualTo(4);

        admin.put("/order", Map.of("id", "424242", "status", 3)).assertFailure("order not found");
        admin.put("/order", Map.of("id", id)).assertFailure("status is required");
    }

    @Test
    void openOrdersCanBeCancelledButNotAfterwardsAdvanced() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        String id = jdbc.queryForObject("SELECT id FROM orders", String.class);

        admin.put("/order", Map.of("id", id, "status", 5)).assertSuccess();
        admin.put("/order", Map.of("id", id, "status", 3)).assertFailure("can not move");
    }

    @Test
    void customersCannotUseAdminOrderEndpoints() {
        String address = addAddress(customer);
        customer.post("/shoppingCart/add", Map.of("dishId", tea)).assertSuccess();
        customer.post("/order/submit", Map.of("addressBookId", address)).assertSuccess();
        String id = jdbc.queryForObject("SELECT id FROM orders", String.class);

        customer.put("/order", Map.of("id", id, "status", 3)).assertNotLoggedIn();
        customer.get("/order/page?page=1&pageSize=10").assertNotLoggedIn();
        assertThat(jdbc.queryForObject("SELECT status FROM orders", Integer.class)).isEqualTo(2);
    }
}
