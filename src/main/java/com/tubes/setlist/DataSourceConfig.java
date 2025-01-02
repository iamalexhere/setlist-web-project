package com.tubes.setlist;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataSourceConfig {
   @Value("${spring.datasource.url}")
   private String url;
   @Value("${spring.datasource.username}")
   private String username;
   @Value("${spring.datasource.password}")
   private String password;

   @Bean
   @Profile("!test")
   public DataSource dataSource() {
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setUrl(this.url);
      dataSource.setUsername(this.username);
      dataSource.setPassword(this.password);
      return dataSource;
   }
}

