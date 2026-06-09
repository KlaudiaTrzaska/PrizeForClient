package com.example.prizes.controllers;

import com.example.prizes.client.Prize;
import com.example.prizes.services.InventoryService;
import com.example.prizes.services.PrizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "Prizes", description = "API endpoints for managing prizes")
public class PrizeController {

    PrizeService prizeService;
    InventoryService inventoryService;

    public PrizeController(PrizeService prizeService, InventoryService inventoryService) {
        this.prizeService = prizeService;
        this.inventoryService = inventoryService;
    }

    @PostMapping("/addPrize")
    @Operation(summary = "Add a new prize", description = "Adds a new prize with the given name and threshold to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prize added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string")))
    })
    public String addPrize(@RequestBody Prize prize) {
        prizeService.addPrize(prize.prizeName(), prize.threshold());
        return "Prize " + prize.prizeName() + " with threshold " + prize.threshold() + " added!";
    }

    @GetMapping("/prizes")
    @Operation(summary = "Get all prizes", description = "Retrieves a list of all available prizes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of prizes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Prize.class)))
    })
    public List<Prize> getPrizes() {
        return prizeService.getAllPrizes()
                .stream()
                .map(entity ->
                        new Prize(entity.getPrizeName(), entity.getThreshold())
                ).toList();
    }

    @GetMapping("/prizes/{name}/amount")
    @Operation(summary = "Get prize amount by name", description = "Retrieves the amount of a prize by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved prize amount",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "integer"))),
            @ApiResponse(responseCode = "404", description = "Prize not found")
    })
    public ResponseEntity<Integer> getPrizeAmount(@PathVariable String name) {
        return inventoryService.getAmountByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/prizes/{name}")
    @Operation(summary = "Delete prize by name", description = "Deletes a prize from the prizes table only when its inventory amount is 0")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prize deleted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Prize cannot be deleted because inventory amount is not 0",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Prize not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string")))
    })
    public ResponseEntity<String> deletePrize(@PathVariable String name) {
        try {
            prizeService.deletePrize(name);
            return ResponseEntity.ok("Prize " + name + " deleted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
