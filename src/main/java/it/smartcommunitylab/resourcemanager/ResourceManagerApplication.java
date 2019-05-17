package it.smartcommunitylab.resourcemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import it.smartcommunitylab.resourcemanager.model.Resource;
import it.smartcommunitylab.resourcemanager.service.ProviderLocalService;
import it.smartcommunitylab.resourcemanager.service.ResourceLocalService;

@EnableResourceServer
@SpringBootApplication
public class ResourceManagerApplication {
	@Autowired
	ProviderLocalService service;

	@Autowired
	ResourceLocalService resourceService;

	public static void main(String[] args) {
		SpringApplication.run(ResourceManagerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

//			System.out.println("Ready.");
			printBanner();

//			service.getProviders().forEach((key, p) -> System.out.println((key + ":" + p.getId())));
//
//			// test
//			Resource res = resourceService.create("default", "mat", SystemKeys.TYPE_SQL, "nullProvider", null);
//			System.out.println("resource " + res.getId() + " uri " + res.getUri());
//
//			// parse
//			String username = SqlUtil.getUsername(res.getUri());
//			System.out.println("resource " + res.getId() + " user " + SqlUtil.getUsername(res.getUri()));
//			System.out.println("resource " + res.getId() + " password " + SqlUtil.getPassword(res.getUri()));
//			System.out.println("resource " + res.getId() + " provider " + SqlUtil.getProvider(res.getUri()));
//			System.out.println("resource " + res.getId() + " endpoint " + SqlUtil.getEndpoint(res.getUri()));
//			System.out.println("resource " + res.getId() + " host " + SqlUtil.getHost(res.getUri()));
//			System.out.println("resource " + res.getId() + " port " + SqlUtil.getPort(res.getUri()));
//			System.out.println("resource " + res.getId() + " database " + SqlUtil.getDatabase(res.getUri()));

		};
	}

	public void printBanner() {
		System.out.println("======================================");
		System.out.println(" ____                _                ");
		System.out.println("|  _ \\ ___  __ _  __| |_   _          ");
		System.out.println("| |_) / _ \\/ _` |/ _` | | | |         ");
		System.out.println("|  _ <  __/ (_| | (_| | |_| |_        ");
		System.out.println("|_| \\_\\___|\\__,_|\\__,_|\\__, (_)       ");
		System.out.println(" :resourcemanager      |___/          ");
		System.out.println("======================================");
	}

}
