package intern.shopadbox.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		/*
		String profile = System.getProperty("spring.profiles.active");
		if(profile == null) {
			System.setProperty("spring.profiles.active", "develop");
		}

		 */
		System.out.println(System.getProperty("spring.profiles.active"));
		SpringApplication.run(ProjectApplication.class, args);
	}

}
