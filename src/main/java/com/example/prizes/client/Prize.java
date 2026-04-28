package com.example.prizes.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Prize", description = "Represents a prize with its name and threshold")
public record Prize(
        @Schema(description = "The name of the prize", example = "Gold Medal")
        String prizeName,
        @Schema(description = "The threshold value for this prize", example = "100")
        int threshold) {
}
