package com.example.devicemanagement.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.exception.InvalidSortException;
import com.example.devicemanagement.generated.model.DeviceSortField;

class StringToSortFieldConverterTest {

    private final StringToSortFieldConverter converter = new StringToSortFieldConverter();

    @Test
    void convertsValue() {
        Assertions.assertThat(converter.convert("createdAt")).isEqualTo(DeviceSortField.CREATED_AT);
        Assertions.assertThat(converter.convert("name")).isEqualTo(DeviceSortField.NAME);
        Assertions.assertThat(converter.convert("brand")).isEqualTo(DeviceSortField.BRAND);
        Assertions.assertThat(converter.convert("state")).isEqualTo(DeviceSortField.STATE);
    }

    @Test
    void passesUnknownValue() {
        Assertions.assertThatThrownBy(() -> converter.convert("unknown"))
                .isInstanceOf(InvalidSortException.class)
                .hasMessage("Unknown sort field: unknown. Allowed: createdAt, name, brand, state");
    }
}
