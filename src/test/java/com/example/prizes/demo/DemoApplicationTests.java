package com.example.prizes.demo;

import com.example.prizes.client.Prize;
import com.example.prizes.client.PrizeClient;
import com.example.prizes.client.PrizePage;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
        PrizeEntity expectedPrize = new PrizeEntity();
        expectedPrize.setPrizeName("book");
        expectedPrize.setThreshold(10);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<PrizeEntity> expectedPrizes = new PageImpl<>(List.of(expectedPrize), pageable, 1);
        when(prizeDao.getPrizesByThreshold(100, pageable)).thenReturn(expectedPrizes);

        Page<PrizeEntity> prizes = prizeService.getAllPrizes(100, pageable);

        assertEquals(expectedPrizes, prizes);
        verify(prizeDao).getPrizesByThreshold(100, pageable);
    }

    @Test
    void clientGetPrizesFetchesAllPages() {
        List<Integer> requestedPages = new ArrayList<>();
        Prize firstPrize = new Prize("book", 10);
        Prize secondPrize = new Prize("bike", 20);
        Prize thirdPrize = new Prize("laptop", 30);
        List<PrizePage> pages = List.of(
                new PrizePage(List.of(firstPrize), 0, 1000, 3, 3, true, false),
                new PrizePage(List.of(secondPrize), 1, 1000, 3, 3, false, false),
                new PrizePage(List.of(thirdPrize), 2, 1000, 3, 3, false, true)
        );
        PrizeClient prizeClient = new PrizeClient() {
            @Override
            public PrizePage getPrizes(int page, int size) {
                requestedPages.add(page);
                assertEquals(PrizeClient.DEFAULT_PAGE_SIZE, size);
                return pages.get(page);
            }

            @Override
            public int takePrize(String name) {
                return 0;
            }
        };

        List<Prize> prizes = prizeClient.getPrizes();

        assertEquals(List.of(firstPrize, secondPrize, thirdPrize), prizes);
        assertEquals(List.of(0, 1, 2), requestedPages);
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

        @Test
        void takePrizeDecreasesAmountAndReturnsRemainingAmount() {
            when(inventoryDao.decreaseAmountByPrizeName("book")).thenReturn(1);
            when(inventoryDao.getAmountByName("book")).thenReturn(Optional.of(2));

            int remainingAmount = inventoryService.takePrize("Book");

            assertEquals(2, remainingAmount);
            verify(inventoryDao).decreaseAmountByPrizeName("book");
            verify(inventoryDao).getAmountByName("book");
        }

        @Test
        void takePrizeThrowsWhenPrizeIsOutOfStock() {
            when(inventoryDao.decreaseAmountByPrizeName("book")).thenReturn(0);
            when(inventoryDao.getAmountByName("book")).thenReturn(Optional.of(0));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> inventoryService.takePrize("Book")
            );

            assertEquals("Prize is out of stock: Book", exception.getMessage());
            verify(inventoryDao).decreaseAmountByPrizeName("book");
            verify(inventoryDao).getAmountByName("book");
        }

        @Test
        void takePrizeThrowsWhenPrizeIsNotInInventory() {
            when(inventoryDao.decreaseAmountByPrizeName("book")).thenReturn(0);
            when(inventoryDao.getAmountByName("book")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inventoryService.takePrize("Book")
            );

            assertEquals("Prize not found in inventory: Book", exception.getMessage());
            verify(inventoryDao).decreaseAmountByPrizeName("book");
            verify(inventoryDao).getAmountByName("book");
        }
    }
}
