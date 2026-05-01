package com.example.prizes.demo;

import com.example.prizes.data.InventoryDao;
import com.example.prizes.data.PrizeDao;
import com.example.prizes.model.PrizeEntity;
import com.example.prizes.services.InventoryService;
import com.example.prizes.services.PrizeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoApplicationTests {

    @Mock
    private PrizeDao prizeDao;

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private PrizeService prizeService;

    static Stream<Arguments> deletePrizeFailureCases() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        Optional.of(3),
                        Optional.empty(),
                        IllegalStateException.class,
                        "Prize cannot be deleted because inventory amount is not 0",
                        false
                ),
                Arguments.of(
                        Optional.empty(),
                        Optional.empty(),
                        IllegalArgumentException.class,
                        "Prize not found in inventory: Book",
                        false
                ),
                Arguments.of(
                        Optional.of(0),
                        Optional.empty(),
                        IllegalArgumentException.class,
                        "Prize not found in prize list: Book",
                        true
                )
        );
    }

    @ParameterizedTest
    @CsvSource({
            "bike, 10",
            "book, 5"
    })
    void addPrizeSavesPrizeAndThreshold(String prizeName, int threshold) {
        prizeService.addPrize(prizeName, threshold);

        ArgumentCaptor<PrizeEntity> prizeCaptor = ArgumentCaptor.forClass(PrizeEntity.class);
        verify(prizeDao).save(prizeCaptor.capture());

        PrizeEntity savedPrize = prizeCaptor.getValue();
        assertEquals(prizeName, savedPrize.getPrizeName());
        assertEquals(threshold, savedPrize.getThreshold());
    }

    @Test
    void getAllPrizesReturnsDaoValues() {
        PrizeEntity prize = new PrizeEntity();
        prize.setPrizeName("book");
        prize.setThreshold(5);
        List<PrizeEntity> expectedPrizes = List.of(prize);
        when(prizeDao.findAll()).thenReturn(expectedPrizes);

        List<PrizeEntity> prizes = prizeService.getAllPrizes();

        assertEquals(expectedPrizes, prizes);
    }

    @Test
    void deletePrizeRemovesPrizeWhenInventoryAmountIsZero() {
        PrizeEntity prize = new PrizeEntity();
        prize.setPrizeName("book");
        when(inventoryDao.getAmountByName("book")).thenReturn(Optional.of(0));
        when(prizeDao.findByPrizeName("book")).thenReturn(Optional.of(prize));

        prizeService.deletePrize("Book");

        verify(prizeDao).delete(prize);
    }

    @ParameterizedTest
    @MethodSource("deletePrizeFailureCases")
    void deletePrizeThrowsForInvalidStates(
            Optional<Integer> inventoryAmount,
            Optional<PrizeEntity> prizeLookup,
            Class<? extends RuntimeException> expectedException,
            String expectedMessage,
            boolean shouldQueryPrize
    ) {
        when(inventoryDao.getAmountByName("book")).thenReturn(inventoryAmount);
        if (shouldQueryPrize) {
            when(prizeDao.findByPrizeName("book")).thenReturn(prizeLookup);
        }

        RuntimeException exception = assertThrows(
                expectedException,
                () -> prizeService.deletePrize("Book")
        );

        assertEquals(expectedMessage, exception.getMessage());

        if (shouldQueryPrize) {
            verify(prizeDao).findByPrizeName("book");
        } else {
            verify(prizeDao, never()).findByPrizeName("book");
        }
        verify(prizeDao, never()).delete(any());
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class InventoryServiceTests {

        @Mock
        private InventoryDao inventoryDao;

        @InjectMocks
        private InventoryService inventoryService;

        @ParameterizedTest
        @CsvSource({
                "book, book, 7",
                "laptop, laptop, 2"
        })
        void getAmountByNameReturnsDaoValue(String prizeName, String daoName, int expectedAmount) {
            when(inventoryDao.getAmountByName(daoName)).thenReturn(Optional.of(expectedAmount));

            Optional<Integer> amount = inventoryService.getAmountByName(prizeName);

            assertTrue(amount.isPresent());
            assertEquals(expectedAmount, amount.get());
            verify(inventoryDao).getAmountByName(daoName);
        }
    }
}
