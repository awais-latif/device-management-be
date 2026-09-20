package com.example.devicemanagement.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.devicemanagement.exception.InvalidSortException;
import com.example.devicemanagement.generated.model.SortDirection;

class StringToSortDirectionConverterTest {

    private final StringToSortDirectionConverter converter = new StringToSortDirectionConverter();

    @Test
    void convertsValue() {
        Assertions.assertThat(converter.convert("asc")).isEqualTo(SortDirection.ASC);
        Assertions.assertThat(converter.convert("desc")).isEqualTo(SortDirection.DESC);
    }

    @Test
    void passesUnknownValue() {
        Assertions.assertThatThrownBy(() -> converter.convert("unknow"))
                .isInstanceOf(InvalidSortException.class)
                .hasMessage("Unknown sort direction: unknow. Allowed: asc, desc");
    }
}
