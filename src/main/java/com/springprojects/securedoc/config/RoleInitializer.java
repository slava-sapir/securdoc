package com.springprojects.securedoc.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.repository.RoleRepository;
import com.springprojects.securedoc.enumeration.Authority;

@Component
public class RoleInitializer {
    
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
    	 System.out.println("Initializing roles...");

         if (roleRepository == null) {
             throw new IllegalStateException("RoleRepository is null!");
         }
    	
        if (roleRepository.findByNameIgnoreCase(Authority.USER.name()).isEmpty()) {
            var userRole = new RoleEntity();
            userRole.setName(Authority.USER.name());
            userRole.setAuthorities(Authority.USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByNameIgnoreCase(Authority.ADMIN.name()).isEmpty()) {
            var adminRole = new RoleEntity();
            adminRole.setName(Authority.ADMIN.name());
            adminRole.setAuthorities(Authority.ADMIN);
            roleRepository.save(adminRole);
        }
        
        if (roleRepository.findByNameIgnoreCase(Authority.SUPER_ADMIN.name()).isEmpty()) {
            var super_adminRole = new RoleEntity();
            super_adminRole.setName(Authority.SUPER_ADMIN.name());
            super_adminRole.setAuthorities(Authority.SUPER_ADMIN);
            roleRepository.save(super_adminRole);
        }
        
        if (roleRepository.findByNameIgnoreCase(Authority.MANAGER.name()).isEmpty()) {
        	var managerRole = new RoleEntity();
        	managerRole.setName(Authority.MANAGER.name());
            managerRole.setAuthorities(Authority.MANAGER);
            roleRepository.save(managerRole);
        }
    }
}

