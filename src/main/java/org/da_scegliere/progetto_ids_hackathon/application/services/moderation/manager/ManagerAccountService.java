package org.da_scegliere.progetto_ids_hackathon.application.services.moderation.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service dedicated to manager account CRUD use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerAccountService {

    private final IManagerRepository managerRepository;
    private final IUserRepository userRepository;
    private final IStaffMemberRepository staffMemberRepository;

    @Transactional(readOnly = true)
    public List<Manager> getAllManagers() {
        log.debug("Retrieving all managers.");
        return List.copyOf(managerRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Manager getManagerById(UUID managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("managerId must not be null.");
        }

        return managerRepository.findById(managerId)
                .orElseThrow(() -> new ManagerNotFoundException(managerId));
    }

    @Transactional(readOnly = true)
    public Manager getManagerByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }

        return managerRepository.findByEmail(email)
                .orElseThrow(() -> new ManagerNotFoundException(email, "email"));
    }

    @Transactional(readOnly = true)
    public Manager getManagerByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }

        return managerRepository.findManagerByName(name)
                .orElseThrow(() -> new ManagerNotFoundException(name, "name"));
    }

    @Transactional
    public Manager createManager(String name, int age, String email) {
        log.info("Creating manager with email={}.", email);
        ensureEmailIsAvailable(email);

        Manager manager = new Manager(name, age, email);
        Manager savedManager = managerRepository.save(manager);
        log.info("Created manager managerId={}.", savedManager.getId());
        return savedManager;
    }

    @Transactional
    public Manager changeManagerName(UUID managerId, String newName) {
        log.info("Changing manager name managerId={}.", managerId);
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("newName must not be blank.");
        }

        Manager manager = getManagerById(managerId);
        manager.setName(newName);
        Manager updatedManager = managerRepository.save(manager);
        log.info("Changed manager name managerId={}.", managerId);
        return updatedManager;
    }

    @Transactional
    public void deleteManager(UUID managerId) {
        log.info("Deleting manager managerId={}.", managerId);
        managerRepository.delete(getManagerById(managerId));
        log.info("Deleted manager managerId={}.", managerId);
    }

    private void ensureEmailIsAvailable(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }

        boolean alreadyUsed = managerRepository.existsByEmailIgnoreCase(email)
                || userRepository.existsByEmailIgnoreCase(email)
                || staffMemberRepository.existsByEmailIgnoreCase(email);
        if (alreadyUsed) {
            throw new ManagerEmailAlreadyInUseException(email);
        }
    }
}
