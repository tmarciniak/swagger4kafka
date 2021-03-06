package com.stavshamir.swagger4kafka.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stavshamir.swagger4kafka.test.Utils;
import io.swagger.annotations.ApiModel;
import io.swagger.models.properties.StringProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class ModelsServiceTest {

    private ModelsService modelsService = new ModelsService();

    private static final String EXAMPLES_PATH = "/models/examples";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void simpleObject() {
        // Given a registered simple object
        // When register is called
        String modelName = modelsService.register(SimpleFoo.class);

        // Then the returned value is the class name
        assertThat(modelName)
                .isEqualTo("SimpleFoo");
    }

    @Test
    public void register_annotatedObject() {
        // Given a registered simple object annotated with @ApiModel
        // When register is called
        String modelName = modelsService.register(AnnotatedFoo.class);

        // Then the returned value is the @ApiModel value
        assertThat(modelName)
                .isEqualTo("ApiModelFoo");
    }

    @Test
    public void getExample_simpleObject() throws IOException {
        // Given a registered simple object
        String modelName = modelsService.register(SimpleFoo.class);

        // When getExample is called
        Map<String, Object> example = modelsService.getExample(modelName);
        Map expectedExample = jsonResourceAsMap(EXAMPLES_PATH + "/simple-foo.json");

        // Then it returns the correct example object as json
        assertThat(example)
                .isEqualTo(expectedExample);
    }

    @Test
    public void getExample_compositeObject() throws IOException {
        // Given a registered composite object
        String modelName = modelsService.register(CompositeFoo.class);

        // When getExample is called
        Map<String, Object> example = modelsService.getExample(modelName);
        Map expectedExample = jsonResourceAsMap(EXAMPLES_PATH + "/composite-foo.json");

        // Then it returns the correct example object as json
        assertThat(example)
                .isEqualTo(expectedExample);
    }

    @Test
    public void getExample_annotatedObject() throws IOException {
        // Given a registered simple object annotated with @ApiModel
        String modelName = modelsService.register(AnnotatedFoo.class);

        // When getExample is called
        Map<String, Object> example = modelsService.getExample(modelName);
        Map expectedExample = jsonResourceAsMap(EXAMPLES_PATH + "/simple-foo.json");

        // Then it returns the correct example object as json
        assertThat(example)
                .isEqualTo(expectedExample);
    }

    @Test
    public void testEnumWithJsonValue() {
        // Given a class with an enum property containing a @JsonValue method
        // When the class is registered
        String modelName = modelsService.register(FooWithEnumWithValues.class);

        StringProperty enumProperty = (StringProperty) modelsService.getDefinitions()
                .get(modelName)
                .getProperties()
                .get("e");

        // Then its enum values are correctly serialized
        assertThat(enumProperty.getEnum())
                .containsExactlyInAnyOrder("foo 1", "foo 2");
    }

    @Test
    public void testCompositeFooWithEnumWithValues() {
        // Given a composite class with a property which is a class with an enum property containing a @JsonValue method
        // When the class is registered
        modelsService.register(CompositeFooWithEnumWithValues.class);

        StringProperty enumProperty = (StringProperty) modelsService.getDefinitions()
                .get(FooWithEnumWithValues.class.getSimpleName())
                .getProperties()
                .get("e");

        // Then the enum values of the inner class are correctly serialized
        assertThat(enumProperty.getEnum())
                .containsExactlyInAnyOrder("foo 1", "foo 2");
    }

    @Test
    public void testCompositeFooWithEnumWithValuesInList() {
        // Given a composite class with a property which is a class with an enum property containing a @JsonValue method
        // When the class is registered
        modelsService.register(CompositeFooWithEnumWithValuesInList.class);

        StringProperty enumProperty = (StringProperty) modelsService.getDefinitions()
                .get(FooWithEnumWithValues.class.getSimpleName())
                .getProperties()
                .get("e");

        // Then the enum values of the inner class are correctly serialized
        assertThat(enumProperty.getEnum())
                .containsExactlyInAnyOrder("foo 1", "foo 2");
    }

    @Test
    public void getDefinitions() throws IOException {
        Map expectedDefinitions = jsonResourceAsMap("/models/definitions.json");

        // Given registered classes
        modelsService.register(CompositeFoo.class);
        modelsService.register(FooWithEnum.class);

        // When getModelsAsJson is called
        String actualDefinitionsJson = objectMapper.writeValueAsString(modelsService.getDefinitions());
        Map actualDefinitions = objectMapper.readValue(actualDefinitionsJson, Map.class);

        // Then it contains the correctly serialized modelsService
        assertThat(actualDefinitions)
                .isEqualTo(expectedDefinitions);
    }

    private Map jsonResourceAsMap(String path) throws IOException {
        return Utils.jsonResourceAsMap(this.getClass(), path);
    }

    @Data
    @NoArgsConstructor
    @ApiModel("ApiModelFoo")
    private static class AnnotatedFoo {
        private String s;
        private boolean b;
    }

    @Data
    @NoArgsConstructor
    private static class SimpleFoo {
        private String s;
        private boolean b;
    }

    @Data
    @NoArgsConstructor
    private static class CompositeFoo {
        private String s;
        private SimpleFoo f;
    }

    @Data
    @NoArgsConstructor
    private static class FooWithEnum {
        private String s;
        private Bar b;

        private enum Bar {
            BAR1, BAR2
        }
    }

    @Data
    @NoArgsConstructor
    private static class FooWithEnumWithValues {
        private String s;
        private EnumWithJsonValue e;
    }

    @Data
    @NoArgsConstructor
    private static class CompositeFooWithEnumWithValues {
        private String s;
        private FooWithEnumWithValues f;
    }

    @Data
    @NoArgsConstructor
    private static class CompositeFooWithEnumWithValuesInList {
        private String s;
        private List<FooWithEnumWithValues> f;
    }

    public enum EnumWithJsonValue {

        FOO1 ("foo 1"),
        FOO2 ("foo 2");

        private final String id;

        EnumWithJsonValue(String id) {
            this.id = id;
        }

        @JsonValue
        public String getValue() {
            return id;
        }

    }

}