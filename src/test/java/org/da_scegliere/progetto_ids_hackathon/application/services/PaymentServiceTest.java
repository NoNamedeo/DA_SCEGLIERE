package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.PaymentStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.PaymentProviderException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.PaymentFailedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.WinnerNotProclaimedException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentStrategy paymentStrategy;

    @Mock
    private Hackathon hackathon;

    @Mock
    private Team winner;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-03-14T10:00:00Z"), ZoneOffset.UTC);
        paymentService = new PaymentService(paymentStrategy, fixedClock);
    }

    @Test
    void awardPrizeToWinnerWhenWinnerNotProclaimedThrowsException() {
        when(hackathon.getWinner()).thenReturn(null);

        assertThrows(
                WinnerNotProclaimedException.class,
                () -> paymentService.awardPrizeToWinner(BigDecimal.TEN, hackathon)
        );
        verify(paymentStrategy, never()).awardPrize(BigDecimal.TEN, winner);
    }

    @Test
    void awardPrizeToWinnerWhenAlreadyPaidIsIdempotent() {
        when(hackathon.getWinner()).thenReturn(winner);
        when(hackathon.isPrizeAlreadyPaid()).thenReturn(true);

        boolean executed = paymentService.awardPrizeToWinner(BigDecimal.TEN, hackathon);

        assertFalse(executed);
        verify(paymentStrategy, never()).awardPrize(BigDecimal.TEN, winner);
    }

    @Test
    void awardPrizeToWinnerWhenProviderFailsThrowsPaymentFailedException() {
        when(hackathon.getWinner()).thenReturn(winner);
        when(hackathon.isPrizeAlreadyPaid()).thenReturn(false);
        doThrow(new PaymentProviderException("Provider unavailable"))
                .when(paymentStrategy)
                .awardPrize(BigDecimal.TEN, winner);

        assertThrows(
                PaymentFailedException.class,
                () -> paymentService.awardPrizeToWinner(BigDecimal.TEN, hackathon)
        );
        verify(hackathon, never()).markPrizeAsPaid(LocalDate.of(2026, 3, 14));
    }

    @Test
    void awardPrizeToWinnerWhenValidPaysAndMarksPaid() {
        when(hackathon.getWinner()).thenReturn(winner);
        when(hackathon.isPrizeAlreadyPaid()).thenReturn(false);

        boolean executed = paymentService.awardPrizeToWinner(BigDecimal.TEN, hackathon);

        assertTrue(executed);
        verify(paymentStrategy).awardPrize(BigDecimal.TEN, winner);
        verify(hackathon).markPrizeAsPaid(LocalDate.of(2026, 3, 14));
    }
}
