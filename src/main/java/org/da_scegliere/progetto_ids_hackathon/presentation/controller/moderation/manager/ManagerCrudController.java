package org.da_scegliere.progetto_ids_hackathon.presentation.controller.moderation.manager;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.services.moderation.manager.ManagerAccountService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.manager.CreateManagerRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.manager.UpdateManagerRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.manager.ManagerResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.ManagerMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/managers")
public class ManagerCrudController {

    private final ManagerAccountService managerAccountService;

    @PostMapping
    public ResponseEntity<Void> createManager(@Valid @RequestBody CreateManagerRequest request) {
        log.info("Received create manager request email={}.", request.email());
        Manager createdManager = managerAccountService.createManager(
                request.name(),
                request.age(),
                request.email()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{managerId}")
                .buildAndExpand(createdManager.getId())
                .toUri();
        log.info("Manager creation request completed managerId={}.", createdManager.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{managerId}")
    public ResponseEntity<ManagerResponse> getManager(@PathVariable UUID managerId) {
        return ResponseEntity.ok(ManagerMapper.toResponse(managerAccountService.getManagerById(managerId)));
    }

    @GetMapping
    public ResponseEntity<List<ManagerResponse>> getManagers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) AccountState status
    ) {
        int activeFilters = (email != null ? 1 : 0) + (name != null ? 1 : 0);
        if (activeFilters > 1) {
            throw new IllegalArgumentException("Use only one among email or name.");
        }

        List<Manager> managers;
        if (email != null) {
            managers = List.of(managerAccountService.getManagerByEmail(email));
        } else if (name != null) {
            managers = List.of(managerAccountService.getManagerByName(name));
        } else {
            managers = managerAccountService.getAllManagers();
        }

        if (status != null) {
            managers = managers.stream()
                    .filter(manager -> manager.getAccountStatus() == status)
                    .toList();
        }

        return ResponseEntity.ok(managers.stream().map(ManagerMapper::toResponse).toList());
    }

    @PatchMapping("/{managerId}")
    public ResponseEntity<ManagerResponse> updateManager(
            @PathVariable UUID managerId,
            @Valid @RequestBody UpdateManagerRequest request
    ) {
        Manager updatedManager = managerAccountService.changeManagerName(managerId, request.name());
        return ResponseEntity.ok(ManagerMapper.toResponse(updatedManager));
    }

    @DeleteMapping("/{managerId}")
    public ResponseEntity<Void> deleteManager(@PathVariable UUID managerId) {
        managerAccountService.deleteManager(managerId);
        return ResponseEntity.noContent().build();
    }
}
