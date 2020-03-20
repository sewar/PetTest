import io.restassured.http.ContentType;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class PetTest {
    public static final String BASE_URL = "https://petstore.swagger.io/v2";

    @Test
    public void addPetThenUpdateItThenDeleteIt() throws IOException {
        // Create a new Pet record
        File file = new File("new_pet.json");
        String jsonContent = FileUtils.readFileToString(file, "UTF-8");

        long petId = given()
                .body(jsonContent)
                .contentType(ContentType.JSON)
                .post(BASE_URL + "/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo("kitten"))
                .body("photoUrls", hasSize(2))
                .body("status", equalTo("available"))
                .extract()
                .path("id");

        // Verify Pet record exists
        given()
                .get(BASE_URL + "/pet/{petId}", petId)
                .then()
                .statusCode(200);

        // Update the Pet record name and status
        File file2 = new File("updated_pet.json");
        String jsonContent2 = FileUtils.readFileToString(file2, "UTF-8")
                .replace("$ID$", String.valueOf(petId));

        given()
                .body(jsonContent2)
                .contentType(ContentType.JSON)
                .post(BASE_URL + "/pet")
                .then()
                .statusCode(200)
                .body("name", equalTo("cat"))
                .body("status", equalTo("sold"));

        // Delete the Pet record
        given()
                .body(jsonContent2)
                .contentType(ContentType.JSON)
                .delete(BASE_URL + "/pet/{petId}", petId)
                .then()
                .statusCode(200);

        // Verify Pet record was deleted
        given()
                .get(BASE_URL + "/pet/{petId}", petId)
                .then()
                .statusCode(404);
    }
}
