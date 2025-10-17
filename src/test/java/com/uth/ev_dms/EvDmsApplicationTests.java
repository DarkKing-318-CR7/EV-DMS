package com.uth.ev_dms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class EvDmsApplicationTests {

	public static void main(String[] args){
		System.out.println(new BCryptPasswordEncoder().encode("123456"));
	}

}
