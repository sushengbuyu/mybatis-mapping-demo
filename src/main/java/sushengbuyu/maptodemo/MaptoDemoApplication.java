package sushengbuyu.maptodemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("sushengbuyu.maptodemo")
@SpringBootApplication
public class MaptoDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaptoDemoApplication.class, args);
    }

}
