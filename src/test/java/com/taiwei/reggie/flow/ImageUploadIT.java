package com.taiwei.reggie.flow;

import com.taiwei.reggie.support.AbstractIntegrationTest;
import com.taiwei.reggie.support.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

class ImageUploadIT extends AbstractIntegrationTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1, 2, 3, 4};

    private ApiClient.Response upload(ApiClient client, String filename, byte[] content) {
        return client.perform(MockMvcRequestBuilders.multipart("/common/upload")
                .file(new MockMultipartFile("file", filename, "application/octet-stream", content)));
    }

    @Test
    void uploadedImagesAreStoredUnderARandomNameAndServedBack() {
        ApiClient admin = adminClient();

        String name = upload(admin, "My Dish.PNG", PNG).assertSuccess().data().asText();

        assertThat(name).matches("[0-9a-f-]{36}\\.png"); // client-chosen names are never used on disk
        ApiClient.Response download = admin.get("/common/download?name=" + name);
        assertThat(download.status()).isEqualTo(200);
        assertThat(download.contentType()).isEqualTo("image/png");
        assertThat(download.bytes()).isEqualTo(PNG);
    }

    @Test
    void onlyImageExtensionsAreAccepted() {
        ApiClient admin = adminClient();

        upload(admin, "shell.jsp", PNG).assertFailure("images");
        upload(admin, "page.html", PNG).assertFailure("images");
        upload(admin, "no-extension", PNG).assertFailure("images");
        upload(admin, "empty.png", new byte[0]).assertFailure("no file");
    }

    @Test
    void customersCannotUpload() {
        upload(customerClient("13800138000"), "a.png", PNG).assertNotLoggedIn();
    }

    @Test
    void downloadRefusesPathTraversalAndUnknownFiles() {
        ApiClient admin = adminClient();

        assertThat(admin.get("/common/download?name=../application.yml").status()).isEqualTo(404);
        assertThat(admin.get("/common/download?name=..%2F..%2Fetc%2Fpasswd").status()).isEqualTo(404);
        assertThat(admin.get("/common/download?name=%2Fetc%2Fpasswd").status()).isEqualTo(404);
        assertThat(admin.get("/common/download?name=missing.png").status()).isEqualTo(404);
        assertThat(admin.get("/common/download").status()).isEqualTo(404);
    }

    @Test
    void bundledDemoImagesAreServedFromTheClasspath() {
        ApiClient.Response response = customerClient("13800138000").get("/common/download?name=kung-pao-chicken.jpg");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.contentType()).isEqualTo("image/jpeg");
        assertThat(response.bytes().length).isGreaterThan(1000);
    }
}
