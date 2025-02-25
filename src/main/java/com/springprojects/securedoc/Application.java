package com.springprojects.securedoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

//	@Bean
//	CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
//		return args -> {
//			RequestContext.setUserId(0L);
//			
//			var userRole = new RoleEntity();
//		    userRole.setName(Authority.USER.name());
//		    userRole.setAuthorities(Authority.USER);
//		    roleRepository.save(userRole);
//		    
//		    var adminRole = new RoleEntity();
//		    adminRole.setName(Authority.ADMIN.name());
//		    adminRole.setAuthorities(Authority.USER);
//		    roleRepository.save(adminRole);
//		    
//		    RequestContext.start();
//		};
//	}
}


