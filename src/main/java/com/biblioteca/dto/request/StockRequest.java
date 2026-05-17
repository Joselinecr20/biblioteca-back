package com.biblioteca.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockRequest {

    @NotNull
    @Valid
    private List<StockItem> items;

    @Data
    public static class StockItem {
        @NotNull(message = "La biblioteca es obligatoria")
        private Integer idBiblioteca;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        private Integer cantidadTotal;
    }
}
