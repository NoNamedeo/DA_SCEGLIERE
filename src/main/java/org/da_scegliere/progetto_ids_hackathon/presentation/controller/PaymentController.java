package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.PaymentService;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.PrizeAwardRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hackathons")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{hackathonId}/prize-awards")
    public ResponseEntity<Void> awardPrize(
            @PathVariable UUID hackathonId,
            @Valid @RequestBody PrizeAwardRequest request
    ) {
        paymentService.awardPrizeToWinner(request.prize(), hackathonId);
        return ResponseEntity.noContent().build();
    }
}
